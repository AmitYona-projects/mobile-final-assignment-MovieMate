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
import com.moviemate.R
import com.moviemate.data.model.Review
import com.moviemate.databinding.FragmentHomeFeedBinding
import com.moviemate.ui.MainActivity
import com.moviemate.ui.adapter.ReviewFeedAdapter
import com.moviemate.ui.viewmodel.ReviewViewModel

class HomeFeedFragment : Fragment() {

    private var _binding: FragmentHomeFeedBinding? = null
    private val binding get() = _binding!!
    private val reviewViewModel: ReviewViewModel by activityViewModels()
    private lateinit var adapter: ReviewFeedAdapter

    private var currentGenreFilter: String = ""
    private var currentRatingFilter: Int = 0
    private var currentSearchQuery: String = ""

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
        adapter = ReviewFeedAdapter()
        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRecyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString().trim()
                applyFilters(reviewViewModel.allReviews.value ?: emptyList())
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
        reviewViewModel.allReviews.observe(viewLifecycleOwner) { reviews ->
            applyFilters(reviews)
        }

        reviewViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    private fun applyFilters(reviews: List<Review>) {
        var filtered = reviews

        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.movieTitle.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        if (currentGenreFilter.isNotEmpty() && currentGenreFilter != getString(R.string.all_genres)) {
            filtered = filtered.filter {
                it.movieGenres.contains(currentGenreFilter, ignoreCase = true)
            }
        }

        if (currentRatingFilter > 0) {
            filtered = filtered.filter { it.rating >= currentRatingFilter }
        }

        adapter.submitList(filtered)

        val isEmpty = filtered.isEmpty()
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.reviewsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
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

        val genreSpinner = dialog.findViewById<Spinner>(R.id.genreSpinner)
        val ratingSpinner = dialog.findViewById<Spinner>(R.id.ratingSpinner)
        val applyButton = dialog.findViewById<MaterialButton>(R.id.applyFilterButton)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancelButton)

        val genres = resources.getStringArray(R.array.genres)
        genreSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, genres
        )

        val ratings = resources.getStringArray(R.array.ratings)
        ratingSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, ratings
        )

        applyButton.setOnClickListener {
            val selectedGenre = genreSpinner.selectedItem.toString()
            val selectedRatingIndex = ratingSpinner.selectedItemPosition

            currentGenreFilter = selectedGenre
            currentRatingFilter = when (selectedRatingIndex) {
                1 -> 5
                2 -> 4
                3 -> 3
                4 -> 2
                5 -> 1
                else -> 0
            }

            applyFilters(reviewViewModel.allReviews.value ?: emptyList())
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
