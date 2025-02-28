package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.User

class ProfileViewModel : ViewModel() {

    // Get current user from DataManager
    fun getCurrentUser(): User {
        return DataManager.getCurrentUser()
    }
}