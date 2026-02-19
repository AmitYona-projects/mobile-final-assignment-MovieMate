package com.moviemate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.moviemate.MovieMateApplication
import com.moviemate.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userDao = MovieMateApplication.instance.database.userDao()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                fetchAndCacheUser(firebaseUser.uid)
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    username = username,
                    email = email,
                    profileImageUrl = ""
                )
                firestore.collection("users").document(firebaseUser.uid)
                    .set(user.toMap()).await()
                userDao.insertUser(user)
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAndCacheUser(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = User.fromMap(doc.data ?: emptyMap())
                userDao.insertUser(user)
                user
            } else null
        } catch (e: Exception) {
            userDao.getUserByIdSync(uid)
        }
    }

    suspend fun logout() {
        userDao.deleteAllUsers()
        MovieMateApplication.instance.database.reviewDao().deleteAllReviews()
        auth.signOut()
    }
}
