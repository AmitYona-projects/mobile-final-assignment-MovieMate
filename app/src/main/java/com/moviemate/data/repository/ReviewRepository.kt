package com.moviemate.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.moviemate.MovieMateApplication
import com.moviemate.data.model.Review
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReviewRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val reviewDao = MovieMateApplication.instance.database.reviewDao()
    private val userDao = MovieMateApplication.instance.database.userDao()

    fun getAllReviewsLocal(): LiveData<List<Review>> = reviewDao.getAllReviews()

    fun getUserReviewsLocal(): LiveData<List<Review>> {
        val uid = auth.currentUser?.uid ?: ""
        return reviewDao.getReviewsByUser(uid)
    }

    suspend fun fetchAllReviews(): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { Review.fromMap(it) }
            }
            reviewDao.deleteAllReviews()
            reviewDao.insertReviews(reviews)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewById(reviewId: String): Review? {
        return reviewDao.getReviewById(reviewId) ?: fetchReviewFromFirestore(reviewId)
    }

    private suspend fun fetchReviewFromFirestore(reviewId: String): Review? {
        return try {
            val doc = firestore.collection("reviews").document(reviewId).get().await()
            if (doc.exists()) {
                val review = Review.fromMap(doc.data ?: emptyMap())
                reviewDao.insertReview(review)
                review
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createReview(
        movieTitle: String,
        moviePosterUrl: String,
        movieGenres: String,
        rating: Int,
        reviewText: String,
        imageUri: Uri?
    ): Result<Review> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        return try {
            val user = userDao.getUserByIdSync(uid)
            val reviewId = UUID.randomUUID().toString()

            var imageUrl = ""
            if (imageUri != null) {
                val ref = storage.reference.child("review_images/$reviewId.jpg")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            val review = Review(
                id = reviewId,
                userId = uid,
                username = user?.username ?: "Anonymous",
                userProfileImageUrl = user?.profileImageUrl ?: "",
                movieTitle = movieTitle,
                moviePosterUrl = moviePosterUrl,
                movieGenres = movieGenres,
                rating = rating,
                reviewText = reviewText,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            firestore.collection("reviews").document(reviewId)
                .set(review.toMap()).await()
            reviewDao.insertReview(review)

            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReview(
        reviewId: String,
        rating: Int,
        reviewText: String,
        imageUri: Uri?
    ): Result<Review> {
        return try {
            val existingReview = getReviewById(reviewId)
                ?: return Result.failure(Exception("Review not found"))

            var imageUrl = existingReview.imageUrl
            if (imageUri != null) {
                val ref = storage.reference.child("review_images/$reviewId.jpg")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            val updatedReview = existingReview.copy(
                rating = rating,
                reviewText = reviewText,
                imageUrl = imageUrl,
                lastUpdated = System.currentTimeMillis()
            )

            firestore.collection("reviews").document(reviewId)
                .set(updatedReview.toMap()).await()
            reviewDao.insertReview(updatedReview)

            Result.success(updatedReview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            firestore.collection("reviews").document(reviewId).delete().await()
            reviewDao.deleteReviewById(reviewId)

            try {
                storage.reference.child("review_images/$reviewId.jpg").delete().await()
            } catch (_: Exception) { }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
