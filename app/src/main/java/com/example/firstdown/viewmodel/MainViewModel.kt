package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson
import com.example.firstdown.model.User

class MainViewModel : ViewModel() {

    fun getCurrentUser(onComplete: (User) -> Unit) {
        DataManager.getCurrentUser(onComplete)
    }

    fun getNextLessonToComplete(onComplete: (Lesson?) -> Unit) {
        DataManager.getNextLessonToComplete(onComplete)
    }

    fun wasLessonCompletedToday(lessonId: String, onComplete: (Boolean) -> Unit) {
        DataManager.wasLessonCompletedToday(lessonId, onComplete)
    }

    fun getCurrentOrNextChapter(onComplete: (Pair<Course, Chapter>?) -> Unit) {
        DataManager.getCurrentOrNextChapter(onComplete)
    }

    fun hasStartedChapter(chapterId: String, onComplete: (Boolean) -> Unit) {
        onComplete(DataManager.hasStartedChapter(chapterId))
    }

    fun hasStartedAnyLearning(onComplete: (Boolean) -> Unit) {
        DataManager.hasStartedLearning(onComplete)
    }

    fun getRandomQuickTip(onComplete: (String) -> Unit) {
        DataManager.getRandomQuickTip(onComplete)
    }

    fun hasStartedLesson(lessonId: String, onComplete: (Boolean) -> Unit) {
        DataManager.hasStartedLesson(lessonId, onComplete)
    }

    fun isLessonCompleted(lessonId: String, onComplete: (Boolean) -> Unit) {
        DataManager.isLessonCompleted(lessonId, onComplete)
    }

    fun markLessonComplete(lessonId: String) {
        DataManager.markLessonComplete(lessonId)
    }

    fun getLessonById(lessonId: String, onComplete: (Lesson?) -> Unit) {
        DataManager.getLessonById(lessonId, onComplete)
    }

    fun getUserStreak(onComplete: (Int) -> Unit) {
        DataManager.getUserStreak(onComplete)
    }

    fun getTodayGoal(onComplete: (Chapter?) -> Unit) {
        DataManager.getTodayGoal(onComplete)
    }
}