// File: app/src/main/java/com/example/firstdown/viewmodel/CourseProgressViewModel.kt
package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson

class CourseProgressViewModel : ViewModel() {

    // Get course by ID
    fun getCourseById(courseId: String): Course? {
        return DataManager.getCourseById(courseId)
    }

    // Get default course if ID is not provided
    fun getDefaultCourse(): Course {
        return DataManager.getCurrentCourse()
    }

    // Get chapter by ID
    fun getChapterById(chapterId: String): Chapter? {
        return DataManager.getChapterById(chapterId)
    }

    // Get the first lesson from a chapter
    fun getFirstLesson(chapter: Chapter): Lesson? {
        return if (chapter.lessons.isNotEmpty()) chapter.lessons.first() else null
    }
}