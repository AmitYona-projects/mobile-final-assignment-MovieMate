package com.moviemate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moviemate.R
import com.moviemate.databinding.FragmentProfileBinding
import com.moviemate.ui.MainActivity
import com.moviemate.ui.adapter.ProfileReviewAdapter
import com.moviemate.ui.viewmodel.ReviewViewModel
import com.moviemate.ui.viewmodel.UserViewModel
import com.moviemate.utils.CircleTransform
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val reviewViewModel: ReviewViewModel by activityViewModels()
    private lateinit var adapter: ProfileReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeData()
        userViewModel.refreshUser()
    }

    private fun setupRecyclerView() {
        adapter = ProfileReviewAdapter(
            onEditClick = { review ->
                val action = ProfileFragmentDirections.actionProfileToEditReview(review.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { review ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_review_title)
                    .setMessage(R.string.delete_review_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        reviewViewModel.deleteReview(review.id)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.reviewsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.reviewsRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.menuButton.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_editProfile)
        }

        binding.createReviewButton.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_createReview)
        }
    }

    private fun observeData() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.usernameText.text = user.username.ifEmpty { "User" }
                binding.emailText.text = user.email

                if (user.profileImageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.circle_background)
                        .error(R.drawable.circle_background)
                        .transform(CircleTransform())
                        .into(binding.profileImage)
                }
            } else {
                // User not in Room yet — trigger a refresh
                userViewModel.refreshUser()
            }
        }

        reviewViewModel.userReviews.observe(viewLifecycleOwner) { reviews ->
            adapter.submitList(reviews)
            val isEmpty = reviews.isNullOrEmpty()
            binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.reviewsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        reviewViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    Toast.makeText(requireContext(), R.string.review_deleted, Toast.LENGTH_SHORT).show()
                    reviewViewModel.fetchAllReviews()
                }
                it.onFailure { e ->
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
                reviewViewModel.clearResults()
            }
        }

        reviewViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
