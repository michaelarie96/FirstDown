package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Achievement
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson
import com.example.firstdown.model.User

class MainViewModel : ViewModel() {

    fun getCurrentUser(onComplete: (User) -> Unit) {
        onComplete(DataManager.getCurrentUser())
    }

    fun getNextLessonToComplete(onComplete: (Lesson?) -> Unit) {
        DataManager.getNextLessonToComplete(onComplete)
    }

    fun wasLessonCompletedToday(lessonId: String, onComplete: (Boolean) -> Unit) {
        onComplete(DataManager.wasLessonCompletedToday(lessonId))
    }

    fun getCurrentOrNextChapter(onComplete: (Pair<Course, Chapter>?) -> Unit) {
        DataManager.getCurrentOrNextChapter(onComplete)
    }

    fun hasStartedChapter(chapterId: String, onComplete: (Boolean) -> Unit) {
        onComplete(DataManager.hasStartedChapter(chapterId))
    }

    fun hasStartedAnyLearning(onComplete: (Boolean) -> Unit) {
        onComplete(DataManager.hasStartedLearning())
    }

    fun getLatestAchievement(onComplete: (Achievement?) -> Unit) {
        onComplete(DataManager.getLatestAchievement())
    }

    fun getRandomQuickTip(onComplete: (String) -> Unit) {
        onComplete(DataManager.getRandomQuickTip())
    }

    fun hasStartedLesson(lessonId: String, onComplete: (Boolean) -> Unit) {
        onComplete(DataManager.hasStartedLesson(lessonId))
    }

    fun isLessonCompleted(lessonId: String, onComplete: (Boolean) -> Unit) {
        onComplete(DataManager.isLessonCompleted(lessonId))
    }

    fun markLessonComplete(lessonId: String) {
        DataManager.markLessonComplete(lessonId)
    }

    fun getLessonById(lessonId: String, onComplete: (Lesson?) -> Unit) {
        DataManager.getLessonById(lessonId, onComplete)
    }
}