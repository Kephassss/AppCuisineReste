package com.repasdelaflemme.app.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "repas_db"
            )
                // .fallbackToDestructiveMigration() // décommente en dev si pas de migrations
                .build().also { INSTANCE = it }
        }
    }
}
