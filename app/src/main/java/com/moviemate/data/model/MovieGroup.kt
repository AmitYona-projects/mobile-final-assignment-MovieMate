package com.moviemate.data.model

data class MovieGroup(
    val movieTitle: String,
    val moviePosterUrl: String,
    val movieGenres: String,
    val averageRating: Float,
    val reviewCount: Int,
    val latestTimestamp: Long,
    val reviews: List<Review>
)
