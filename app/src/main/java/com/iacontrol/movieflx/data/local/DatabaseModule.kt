package com.iacontrol.movieflx.data.local

import android.content.Context
import androidx.room.Room

import com.iacontrol.movieflx.data.local.AppDatabase
import com.iacontrol.movieflx.data.local.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour fournir les dépendances liées à la base de données (Room).
 * Installé dans le SingletonComponent pour que ces dépendances soient disponibles
 * durant tout le cycle de vie de l'application.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "database.db"   // Le nom interne de ta DB
        )
            .createFromAsset("databases/database.db") // <-- Lien avec ta DB pré-remplie
            //.fallbackToDestructiveMigration()       // Optionnel
            .build()
    }

    @Provides
    fun provideMovieDao(appDatabase: AppDatabase): MovieDao {
        return appDatabase.movieDao()
    }
}
