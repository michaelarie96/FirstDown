package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.User

class ProfileViewModel : ViewModel() {

    fun getCurrentUser(onComplete: (User) -> Unit) {
        onComplete(DataManager.getCurrentUser())
    }
}