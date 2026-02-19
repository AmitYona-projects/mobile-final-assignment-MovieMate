package com.moviemate.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.moviemate.data.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserById(uid: String): LiveData<User?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByIdSync(uid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUser(uid: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
