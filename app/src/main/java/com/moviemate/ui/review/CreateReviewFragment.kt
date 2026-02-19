package com.moviemate.ui.review

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.moviemate.R
import com.moviemate.databinding.FragmentCreateReviewBinding
import com.moviemate.ui.adapter.MovieSuggestionAdapter
import com.moviemate.ui.viewmodel.ReviewViewModel
import com.squareup.picasso.Picasso

class CreateReviewFragment : Fragment() {

    private var _binding: FragmentCreateReviewBinding? = null
    private val binding get() = _binding!!
    private val reviewViewModel: ReviewViewModel by activityViewModels()
    private lateinit var movieAdapter: MovieSuggestionAdapter
    private var selectedRating = 0
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.reviewImage.setImageURI(it)
            binding.reviewImage.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewViewModel.clearResults()
        setupMovieSearch()
        setupRating()
        setupListeners()
        observeData()
    }

    private fun setupMovieSearch() {
        movieAdapter = MovieSuggestionAdapter(requireContext())
        binding.movieSearchAutoComplete.setAdapter(movieAdapter)

        binding.movieSearchAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    reviewViewModel.searchMovies(query)
                }
            }
        })

        binding.movieSearchAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val movie = movieAdapter.getItem(position)
                reviewViewModel.selectMovie(movie)
                binding.movieSearchAutoComplete.dismissDropDown()
            }
    }

    private fun setupRating() {
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStars(stars, selectedRating)
            }
        }
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            star.setImageResource(
                if (index < rating) R.drawable.ic_star_filled else R.drawable.ic_star_empty
            )
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.uploadImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.postReviewButton.setOnClickListener {
            val reviewText = binding.reviewEditText.text.toString().trim()

            if (reviewViewModel.selectedMovie.value == null) {
                Toast.makeText(requireContext(), R.string.error_select_movie, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedRating == 0) {
                Toast.makeText(requireContext(), R.string.error_select_rating, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (reviewText.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_write_review, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            reviewViewModel.createReview(selectedRating, reviewText, selectedImageUri)
        }
    }

    private fun observeData() {
        reviewViewModel.movieSuggestions.observe(viewLifecycleOwner) { movies ->
            movieAdapter.updateMovies(movies)
            if (movies.isNotEmpty()) {
                binding.movieSearchAutoComplete.showDropDown()
            }
        }

        reviewViewModel.selectedMovie.observe(viewLifecycleOwner) { movie ->
            movie?.let {
                binding.selectedMovieTitle.text = it.toString()
                binding.selectedMovieTitle.visibility = View.VISIBLE

                if (it.fullPosterUrl.isNotEmpty()) {
                    binding.moviePosterImage.visibility = View.VISIBLE
                    Picasso.get()
                        .load(it.fullPosterUrl)
                        .placeholder(R.drawable.rounded_card_background)
                        .fit().centerCrop()
                        .into(binding.moviePosterImage)
                }
            }
        }

        reviewViewModel.movieGenresText.observe(viewLifecycleOwner) { genres ->
            if (genres.isNotEmpty()) {
                binding.selectedMovieGenres.text = genres
                binding.selectedMovieGenres.visibility = View.VISIBLE
            }
        }

        reviewViewModel.createResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    Toast.makeText(requireContext(), R.string.review_posted, Toast.LENGTH_SHORT).show()
                    reviewViewModel.fetchAllReviews()
                    findNavController().navigateUp()
                }
                it.onFailure { e ->
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
                reviewViewModel.clearResults()
            }
        }

        reviewViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.postReviewButton.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
