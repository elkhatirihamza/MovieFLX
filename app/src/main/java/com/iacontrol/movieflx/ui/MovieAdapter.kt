package com.iacontrol.movieflx.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iacontrol.movieflx.data.model.Movie
import com.iacontrol.movieflx.databinding.ComponentMovieTileBinding

// 1. AJOUT DU PARAMÈTRE isHorizontal DANS LE CONSTRUCTEUR
class MovieAdapter(
    private val isHorizontal: Boolean = false, // Par défaut false (Mode Grille verticale)
    private val onClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ComponentMovieTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val layoutParams = binding.root.layoutParams

        if (isHorizontal) {
            // Configuration pour la liste "Pour Vous" (Large et moins haut)
            layoutParams.width = dpToPx(280, parent.context)
            layoutParams.height = dpToPx(160, parent.context)
        } else {
            // Configuration pour la Grille (Haut type "Poster")
            layoutParams.height = dpToPx(220, parent.context)
            // La largeur reste gérée par le GridManager/XML
        }
        binding.root.layoutParams = layoutParams

        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie)
    }

    inner class MovieViewHolder(private val binding: ComponentMovieTileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.movieTitle.text = movie.title

            // Chargement image avec Glide
            Glide.with(binding.root)
                .load(movie.imageUrl)
                .centerCrop()
                .into(binding.movieImage)

            binding.root.setOnClickListener { onClick(movie) }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}