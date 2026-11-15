package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TournamentEntity::class, WalletTransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShockwaveDatabase : RoomDatabase() {
    abstract fun shockwaveDao(): ShockwaveDao

    companion object {
        @Volatile
        private var INSTANCE: ShockwaveDatabase? = null

        fun getInstance(context: Context): ShockwaveDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ShockwaveDatabase::class.java,
                    "shockwave_tournament_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
