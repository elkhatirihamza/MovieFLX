package com.iacontrol.movieflx.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "movie_table")
@Parcelize
data class Movie(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int?,  // Changed to Nullable (Int?)

    @ColumnInfo(name = "Movie_Title")
    val title: String?, // Changed to Nullable (String?)

    @ColumnInfo(name = "Year")
    val year: Int?,

    @ColumnInfo(name = "Director")
    val director: Int?,

    @ColumnInfo(name = "Actors")
    val actors: String?,

    @ColumnInfo(name = "Rating")
    val rating: Double?,

    @ColumnInfo(name = "Runtime(Mins)")
    val duration: Int?,

    @ColumnInfo(name = "Total_Gross")
    val totalGross: String?,

    @ColumnInfo(name = "main_genre")
    val mainGenre: String?,

    @ColumnInfo(name = "side_genre")
    val sideGenre: String?,

    @ColumnInfo(name = "poster_url")
    val imageUrl: String?,

    @ColumnInfo(name = "plot")
    val description: String?,

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean? // Changed to Nullable (Boolean?)
) : Parcelable