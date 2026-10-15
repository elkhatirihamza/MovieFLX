package com.iacontrol.movieflx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.iacontrol.movieflx.data.model.Movie
import com.iacontrol.movieflx.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface LikedMoviesUiState {
    //etats: chargement, succes, echec
    data object Loading : LikedMoviesUiState
    data class Success(val movies: List<Movie>) : LikedMoviesUiState
    data class Error(val message: String) : LikedMoviesUiState
}


@HiltViewModel
class LikedMoviesViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    // Utilisation de StateFlow pour les données d'état de l'UI
    private val _uiState = MutableStateFlow<LikedMoviesUiState>(LikedMoviesUiState.Loading)
    val uiState: StateFlow<LikedMoviesUiState> = _uiState

    init {
        //recuperer les films favoris au chargement initial du view model
        fetchLikedMovies()
    }

    private fun fetchLikedMovies() {
        viewModelScope.launch {
            repository.getFavoriteMovies()
                .map { movies -> LikedMoviesUiState.Success(movies) } // Mappe le résultat à l'état Success
                .catch { exception -> // Gère les erreurs
                    _uiState.value = LikedMoviesUiState.Error(
                        exception.localizedMessage ?: "Une erreur inconnue est survenue."
                    )
                }
                .collectLatest { state -> // Émet le nouvel état
                    _uiState.value = state
                }
        }
    }

    /**
     * Tente de supprimer un film de la base de données (exemple d'action)
     */
    fun removeMovie(movieId: Int) {
        viewModelScope.launch {
            try {
                // Mettre à jour le statut isFavorite à false
                repository.updateFavoriteStatus(movieId, isFavorite = false)

            } catch (e: Exception) {
                _uiState.value = LikedMoviesUiState.Error(
                    "Échec de la suppression: ${e.localizedMessage ?: "Erreur"}"
                )
            }
        }
    }
}