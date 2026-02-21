package com.moviemate.ui.feed

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.moviemate.R
import com.moviemate.data.model.MovieGroup
import com.moviemate.databinding.FragmentHomeFeedBinding
import com.moviemate.ui.MainActivity
import com.moviemate.ui.adapter.MovieFeedAdapter
import com.moviemate.ui.viewmodel.ReviewViewModel

class HomeFeedFragment : Fragment() {

    private var _binding: FragmentHomeFeedBinding? = null
    private val binding get() = _binding!!
    private val reviewViewModel: ReviewViewModel by activityViewModels()
    private lateinit var adapter: MovieFeedAdapter

    private var currentGenreFilters: MutableSet<String> = mutableSetOf()
    private var currentRatingFilter: Int = 0
    private var currentSearchQuery: String = ""
    private var allMovieGroups: List<MovieGroup> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupListeners()
        observeData()
        reviewViewModel.fetchAllReviews()
    }

    private fun setupRecyclerView() {
        adapter = MovieFeedAdapter { group ->
            val action = HomeFeedFragmentDirections.actionHomeToMovieReviews(group.movieTitle)
            findNavController().navigate(action)
        }
        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRecyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString().trim()
                applyFilters()
            }
        })
    }

    private fun setupListeners() {
        binding.menuButton.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }

        binding.fabCreateReview.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_createReview)
        }

        binding.createFirstReviewButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_createReview)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            reviewViewModel.fetchAllReviews()
        }
    }

    private fun observeData() {
        reviewViewModel.groupedMovies.observe(viewLifecycleOwner) { groups ->
            allMovieGroups = groups
            applyFilters()
        }

        reviewViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    private fun applyFilters() {
        var filtered = allMovieGroups

        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.movieTitle.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        if (currentGenreFilters.isNotEmpty()) {
            filtered = filtered.filter { group ->
                currentGenreFilters.any { genre ->
                    group.movieGenres.contains(genre, ignoreCase = true)
                }
            }
        }

        if (currentRatingFilter > 0) {
            filtered = filtered.filter { it.averageRating >= currentRatingFilter }
        }

        adapter.submitList(filtered)

        val isEmpty = filtered.isEmpty()
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.reviewsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateFilterChips() {
        val hasGenre = currentGenreFilters.isNotEmpty()
        val hasRating = currentRatingFilter > 0

        binding.genreFilterChip.visibility = if (hasGenre) View.VISIBLE else View.GONE
        binding.ratingFilterChip.visibility = if (hasRating) View.VISIBLE else View.GONE
        binding.activeFiltersScroll.visibility =
            if (hasGenre || hasRating) View.VISIBLE else View.GONE

        if (hasGenre) binding.genreFilterChip.text = currentGenreFilters.joinToString(", ")
        if (hasRating) binding.ratingFilterChip.text = "★ $currentRatingFilter+"

        binding.genreFilterChip.setOnCloseIconClickListener {
            currentGenreFilters.clear()
            applyFilters()
            updateFilterChips()
        }
        binding.ratingFilterChip.setOnCloseIconClickListener {
            currentRatingFilter = 0
            applyFilters()
            updateFilterChips()
        }
    }

    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_filter)
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_white_background)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val genreChipGroup = dialog.findViewById<ChipGroup>(R.id.genreChipGroup)
        val ratingSpinner = dialog.findViewById<Spinner>(R.id.ratingSpinner)
        val applyButton = dialog.findViewById<MaterialButton>(R.id.applyFilterButton)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancelButton)
        val clearButton = dialog.findViewById<MaterialButton>(R.id.clearFiltersButton)

        // Build genre chips (skip "All Genres" entry)
        val genres = resources.getStringArray(R.array.genres).drop(1)
        genres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre
                isCheckable = true
                isChecked = currentGenreFilters.contains(genre)
                setChipBackgroundColorResource(R.color.selector_chip_background)
                setTextColor(resources.getColorStateList(R.color.selector_chip_text, null))
            }
            genreChipGroup.addView(chip)
        }

        val ratings = resources.getStringArray(R.array.ratings)
        ratingSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, ratings
        )

        val ratingIndex = when (currentRatingFilter) {
            5 -> 1; 4 -> 2; 3 -> 3; 2 -> 4; 1 -> 5; else -> 0
        }
        ratingSpinner.setSelection(ratingIndex)

        applyButton.setOnClickListener {
            currentGenreFilters = (0 until genreChipGroup.childCount)
                .map { genreChipGroup.getChildAt(it) as Chip }
                .filter { it.isChecked }
                .map { it.text.toString() }
                .toMutableSet()
            currentRatingFilter = when (ratingSpinner.selectedItemPosition) {
                1 -> 5; 2 -> 4; 3 -> 3; 4 -> 2; 5 -> 1; else -> 0
            }
            applyFilters()
            updateFilterChips()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        clearButton.setOnClickListener {
            currentGenreFilters.clear()
            currentRatingFilter = 0
            applyFilters()
            updateFilterChips()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
