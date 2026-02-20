package com.moviemate.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.moviemate.data.model.User
import com.moviemate.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    // Reactive UID — updated after login/register so LiveData re-queries Room with the correct UID
    private val _currentUserId = MutableLiveData(auth.currentUser?.uid ?: "")

    val currentUser: LiveData<User?> = Transformations.switchMap(_currentUserId) { uid ->
        if (uid.isEmpty()) MutableLiveData(null)
        else repository.getCurrentUserLive(uid)
    }

    private val _updateResult = MutableLiveData<Result<User>?>()
    val updateResult: LiveData<Result<User>?> = _updateResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /** Call this after login or registration so currentUser LiveData points to the right UID */
    fun notifyUserLoggedIn() {
        _currentUserId.value = auth.currentUser?.uid ?: ""
        refreshUser()
    }

    fun refreshUser() {
        viewModelScope.launch {
            repository.getCurrentUser()
        }
    }

    fun updateProfile(username: String, imageUri: Uri?) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateProfile(username, imageUri)
            _updateResult.value = result
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _updateResult.value = null
    }
}
