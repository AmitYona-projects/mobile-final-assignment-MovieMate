package com.moviemate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImageUrl: String = "",
    val movieTitle: String = "",
    val moviePosterUrl: String = "",
    val movieGenres: String = "",
    val rating: Int = 0,
    val reviewText: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "username" to username,
        "userProfileImageUrl" to userProfileImageUrl,
        "movieTitle" to movieTitle,
        "moviePosterUrl" to moviePosterUrl,
        "movieGenres" to movieGenres,
        "rating" to rating,
        "reviewText" to reviewText,
        "imageUrl" to imageUrl,
        "timestamp" to timestamp,
        "lastUpdated" to lastUpdated
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Review = Review(
            id = map["id"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            username = map["username"] as? String ?: "",
            userProfileImageUrl = map["userProfileImageUrl"] as? String ?: "",
            movieTitle = map["movieTitle"] as? String ?: "",
            moviePosterUrl = map["moviePosterUrl"] as? String ?: "",
            movieGenres = map["movieGenres"] as? String ?: "",
            rating = (map["rating"] as? Long)?.toInt() ?: 0,
            reviewText = map["reviewText"] as? String ?: "",
            imageUrl = map["imageUrl"] as? String ?: "",
            timestamp = map["timestamp"] as? Long ?: 0L,
            lastUpdated = map["lastUpdated"] as? Long ?: 0L
        )
    }
}
