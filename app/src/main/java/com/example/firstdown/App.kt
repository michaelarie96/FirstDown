package com.example.firstdown

import android.app.Application
import android.util.Log
import com.example.firstdown.model.DataManager
import com.example.firstdown.utilities.SharedPreferencesManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable Firebase offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        SharedPreferencesManager.init(this)
        DataManager.initialize()

        // To upload static collections to database when necessary
        //SharedPreferencesManager.getInstance().putBoolean("database_initialized", false)
    }
}
