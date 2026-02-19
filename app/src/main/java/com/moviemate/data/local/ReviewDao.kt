package com.moviemate.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.moviemate.data.model.Review

@Dao
interface ReviewDao {

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getReviewsByUser(userId: String): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: String): Review?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: String)

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()
}
