package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Achievement
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson
import com.example.firstdown.model.User

class MainViewModel : ViewModel() {

    // Get current user data from DataManager
    fun getCurrentUser(): User {
        return DataManager.getCurrentUser()
    }

    // Get next lesson to complete (for Today's Goal)
    fun getNextLessonToComplete(): Lesson? {
        return DataManager.getNextLessonToComplete()
    }

    // Check if lesson was completed today
    fun wasLessonCompletedToday(lessonId: String): Boolean {
        return DataManager.wasLessonCompletedToday(lessonId)
    }

    // Get current or next chapter to study
    fun getCurrentOrNextChapter(): Pair<Course, Chapter>? {
        return DataManager.getCurrentOrNextChapter()
    }

    // Check if a chapter has been started
    fun hasStartedChapter(chapterId: String): Boolean {
        return DataManager.hasStartedChapter(chapterId)
    }

    // Check if user has started any learning
    fun hasStartedAnyLearning(): Boolean {
        return DataManager.hasStartedLearning()
    }

    // Get the latest achievement
    fun getLatestAchievement(): Achievement? {
        return DataManager.getLatestAchievement()
    }

    // Get a random quick tip
    fun getRandomQuickTip(): String {
        return DataManager.getRandomQuickTip()
    }

    // Check if lesson has started
    fun hasStartedLesson(lessonId: String): Boolean {
        return DataManager.hasStartedLesson(lessonId)
    }

    // Get lesson progress
    fun getLessonProgress(lessonId: String): Int {
        return DataManager.getLessonProgress(lessonId)
    }

    // Get lesson content size
    fun getLessonContentSize(lessonId: String): Int {
        return DataManager.getLessonById(lessonId)?.content?.size ?: 0
    }
}