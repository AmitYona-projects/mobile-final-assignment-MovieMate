package com.moviemate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moviemate.R
import com.moviemate.data.model.Review
import com.moviemate.databinding.ItemReviewSimpleBinding
import com.moviemate.utils.CircleTransform
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SimpleReviewAdapter : ListAdapter<Review, SimpleReviewAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemReviewSimpleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.usernameText.text = review.username
            binding.reviewText.text = review.reviewText
            binding.dateText.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(review.timestamp))

            val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
            stars.forEachIndexed { index, star ->
                star.setImageResource(
                    if (index < review.rating) R.drawable.ic_star_filled else R.drawable.ic_star_empty
                )
            }

            if (review.userProfileImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(review.userProfileImageUrl)
                    .placeholder(R.drawable.circle_background)
                    .error(R.drawable.circle_background)
                    .transform(CircleTransform())
                    .into(binding.userProfileImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewSimpleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(old: Review, new: Review) = old.id == new.id
        override fun areContentsTheSame(old: Review, new: Review) = old == new
    }
}
