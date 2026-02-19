package com.moviemate

import android.app.Application
import com.moviemate.data.local.AppDatabase

class MovieMateApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MovieMateApplication
            private set
    }
}
