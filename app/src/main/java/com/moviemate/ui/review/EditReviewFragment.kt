package com.moviemate.ui.review

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.moviemate.R
import com.moviemate.databinding.FragmentEditReviewBinding
import com.moviemate.ui.viewmodel.ReviewViewModel
import com.squareup.picasso.Picasso

class EditReviewFragment : Fragment() {

    private var _binding: FragmentEditReviewBinding? = null
    private val binding get() = _binding!!
    private val reviewViewModel: ReviewViewModel by activityViewModels()
    private val args: EditReviewFragmentArgs by navArgs()
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
        _binding = FragmentEditReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewViewModel.clearResults()
        reviewViewModel.loadReview(args.reviewId)
        setupRating()
        setupListeners()
        observeData()
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

        binding.updateReviewButton.setOnClickListener {
            val reviewText = binding.reviewEditText.text.toString().trim()

            if (selectedRating == 0) {
                Toast.makeText(requireContext(), R.string.error_select_rating, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (reviewText.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_write_review, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            reviewViewModel.updateReview(args.reviewId, selectedRating, reviewText, selectedImageUri)
        }
    }

    private fun observeData() {
        reviewViewModel.currentReview.observe(viewLifecycleOwner) { review ->
            review?.let {
                binding.movieTitleText.text = it.movieTitle
                binding.reviewEditText.setText(it.reviewText)
                selectedRating = it.rating
                updateStars(
                    listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5),
                    selectedRating
                )

                val imageUrl = it.imageUrl.ifEmpty { it.moviePosterUrl }
                if (imageUrl.isNotEmpty()) {
                    binding.reviewImage.visibility = View.VISIBLE
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.rounded_card_background)
                        .fit().centerCrop()
                        .into(binding.reviewImage)
                }
            }
        }

        reviewViewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    Toast.makeText(requireContext(), R.string.review_updated, Toast.LENGTH_SHORT).show()
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
            binding.updateReviewButton.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
