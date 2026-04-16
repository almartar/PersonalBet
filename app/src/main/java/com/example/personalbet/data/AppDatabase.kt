package com.example.personalbet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Bet::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun betDao(): BetDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bets ADD COLUMN betTypeGroup TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE bets ADD COLUMN betTypeName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE bets ADD COLUMN marketType TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
