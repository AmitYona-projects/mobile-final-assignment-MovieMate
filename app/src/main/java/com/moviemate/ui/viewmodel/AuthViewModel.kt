package com.moviemate.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.moviemate.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<Result<FirebaseUser>?>()
    val loginResult: LiveData<Result<FirebaseUser>?> = _loginResult

    private val _registerResult = MutableLiveData<Result<FirebaseUser>?>()
    val registerResult: LiveData<Result<FirebaseUser>?> = _registerResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val isLoggedIn: Boolean
        get() = repository.isLoggedIn

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(email, password)
            _loginResult.value = result
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String, username: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(email, password, username)
            _registerResult.value = result
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearResults() {
        _loginResult.value = null
        _registerResult.value = null
    }
}
