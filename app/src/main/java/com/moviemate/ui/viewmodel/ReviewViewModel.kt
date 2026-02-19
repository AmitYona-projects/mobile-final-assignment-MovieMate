package com.moviemate.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviemate.data.model.Movie
import com.moviemate.data.model.Review
import com.moviemate.data.repository.MovieRepository
import com.moviemate.data.repository.ReviewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private val reviewRepository = ReviewRepository()
    private val movieRepository = MovieRepository()

    val allReviews: LiveData<List<Review>> = reviewRepository.getAllReviewsLocal()
    val userReviews: LiveData<List<Review>> = reviewRepository.getUserReviewsLocal()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _movieSuggestions = MutableLiveData<List<Movie>>()
    val movieSuggestions: LiveData<List<Movie>> = _movieSuggestions

    private val _selectedMovie = MutableLiveData<Movie?>()
    val selectedMovie: LiveData<Movie?> = _selectedMovie

    private val _movieGenresText = MutableLiveData<String>()
    val movieGenresText: LiveData<String> = _movieGenresText

    private val _createResult = MutableLiveData<Result<Review>?>()
    val createResult: LiveData<Result<Review>?> = _createResult

    private val _updateResult = MutableLiveData<Result<Review>?>()
    val updateResult: LiveData<Result<Review>?> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private val _currentReview = MutableLiveData<Review?>()
    val currentReview: LiveData<Review?> = _currentReview

    private var searchJob: Job? = null

    fun fetchAllReviews() {
        _isLoading.value = true
        viewModelScope.launch {
            reviewRepository.fetchAllReviews()
            _isLoading.value = false
        }
    }

    fun searchMovies(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _movieSuggestions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            val result = movieRepository.searchMovies(query)
            result.onSuccess { movies ->
                _movieSuggestions.value = movies.take(8)
            }
        }
    }

    fun selectMovie(movie: Movie) {
        _selectedMovie.value = movie
        viewModelScope.launch {
            val genres = movieRepository.getGenreNames(movie.genreIds)
            _movieGenresText.value = genres
        }
    }

    fun createReview(
        rating: Int,
        reviewText: String,
        imageUri: Uri?
    ) {
        val movie = _selectedMovie.value ?: return
        val genres = _movieGenresText.value ?: ""

        _isLoading.value = true
        viewModelScope.launch {
            val result = reviewRepository.createReview(
                movieTitle = movie.toString(),
                moviePosterUrl = movie.fullPosterUrl,
                movieGenres = genres,
                rating = rating,
                reviewText = reviewText,
                imageUri = imageUri
            )
            _createResult.value = result
            _isLoading.value = false
        }
    }

    fun loadReview(reviewId: String) {
        viewModelScope.launch {
            _currentReview.value = reviewRepository.getReviewById(reviewId)
        }
    }

    fun updateReview(
        reviewId: String,
        rating: Int,
        reviewText: String,
        imageUri: Uri?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = reviewRepository.updateReview(reviewId, rating, reviewText, imageUri)
            _updateResult.value = result
            _isLoading.value = false
        }
    }

    fun deleteReview(reviewId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = reviewRepository.deleteReview(reviewId)
            _deleteResult.value = result
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _createResult.value = null
        _updateResult.value = null
        _deleteResult.value = null
        _selectedMovie.value = null
        _movieGenresText.value = ""
    }
}
