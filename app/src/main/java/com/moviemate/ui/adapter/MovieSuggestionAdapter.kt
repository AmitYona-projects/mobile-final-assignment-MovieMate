package com.moviemate.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.moviemate.R
import com.moviemate.data.model.Movie
import com.squareup.picasso.Picasso

class MovieSuggestionAdapter(
    context: Context,
    private var movies: List<Movie> = emptyList()
) : ArrayAdapter<Movie>(context, R.layout.item_movie_suggestion, movies) {

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }

    override fun getCount(): Int = movies.size

    override fun getItem(position: Int): Movie = movies[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_movie_suggestion, parent, false)

        val movie = movies[position]
        val titleView = view.findViewById<TextView>(R.id.movieTitle)
        val yearView = view.findViewById<TextView>(R.id.movieYear)
        val posterView = view.findViewById<ImageView>(R.id.moviePoster)

        titleView.text = movie.title
        yearView.text = movie.year

        if (movie.fullPosterUrl.isNotEmpty()) {
            Picasso.get()
                .load(movie.fullPosterUrl)
                .placeholder(R.drawable.rounded_card_background)
                .fit().centerCrop()
                .into(posterView)
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?) = FilterResults().apply {
                values = movies
                count = movies.size
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? Movie)?.toString() ?: ""
            }
        }
    }
}
