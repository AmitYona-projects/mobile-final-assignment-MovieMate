package com.moviemate.data.remote

import com.moviemate.data.remote.model.TmdbGenreResponse
import com.moviemate.data.remote.model.TmdbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbSearchResponse

    @GET("genre/movie/list")
    suspend fun getGenres(): TmdbGenreResponse
}
