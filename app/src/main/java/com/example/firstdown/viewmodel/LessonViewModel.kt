package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson

class LessonViewModel : ViewModel() {

    // Get lesson details
    fun getLessonById(lessonId: String): Lesson? {
        return DataManager.getLessonById(lessonId)
    }

    // Mark lesson as completed
    fun markLessonComplete(lessonId: String) {
        DataManager.markLessonComplete(lessonId)
    }

    // Get lesson content (text and optional image)
    fun getLessonContent(lessonId: String): Pair<String, Int?> {
        return DataManager.getLessonContent(lessonId)
    }

    // Get the chapter that contains this lesson
    fun getChapterForLesson(lessonId: String): Chapter? {
        val lesson = getLessonById(lessonId) ?: return null
        return DataManager.getChapterById(lesson.chapterId)
    }

    // Get the next lesson in the same chapter
    fun getNextLesson(currentLessonId: String): Lesson? {
        val currentLesson = getLessonById(currentLessonId) ?: return null
        val chapter = DataManager.getChapterById(currentLesson.chapterId) ?: return null

        // Find the current lesson's index
        val currentIndex = currentLesson.index

        // Get the next lesson by index
        return chapter.lessons.firstOrNull { it.index > currentIndex }
    }

    // Get the previous lesson in the same chapter
    fun getPreviousLesson(currentLessonId: String): Lesson? {
        val currentLesson = getLessonById(currentLessonId) ?: return null
        val chapter = DataManager.getChapterById(currentLesson.chapterId) ?: return null

        // Find the current lesson's index
        val currentIndex = currentLesson.index

        // Get previous lessons sorted by index and take the last one
        return chapter.lessons
            .filter { it.index < currentIndex }
            .maxByOrNull { it.index }
    }

    fun isLastLessonInChapter(lessonId: String): Boolean {
        return DataManager.isLastLessonInChapter(lessonId)
    }
}