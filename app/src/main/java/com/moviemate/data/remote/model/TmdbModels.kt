package com.moviemate.data.remote.model

import com.google.gson.annotations.SerializedName
import com.moviemate.data.model.Movie

data class TmdbSearchResponse(
    val page: Int,
    val results: List<TmdbMovie>,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

data class TmdbMovie(
    val id: Int,
    val title: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("genre_ids")
    val genreIds: List<Int>?,
    val overview: String?
) {
    fun toMovie(): Movie = Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        releaseDate = releaseDate ?: "",
        genreIds = genreIds ?: emptyList(),
        overview = overview ?: ""
    )
}

data class TmdbGenreResponse(
    val genres: List<TmdbGenre>
)

data class TmdbGenre(
    val id: Int,
    val name: String
)
