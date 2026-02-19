package com.moviemate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moviemate.R
import com.moviemate.data.model.Review
import com.moviemate.databinding.ItemReviewFeedBinding
import com.moviemate.utils.CircleTransform
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewFeedAdapter : ListAdapter<Review, ReviewFeedAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemReviewFeedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewFeedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = getItem(position)
        with(holder.binding) {
            usernameText.text = review.username
            movieTitleText.text = review.movieTitle
            reviewText.text = review.reviewText
            dateText.text = formatDate(review.timestamp)

            setStars(review.rating, star1, star2, star3, star4, star5)

            if (review.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(review.imageUrl)
                    .placeholder(R.drawable.rounded_card_background)
                    .error(R.drawable.rounded_card_background)
                    .fit().centerCrop()
                    .into(reviewImage)
            } else if (review.moviePosterUrl.isNotEmpty()) {
                Picasso.get()
                    .load(review.moviePosterUrl)
                    .placeholder(R.drawable.rounded_card_background)
                    .error(R.drawable.rounded_card_background)
                    .fit().centerCrop()
                    .into(reviewImage)
            }

            if (review.userProfileImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(review.userProfileImageUrl)
                    .placeholder(R.drawable.circle_background)
                    .error(R.drawable.circle_background)
                    .transform(CircleTransform())
                    .into(userProfileImage)
            }
        }
    }

    private fun setStars(rating: Int, vararg stars: ImageView) {
        stars.forEachIndexed { index, star ->
            star.setImageResource(
                if (index < rating) R.drawable.ic_star_filled else R.drawable.ic_star_empty
            )
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    class DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }
}
