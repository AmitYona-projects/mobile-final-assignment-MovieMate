package com.moviemate.data.model

data class Movie(
    val id: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val releaseDate: String = "",
    val genreIds: List<Int> = emptyList(),
    val overview: String = ""
) {
    val fullPosterUrl: String
        get() = if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else ""

    val year: String
        get() = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else ""

    override fun toString(): String = if (year.isNotEmpty()) "$title ($year)" else title
}
