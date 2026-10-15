package com.iacontrol.movieflx.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iacontrol.movieflx.data.model.Movie


@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
}

