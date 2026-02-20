package com.moviemate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.moviemate.MovieMateApplication
import com.moviemate.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

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
            val result = withTimeout(15_000) {
                auth.signInWithEmailAndPassword(email, password).await()
            }
            result.user?.let { firebaseUser ->
                try { fetchAndCacheUser(firebaseUser.uid) } catch (_: Exception) { }
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val result = withTimeout(15_000) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }
            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    username = username,
                    email = email,
                    profileImageUrl = ""
                )
                userDao.insertUser(user)
                // Save to Firestore without blocking registration success
                try {
                    withTimeout(10_000) {
                        firestore.collection("users").document(firebaseUser.uid)
                            .set(user.toMap()).await()
                    }
                } catch (_: Exception) { }
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
