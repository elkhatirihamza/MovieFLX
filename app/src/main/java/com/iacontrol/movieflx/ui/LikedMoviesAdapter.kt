package com.iacontrol.movieflx.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.iacontrol.movieflx.data.model.Movie
import com.iacontrol.movieflx.databinding.ItemLikedMovieBinding


class LikedMoviesAdapter :
    ListAdapter<Movie, LikedMoviesAdapter.LikedMovieViewHolder>(MovieDiffCallback()) {

   //instantiation desliked movies items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedMovieViewHolder {
        val binding = ItemLikedMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LikedMovieViewHolder(binding)
    }

    //afficher les donnees de film a l'interieur du view holder via la methode bind
    override fun onBindViewHolder(holder: LikedMovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    //utile pour la suppression et la modification (ne pas alterer tout les elements du recycler view)
    inner class LikedMovieViewHolder(private val binding: ItemLikedMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.movieTitle.text = movie.title
            binding.movieReleaseDate.text = movie.year.toString()
            binding.movieRating.text = movie.rating.toString()

            // Chargement de l'image
            Glide.with(binding.movieLeadingImage.context)
                .load(movie.imageUrl)
                .centerCrop()
                .into(binding.movieLeadingImage)
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem
    }
}