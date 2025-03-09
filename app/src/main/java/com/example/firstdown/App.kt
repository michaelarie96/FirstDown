package com.example.firstdown

import android.app.Application
import com.example.firstdown.model.DataManager
import com.example.firstdown.utilities.ImageLoader
import com.example.firstdown.utilities.SharedPreferencesManager

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        SharedPreferencesManager.init(this)
        //ImageLoader.init(this)
        DataManager.initialize()
    }
}
