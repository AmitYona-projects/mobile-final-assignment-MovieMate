package com.moviemate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moviemate.R
import com.moviemate.data.model.Review
import com.moviemate.databinding.ItemReviewProfileBinding
import com.squareup.picasso.Picasso

class ProfileReviewAdapter(
    private val onEditClick: (Review) -> Unit,
    private val onDeleteClick: (Review) -> Unit
) : ListAdapter<Review, ProfileReviewAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemReviewProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewProfileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = getItem(position)
        with(holder.binding) {
            movieTitleText.text = review.movieTitle

            setStars(review.rating, star1, star2, star3, star4, star5)

            val imageUrl = review.imageUrl.ifEmpty { review.moviePosterUrl }
            if (imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.rounded_card_background)
                    .error(R.drawable.rounded_card_background)
                    .fit().centerCrop()
                    .into(reviewImage)
            }

            editButton.setOnClickListener { onEditClick(review) }
            deleteButton.setOnClickListener { onDeleteClick(review) }
        }
    }

    private fun setStars(rating: Int, vararg stars: ImageView) {
        stars.forEachIndexed { index, star ->
            star.setImageResource(
                if (index < rating) R.drawable.ic_star_filled else R.drawable.ic_star_empty
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }
}
