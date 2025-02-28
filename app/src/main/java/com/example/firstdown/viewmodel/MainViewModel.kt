package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson
import com.example.firstdown.model.User

class MainViewModel : ViewModel() {

    // Get current user data from DataManager
    fun getCurrentUser(): User {
        return DataManager.getCurrentUser()
    }

    // Get current lesson
    fun getCurrentLesson(): Lesson {
        return DataManager.getCurrentLesson()
    }

    // Check if lesson has started
    fun hasStartedLesson(lessonId: String): Boolean {
        return DataManager.hasStartedLesson(lessonId)
    }

    // Get lesson progress
    fun getLessonProgress(lessonId: String): Int {
        return DataManager.getLessonProgress(lessonId)
    }

    // Update goal completion status
    fun setGoalCompleted(completed: Boolean) {
        DataManager.setGoalCompleted(completed)
    }

    // Check if goal is completed
    fun isGoalCompleted(): Boolean {
        return DataManager.isGoalCompleted()
    }

    // Get lesson content size
    fun getLessonContentSize(lessonId: String): Int {
        return DataManager.getLessonById(lessonId)?.content?.size ?: 0
    }
}