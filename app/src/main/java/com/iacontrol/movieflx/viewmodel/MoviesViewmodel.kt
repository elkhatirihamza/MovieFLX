package com.iacontrol.movieflx.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.iacontrol.movieflx.data.model.Movie
import com.iacontrol.movieflx.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class pour l'état des filtres
data class MovieFilters(
    val searchQuery: String = "",
    val genre: String = "Tous",
    val minRating: Float = 0.0f,
    val yearInterval: Pair<Int, Int> = Pair(1950, 2025),
    val sortType: String = "" // "rating", "recent"
)

// Sealed class pour l'état des recommandations
sealed class RecommendationState {
    object Loading : RecommendationState()
    data class Success(val movies: List<Movie>) : RecommendationState()
    object NeedMoreFavorites : RecommendationState() // Exactement 5 favoris requis
    data class Error(val message: String) : RecommendationState()
}

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MovieRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "MovieFlxPrefs"
        private const val KEY_RECOMMENDATION_IDS = "recommendation_ids"
    }

    private val PAGE_SIZE = 30

    // 1. État des filtres
    private val _filters = MutableStateFlow(MovieFilters())
    // Exposé pour le BottomSheet
    val currentFilters = _filters.asLiveData()

    //liste des films
    private val _movieListState = MutableStateFlow<List<Movie>>(emptyList())
    val displayedMovies = _movieListState.asLiveData()

    // État des recommandations "Pour Vous"
    private val _recommendationState = MutableLiveData<RecommendationState>()
    val recommendationState: LiveData<RecommendationState> = _recommendationState

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    init {
        // Charger les recommandations sauvegardées au démarrage
        loadSavedRecommendations()
        
        // Observer les changements de filtres pour recharger la liste depuis zéro
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _filters
                .debounce(300L)
                .collectLatest { filters ->
                    resetPaginationAndLoad(filters)
                }
        }
    }

    /**
     * Charge les recommandations personnalisées uniquement si exactement 5 films favoris
     * Ne change PAS l'état si pas exactement 5 (garde les recommandations sauvegardées)
     */
    fun loadRecommendations() {
        viewModelScope.launch {
            try {
                android.util.Log.d("MoviesViewModel", "Loading recommendations...")
                
                val favCount = repository.getFavoriteCount()
                android.util.Log.d("MoviesViewModel", "Favorite count: $favCount")
                
                // Si exactement 5 favoris, générer de nouvelles recommandations
                if (favCount == 5) {
                    _recommendationState.value = RecommendationState.Loading
                    
                    val recommendations = repository.getPersonalizedRecommendations()
                    android.util.Log.d("MoviesViewModel", "Recommendations received: ${recommendations?.size ?: 0}")
                    
                    if (recommendations != null && recommendations.isNotEmpty()) {
                        // Sauvegarder les IDs dans SharedPreferences
                        saveRecommendationIds(recommendations.mapNotNull { it.id })
                        android.util.Log.d("MoviesViewModel", "Setting Success state with ${recommendations.size} movies")
                        _recommendationState.value = RecommendationState.Success(recommendations)
                    } else {
                        _recommendationState.value = RecommendationState.Error("Aucune recommandation disponible")
                    }
                } else {
                    // Si pas exactement 5, NE PAS changer l'état
                    // Les recommandations sauvegardées restent affichées
                    android.util.Log.d("MoviesViewModel", "Not exactly 5 favorites ($favCount), keeping current state")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MoviesViewModel", "Error loading recommendations", e)
            }
        }
    }

    /**
     * Réinitialise la pagination et charge la première page.
     * Appelé automatiquement quand _filters change (Recherche, Genre, Tri...).
     */
    private fun resetPaginationAndLoad(filters: MovieFilters) {
        currentPage = 0
        isLastPage = false
        _movieListState.value = emptyList() // Optionnel : vide la liste pour montrer un loading clair
        loadMovies(filters)
    }

    /**
     * Appelé par le Fragment via le ScrollListener quand on arrive en bas.
     */
    fun loadNextPage() {
        // Garde-fou : ne pas charger si déjà en cours ou si tout est chargé
        if (isLoading || isLastPage) return

        loadMovies(_filters.value)
    }

    /**
     * Logique centrale de chargement des données.
     */
    private fun loadMovies(filters: MovieFilters) {
        isLoading = true
        viewModelScope.launch {
            try {
                val offset = currentPage * PAGE_SIZE

                // Appel au Repository (Note: filterMovies doit être une suspend function retournant List<Movie>)
                val newMovies = repository.filterMovies(
                    searchQuery = filters.searchQuery,
                    genre = filters.genre,
                    minRating = filters.minRating,
                    yearInterval = filters.yearInterval,
                    sortType = filters.sortType,
                    limit = PAGE_SIZE,
                    offset = offset
                )

                // Vérification si on a atteint la fin des données
                if (newMovies.size < PAGE_SIZE) {
                    isLastPage = true
                }

                // Accumulation : Ancienne liste + Nouvelle page
                val currentList = _movieListState.value.toMutableList()
                currentList.addAll(newMovies)

                // Mise à jour de l'UI
                _movieListState.value = currentList

                // Préparer la page suivante
                currentPage++

            } catch (e: Exception) {
                e.printStackTrace()
                // Ici, tu pourrais mettre à jour un LiveData d'erreur pour afficher un Toast
            } finally {
                isLoading = false
            }
        }
    }


    fun setSearchQuery(query: String) {
        // Mise à jour seulement si différent pour éviter les boucles
        if (_filters.value.searchQuery != query) {
            _filters.value = _filters.value.copy(searchQuery = query)
        }
    }

    fun applyFilters(
        genre: String,
        sortType: String,
        minRating: Float,
        yearInterval: Pair<Int, Int>
    ) {
        _filters.value = _filters.value.copy(
            genre = genre,
            sortType = sortType,
            minRating = minRating,
            yearInterval = yearInterval
        )

    }
    
    /**
     * Charge les recommandations sauvegardées depuis SharedPreferences au démarrage
     */
    private fun loadSavedRecommendations() {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedIds = prefs.getStringSet(KEY_RECOMMENDATION_IDS, null)
            
            android.util.Log.d("MoviesViewModel", "Saved recommendation IDs: $savedIds")
            
            if (savedIds != null && savedIds.isNotEmpty()) {
                // Récupérer les films depuis la base de données par leurs IDs
                try {
                    _recommendationState.value = RecommendationState.Loading
                    val allMovies = repository.getAllMoviesAsList()
                    val recommendedMovies = savedIds.mapNotNull { id ->
                        allMovies.find { it.id == id.toIntOrNull() }
                    }
                    
                    if (recommendedMovies.isNotEmpty()) {
                        android.util.Log.d("MoviesViewModel", "Loaded ${recommendedMovies.size} saved recommendations")
                        _recommendationState.value = RecommendationState.Success(recommendedMovies)
                    } else {
                        _recommendationState.value = RecommendationState.NeedMoreFavorites
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MoviesViewModel", "Error loading saved recommendations", e)
                    _recommendationState.value = RecommendationState.NeedMoreFavorites
                }
            } else {
                // Première fois, aucune recommandation sauvegardée
                android.util.Log.d("MoviesViewModel", "No saved recommendations found")
                _recommendationState.value = RecommendationState.NeedMoreFavorites
            }
        }
    }
    
    /**
     * Sauvegarde les IDs des recommandations dans SharedPreferences
     */
    private fun saveRecommendationIds(ids: List<Int>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_RECOMMENDATION_IDS, ids.map { it.toString() }.toSet()).apply()
        android.util.Log.d("MoviesViewModel", "Saved recommendation IDs: $ids")
    }
}