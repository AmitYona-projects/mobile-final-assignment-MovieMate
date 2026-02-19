package com.moviemate.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.moviemate.MovieMateApplication
import com.moviemate.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userDao = MovieMateApplication.instance.database.userDao()

    fun getCurrentUserLive(): LiveData<User?> {
        val uid = auth.currentUser?.uid ?: ""
        return userDao.getUserById(uid)
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return userDao.getUserByIdSync(uid)
            ?: fetchAndCacheUser(uid)
    }

    private suspend fun fetchAndCacheUser(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = User.fromMap(doc.data ?: emptyMap())
                userDao.insertUser(user)
                user
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(username: String, imageUri: Uri?): Result<User> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        return try {
            var imageUrl = ""

            if (imageUri != null) {
                val ref = storage.reference.child("profile_images/$uid.jpg")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            val currentUser = getCurrentUser()
            val updates = mutableMapOf<String, Any>("username" to username)
            if (imageUrl.isNotEmpty()) {
                updates["profileImageUrl"] = imageUrl
            }

            firestore.collection("users").document(uid).update(updates).await()

            val updatedUser = User(
                uid = uid,
                username = username,
                email = currentUser?.email ?: auth.currentUser?.email ?: "",
                profileImageUrl = if (imageUrl.isNotEmpty()) imageUrl else (currentUser?.profileImageUrl ?: "")
            )
            userDao.insertUser(updatedUser)

            updateUserReviews(uid, username, updatedUser.profileImageUrl)

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateUserReviews(uid: String, username: String, profileImageUrl: String) {
        try {
            val reviews = firestore.collection("reviews")
                .whereEqualTo("userId", uid)
                .get().await()

            for (doc in reviews.documents) {
                doc.reference.update(
                    mapOf(
                        "username" to username,
                        "userProfileImageUrl" to profileImageUrl
                    )
                )
            }
        } catch (_: Exception) { }
    }
}
