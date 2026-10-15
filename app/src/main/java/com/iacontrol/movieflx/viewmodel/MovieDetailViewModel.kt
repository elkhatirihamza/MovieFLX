package com.iacontrol.movieflx.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iacontrol.movieflx.data.model.Movie
import com.iacontrol.movieflx.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Interface pour encapsuler les événements UI (Toasts/Snackbars)
sealed interface DetailUiEvent {
    data class ShowSnackbar(val message: String) : DetailUiEvent
    data object None : DetailUiEvent
}

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    // État du film pour l'UI, initialisé à null ou avec un objet Movie par défaut
    private val _movieData = MutableStateFlow<Movie?>(null)

    // État pour savoir si le film est favori
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // État pour les événements ponctuels comme les Toasts/Snackbars
    private val _uiEvent = MutableStateFlow<DetailUiEvent>(DetailUiEvent.None)
    val uiEvent: StateFlow<DetailUiEvent> = _uiEvent.asStateFlow()

    private var currentMovieId: Int? = null

    /**
     * Doit être appelé par le Fragment pour initialiser le ViewModel avec les données reçues.
     */
    fun initialize(movie: Movie) {
        _movieData.value = movie
        currentMovieId = movie.id // <-- Stocker l'ID

        // Lancement de l'observation de l'état du film dans la base de données
        fetchFavoriteStatus(movie.id)
        }



    private fun fetchFavoriteStatus(movieId: Int?) {
            if (movieId == null) return

            viewModelScope.launch {
                repository.getMovieStatus(movieId) // <-- Appel au nouveau DAO
                    .collect { isFavoriteDb ->
                        // Met à jour l'état local du ViewModel (et donc l'icône via le Fragment)
                        _isFavorite.value = isFavoriteDb
                    }
            }
        }

    /**
     * Bascule le statut de favori du film.
     */
    fun toggleFavoriteStatus() {

        val currentMovie = _movieData.value ?: return

        print(currentMovie)
        // La nouvelle valeur de favori sera l'inverse de l'actuelle
        val newFavoriteStatus = !_isFavorite.value
        val movieId = currentMovie.id

        viewModelScope.launch {

            try {
                // 1. Appel au Repository pour mettre à jour la base de données
                if (movieId == null) return@launch
                repository.updateFavoriteStatus(movieId, newFavoriteStatus)

                // 2. Mise à jour de l'état local du ViewModel (le bouton)
                _isFavorite.value = newFavoriteStatus

                // 3. Envoyer un événement de succès à l'UI
                val message = if (newFavoriteStatus)
                    "${currentMovie.title} ajouté aux favoris."
                else
                    "${currentMovie.title} retiré des favoris."

                sendUiEvent(DetailUiEvent.ShowSnackbar(message))

            } catch (e: Exception) {
                // 4. En cas d'erreur (ex: limite de 5 films atteinte dans le Repository)
                val errorMessage = e.localizedMessage ?: "Erreur inconnue lors de la mise à jour des favoris."
                sendUiEvent(DetailUiEvent.ShowSnackbar(errorMessage))
            }
        }
    }

    /**
     * Réinitialise l'événement UI après qu'il a été consommé par le Fragment.
     */
    fun onUiEventConsumed() {
        _uiEvent.value = DetailUiEvent.None
    }

    private fun sendUiEvent(event: DetailUiEvent) {
        _uiEvent.value = event
    }
}