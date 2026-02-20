package com.moviemate.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.moviemate.R
import com.moviemate.databinding.FragmentMovieReviewsBinding
import com.moviemate.ui.adapter.SimpleReviewAdapter
import com.moviemate.ui.viewmodel.ReviewViewModel
import com.squareup.picasso.Picasso

class MovieReviewsFragment : Fragment() {

    private var _binding: FragmentMovieReviewsBinding? = null
    private val binding get() = _binding!!
    private val reviewViewModel: ReviewViewModel by activityViewModels()
    private val args: MovieReviewsFragmentArgs by navArgs()
    private lateinit var adapter: SimpleReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieTitle = args.movieTitle

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.movieTitleToolbar.text = movieTitle

        adapter = SimpleReviewAdapter()
        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRecyclerView.adapter = adapter

        reviewViewModel.groupedMovies.observe(viewLifecycleOwner) { groups ->
            val group = groups.find { it.movieTitle == movieTitle } ?: return@observe

            binding.movieTitleText.text = group.movieTitle
            binding.movieGenresText.text = group.movieGenres
            binding.averageRatingText.text = String.format("%.1f / 5.0", group.averageRating)
            binding.reviewCountText.text = if (group.reviewCount == 1)
                "(1 review)" else "(${group.reviewCount} reviews)"

            if (group.moviePosterUrl.isNotEmpty()) {
                Picasso.get()
                    .load(group.moviePosterUrl)
                    .placeholder(R.drawable.rounded_card_background)
                    .fit().centerCrop()
                    .into(binding.moviePosterImage)
            }

            adapter.submitList(group.reviews)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
