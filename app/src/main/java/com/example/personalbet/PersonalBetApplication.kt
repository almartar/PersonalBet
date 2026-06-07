package com.example.personalbet

import android.app.Application
import androidx.room.Room
import com.example.personalbet.data.AppDatabase


class PersonalBetApplication : Application() {

    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "personalbet.db",
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }
}
