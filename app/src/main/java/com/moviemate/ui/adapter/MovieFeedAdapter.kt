package com.moviemate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moviemate.R
import com.moviemate.data.model.MovieGroup
import com.moviemate.databinding.ItemMovieFeedBinding
import com.squareup.picasso.Picasso
import android.view.animation.DecelerateInterpolator

class MovieFeedAdapter(
    private val onMovieClick: (MovieGroup) -> Unit
) : ListAdapter<MovieGroup, MovieFeedAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMovieFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: MovieGroup) {
            binding.movieTitleText.text = group.movieTitle
            binding.movieGenresText.text = group.movieGenres
            binding.averageRatingText.text = String.format("%.1f", group.averageRating)
            binding.reviewCountText.text = if (group.reviewCount == 1)
                "1 review" else "${group.reviewCount} reviews"

            if (group.moviePosterUrl.isNotEmpty()) {
                Picasso.get()
                    .load(group.moviePosterUrl)
                    .placeholder(R.drawable.rounded_card_background)
                    .fit().centerCrop()
                    .into(binding.moviePosterImage)
            }

            binding.root.setOnClickListener {
                // Subtle scale-down then scale-up before navigating
                it.animate()
                    .scaleX(0.97f).scaleY(0.97f)
                    .setDuration(100)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        it.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .withEndAction { onMovieClick(group) }
                            .start()
                    }.start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMovieFeedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MovieGroup>() {
        override fun areItemsTheSame(old: MovieGroup, new: MovieGroup) =
            old.movieTitle == new.movieTitle

        override fun areContentsTheSame(old: MovieGroup, new: MovieGroup) =
            old == new
    }
}
