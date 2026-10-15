package com.iacontrol.movieflx.data.repository

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteException
import android.util.Log
import com.iacontrol.movieflx.data.model.Movie
import com.iacontrol.movieflx.data.local.MovieDao
import com.iacontrol.movieflx.data.recommendation.MovieRecommendationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Référentiel (Repository) qui sert de source unique de vérité pour les données de films.
 * Il abstrait la source de données (Room Database) du reste de l'application.
 */
@Singleton
class MovieRepository @Inject constructor(
    private val movieDao: MovieDao,
    private val recommendationEngine: MovieRecommendationEngine
) {

    /**
     * Insère ou met à jour un film dans la base de données.
     * Utilisé pour marquer des films comme favoris ou ajouter de nouvelles données.
     */
    suspend fun insertMovie(movie: Movie) {
        movieDao.insertMovie(movie)
    }

    fun getMovieStatus(movieId : Int): Flow<Boolean> {
        return movieDao.getMovieState(movieId)
    }


    /**
     * Met à jour le statut 'isFavorite' d'un film spécifique par son ID.
     */
    @SuppressLint("RestrictedApi")
    suspend fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean) {

        val data = movieDao.getFavoriteMovies().first()

        if (data.size == 5 && isFavorite) {
            throw SQLiteException("Impossible d'ajouter plus de 5 films!")
        }

        movieDao.updateFavoriteStatus(movieId, isFavorite)
        Log.d("test", "update OK")
    }


    /**
     * Récupère la liste des films favoris en appliquant les filtres et le tri conditionnel.
     * Cette fonction utilise la logique SQL que nous avons définie (Tri par rating DESC si bestRated=true).
     *
     * @param searchQuery Terme de recherche pour le titre.
     * @param year Année de sortie.
     * @param bestRated Si TRUE, trie par note (rating) DESC. Sinon, utilise un tri neutre.
     * @return Flow<List<Movie>> Mise à jour en temps réel.
     */
    suspend fun filterMovies(
        searchQuery: String?,
        genre: String?,
        minRating: Float?,
        yearInterval: Pair<Int, Int>,
        sortType: String,
        limit: Int,
        offset: Int
    ): List<Movie> {
        return movieDao.filterMovies(
            searchQuery, genre, minRating,
            yearInterval.first, yearInterval.second,
            sortType, limit, offset
        )
    }

    /**
     * Récupère tous les films de la base de données sans filtre.
     */
    suspend fun getAllMoviesAsList(): List<Movie> {
        return movieDao.getAllMoviesAsList()
    }

    fun getAllMovies(): Flow<List<Movie>> {
        return movieDao.getAllMovies()
    }

    fun getAllCategories() : Flow<List<String>>{
        return movieDao.getAllCategories()
    }

    fun getFavoriteMovies() : Flow<List<Movie>> {
        return movieDao.getFavoriteMovies()
    }

    /**
     * Récupère le nombre de films favoris
     */
    suspend fun getFavoriteCount(): Int {
        return movieDao.getFavoriteMovies().first().size
    }

    /**
     * Génère des recommandations personnalisées basées sur les 5 films favoris
     * 
     * @return Liste de 5 films recommandés, ou null si pas exactement 5 favoris
     */
    suspend fun getPersonalizedRecommendations(): List<Movie>? {
        val favorites = movieDao.getFavoriteMovies().first()
        
        // Vérifier qu'il y a exactement 5 favoris
        if (favorites.size != 5) {
            return null
        }
        
        // Récupérer tous les films pour la recherche
        val allMovies = getAllMoviesAsList()
        
        // Générer les recommandations
        return recommendationEngine.getRecommendations(favorites, allMovies)
    }

    /**
     * Vérifie si l'utilisateur a exactement 5 films favoris
     */
    suspend fun hasRequiredFavorites(): Boolean {
        val count = getFavoriteCount()
        return recommendationEngine.hasRequiredFavorites(count)
    }
}