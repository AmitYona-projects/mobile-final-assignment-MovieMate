package com.moviemate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "username" to username,
        "email" to email,
        "profileImageUrl" to profileImageUrl
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            uid = map["uid"] as? String ?: "",
            username = map["username"] as? String ?: "",
            email = map["email"] as? String ?: "",
            profileImageUrl = map["profileImageUrl"] as? String ?: ""
        )
    }
}
