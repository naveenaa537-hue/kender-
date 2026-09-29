package com.example

import android.app.Application
import com.example.data.local.KindredDatabase
import com.example.data.repository.KindredRepository

class KindredApp : Application() {
    lateinit var database: KindredDatabase
        private set
    lateinit var repository: KindredRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = KindredDatabase.getDatabase(this)
        repository = KindredRepository(database, this)
    }
}
