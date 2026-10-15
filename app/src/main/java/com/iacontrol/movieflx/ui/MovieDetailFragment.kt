package com.iacontrol.movieflx.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

import com.iacontrol.movieflx.data.model.Movie

import com.iacontrol.movieflx.R
import com.iacontrol.movieflx.databinding.FragmentMovieDetailBinding
import com.iacontrol.movieflx.viewmodel.DetailUiEvent
import com.iacontrol.movieflx.viewmodel.MovieDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {

    private val args: MovieDetailFragmentArgs by navArgs()
    private val viewModel: MovieDetailViewModel by viewModels()

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieDetailBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Retourne à la page précédente
        }
        val movie = args.movieData

        // 1. Initialiser le ViewModel avec les données du film
        viewModel.initialize(movie)

        populateMovieDetails(movie)

        // 3. Observer l'état de favori pour l'icône
        observeFavoriteStatus()

        // 4. Observer les événements UI (messages d'erreur/succès)
        observeUiEvents()

        // 5. Configuration du listener pour le bouton Like
        binding.btnLike.setOnClickListener {
            viewModel.toggleFavoriteStatus()
        }
    }

    private fun populateMovieDetails(movie: Movie) {
        binding.textDescriptionContent.text = movie.description
        binding.textRating.text = movie.rating.toString()
        binding.textReleaseDate.text = movie.year.toString()
        binding.textGenres.text = movie.mainGenre

        val hours = movie.duration?.div(60)
        val minutes = movie.duration?.rem(60)
        binding.textDuration.text = "${hours}h ${minutes}min"

        Glide.with(this)
            .load(movie.imageUrl)
            .centerCrop()
            .into(binding.movieImage)
    }

    private fun observeFavoriteStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFavorite.collect { isFavorite ->
                val iconRes = if (isFavorite) {
                    R.drawable.ic_thumb_up
                } else {
                    R.drawable.ic_thumb_up_off
                }
                binding.btnLike.setImageResource(iconRes)
            }
        }
    }

    private fun observeUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is DetailUiEvent.ShowSnackbar -> {
                        // Afficher le message d'erreur ou de succès
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
                        // Consommer l'événement pour qu'il ne soit pas rejoué
                        viewModel.onUiEventConsumed()
                    }
                    DetailUiEvent.None -> { /* Rien à faire */ }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // Nouvelle méthode pour régler l'icône au chargement initial
    private fun setInitialFavoriteIcon(isFavorite: Boolean) {
        val iconRes = if (isFavorite) {
            R.drawable.ic_thumb_up
        } else {
            R.drawable.ic_thumb_up_off
        }
        binding.btnLike.setImageResource(iconRes)
    }
}