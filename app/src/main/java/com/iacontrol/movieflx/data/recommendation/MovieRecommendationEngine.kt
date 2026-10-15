package com.iacontrol.movieflx.data.recommendation

import android.content.Context
import com.iacontrol.movieflx.data.model.Movie
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Moteur de recommandation de films basé sur la similarité cosinus
 *
 * Utilise un fichier JSON contenant pour chaque film les 30 films les plus similaires.
 * Input: 5 films favoris (IDs)
 * Output: 5 films recommandés (1 par favori)
 * 
 * @param context Context Android pour accès au JSON dans assets
 */
@Singleton
class MovieRecommendationEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Cache du modèle JSON chargé en mémoire
    private var similarityModel: JSONObject? = null

    /**
     * Charge le modèle de similarité depuis assets/databases/model.json
     */
    private fun loadModel(): JSONObject {
        if (similarityModel == null) {
            val jsonString = context.assets.open("databases/model.json")
                .bufferedReader()
                .use { it.readText() }
            similarityModel = JSONObject(jsonString)
        }
        return similarityModel!!
    }

    /**
     * Génère des recommandations basées sur les films favoris selectionnés
     * 
     * Requiert exactement 5 films favoris pour fonctionner.
     * Pour chaque film favori, trouve le premier film similaire non-favori.
     * 
     * @param favoriteMovies Liste de 5 films favoris
     * @param allMovies Pool complet de films (pour récupérer les objets Movie)
     * @return Liste de 5 films recommandés (ou moins si pas assez de similarités)
     */
    suspend fun getRecommendations(
        favoriteMovies: List<Movie>,
        allMovies: List<Movie>
    ): List<Movie> = withContext(Dispatchers.Default) {
        
        // Validation: besoin d'exactement 5 films favoris
        if (favoriteMovies.size != 5) {
            return@withContext emptyList()
        }

        try {
            val model = loadModel()
            val recommendations = mutableListOf<Movie>()
            val favoriteIds = favoriteMovies.mapNotNull { it.id }.toSet()
            
            // Map pour accès rapide aux films par ID
            val movieMap = allMovies.associateBy { it.id }

            // Pour chaque film favori
            for (favoriteMovie in favoriteMovies) {
                val favoriteId = favoriteMovie.id ?: continue
                android.util.Log.d("RecommendationEngine", "Processing favorite: ${favoriteMovie.title} (ID: $favoriteId)")
                
                // Récupérer la liste des films similaires (array JSON)
                val similarMoviesArray = model.optJSONArray(favoriteId.toString())
                
                if (similarMoviesArray != null) {
                    // Parcourir les films similaires (triés par score de similarité)
                    var found = false
                    for (i in 0 until similarMoviesArray.length()) {
                        if (found) break
                        
                        val similarItem = similarMoviesArray.getJSONObject(i)
                        val recommendedId = similarItem.getInt("id")
                        
                        // Prendre le 1er film qui n'est pas dans les favoris
                        if (recommendedId !in favoriteIds && recommendedId !in recommendations.mapNotNull { it.id }) {
                            val movie = movieMap[recommendedId]
                            if (movie != null) {
                                android.util.Log.d("RecommendationEngine", "Recommending: ${movie.title} (ID: $recommendedId)")
                                recommendations.add(movie)
                                found = true // Marquer comme trouvé pour sortir de la boucle
                            }
                        }
                    }
                }
            }

            android.util.Log.d("RecommendationEngine", "Total recommendations: ${recommendations.size}")
            return@withContext recommendations
            
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    /**
     * Vérifie si l'utilisateur a exactement 5 films favoris
     * @return true si l'utilisateur a exactement 5 favoris, false sinon
     */
    fun hasRequiredFavorites(favoriteCount: Int): Boolean {
        return favoriteCount == 5
    }

    /**
     * Libère les ressources du modèle
     */
    fun close() {
        similarityModel = null
    }
}
