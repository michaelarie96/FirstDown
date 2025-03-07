package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson
import com.example.firstdown.model.LessonContent

class LessonContentViewModel : ViewModel() {

    // Get lesson details
    fun getLessonById(lessonId: String): Lesson? {
        return DataManager.getLessonById(lessonId)
    }

    // Update lesson progress
    fun updateLessonProgress(lessonId: String, pageNumber: Int) {
        DataManager.updateLessonProgress(lessonId, pageNumber)
    }

    // Get lesson content for a specific page
    fun getLessonContentForPage(lessonId: String, pageNumber: Int): LessonContent? {
        val lesson = getLessonById(lessonId)
        return if (lesson != null && pageNumber > 0 && pageNumber <= lesson.content.size) {
            lesson.content[pageNumber - 1]
        } else {
            null
        }
    }

    // Check if this is the last page
    fun isLastPage(lessonId: String, currentPage: Int): Boolean {
        val lesson = getLessonById(lessonId)
        return lesson != null && currentPage >= lesson.content.size
    }

    // Check if this is the first page
    fun isFirstPage(currentPage: Int): Boolean {
        return currentPage <= 1
    }

    // Get total pages
    fun getTotalPages(lessonId: String): Int {
        return getLessonById(lessonId)?.content?.size ?: 0
    }

    // Add this method to LessonContentViewModel
    fun getChapterForLesson(lessonId: String): Chapter? {
        // Look through all chapters to find the one containing this lesson
        return DataManager.getAllChapters().find { chapter ->
            chapter.lessons.any { it.id == lessonId }
        }
    }
}