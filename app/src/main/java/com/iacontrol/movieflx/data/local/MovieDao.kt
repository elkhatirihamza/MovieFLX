package com.iacontrol.movieflx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iacontrol.movieflx.data.model.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Query("""
    UPDATE movie_table 
    SET isFavorite = :isFavorite 
    WHERE id = :movieId
      """)
    suspend fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean)


    // MODIFICATION ICI : Ajout de limit/offset et retour List<Movie> au lieu de Flow
    @Query("""
    SELECT * FROM movie_table 
    WHERE (:searchQuery IS NULL OR Movie_Title LIKE '%' || :searchQuery || '%')
      AND (:genre IS NULL OR :genre = 'Tous' OR main_genre LIKE '%' || :genre || '%')
      AND (:minRating IS NULL OR Rating >= :minRating)
      AND (:minYear IS NULL OR Year >= :minYear)
      AND (:maxYear IS NULL OR Year <= :maxYear)
    ORDER BY 
      CASE WHEN :sortType = 'rating' THEN Rating END DESC,
      CASE WHEN :sortType = 'recent' THEN Year END DESC,
      rowid ASC
      LIMIT :limit OFFSET :offset
""")
    suspend fun filterMovies(
        searchQuery: String?,
        genre: String?,
        minRating: Float?,
        minYear: Int?,
        maxYear: Int?,
        sortType: String,
        limit: Int,
        offset: Int
    ): List<Movie>


    // 3. Obtenir tous les films (pour l'affichage LikedMoviesFragment)
    @Query("SELECT * FROM movie_table LIMIT 10")
    fun getAllMovies(): Flow<List<Movie>>

    // Obtenir tous les films (pour les recommandations)
    @Query("SELECT * FROM movie_table")
    suspend fun getAllMoviesAsList(): List<Movie>

    @Query("SELECT * FROM movie_table WHERE isFavorite = 1 ORDER BY rowid ASC")
    fun getFavoriteMovies(): Flow<List<Movie>>

    @Query("SELECT isFavorite FROM movie_table WHERE  id = :movieId ")
    fun getMovieState(movieId: Int) : Flow<Boolean>


    @Query("SELECT DISTINCT main_genre from movie_table")
    fun getAllCategories(): Flow<List<String>>

}