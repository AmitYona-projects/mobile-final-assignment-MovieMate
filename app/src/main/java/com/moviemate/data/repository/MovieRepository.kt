package com.moviemate.data.repository

import com.moviemate.data.model.Movie
import com.moviemate.data.remote.RetrofitClient
import com.moviemate.data.remote.model.TmdbGenre

class MovieRepository {

    private val api = RetrofitClient.tmdbApi
    private var genresCache: Map<Int, String> = emptyMap()

    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            val response = api.searchMovies(query)
            val movies = response.results.map { it.toMovie() }
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGenreNames(genreIds: List<Int>): String {
        if (genresCache.isEmpty()) {
            loadGenres()
        }
        return genreIds.mapNotNull { genresCache[it] }.joinToString(", ")
    }

    private suspend fun loadGenres() {
        try {
            val response = api.getGenres()
            genresCache = response.genres.associate { it.id to it.name }
        } catch (_: Exception) { }
    }
}
