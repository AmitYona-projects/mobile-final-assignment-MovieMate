package com.moviemate.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviemate.data.model.User
import com.moviemate.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    val currentUser: LiveData<User?> = repository.getCurrentUserLive()

    private val _updateResult = MutableLiveData<Result<User>?>()
    val updateResult: LiveData<Result<User>?> = _updateResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

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
