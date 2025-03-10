package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.R
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson

class ChaptersViewModel : ViewModel() {

    fun getCourseById(courseId: String, onComplete: (Course?) -> Unit) {
        DataManager.getCourseById(courseId, onComplete)
    }

    fun getDefaultCourse(onComplete: (Course) -> Unit) {
        DataManager.getCourseById("football-basics") { course ->
            if (course != null) {
                onComplete(course)
            } else {
                // Fallback to first course if default not found
                DataManager.getAllCourses { courses ->
                    if (courses.isNotEmpty()) {
                        onComplete(courses.first())
                    } else {
                        // Create an empty course as last resort
                        onComplete(Course(
                            id = "empty",
                            title = "No Courses Available",
                            description = "Please try again later",
                            imageResId = R.drawable.football_field_bg,
                            chapters = emptyList()
                        ))
                    }
                }
            }
        }
    }

    fun getChapterById(chapterId: String, onComplete: (Chapter?) -> Unit) {
        DataManager.getChapterById(chapterId, onComplete)
    }

    fun getFirstLesson(chapter: Chapter, onComplete: (Lesson?) -> Unit) {
        if (chapter.lessons.isNotEmpty()) {
            onComplete(chapter.lessons.first())
        } else {
            onComplete(null)
        }
    }

    fun getNextLesson(chapter: Chapter, onComplete: (Lesson?) -> Unit) {
        DataManager.getNextLessonInChapter(chapter.id, onComplete)
    }

    fun hasStartedChapter(chapterId: String, onComplete: (Boolean) -> Unit) {
        onComplete(DataManager.hasStartedChapter(chapterId))
    }

    fun shouldChapterBeLocked(chapterId: String, onComplete: (Boolean) -> Unit) {
        DataManager.shouldChapterBeLocked(chapterId, onComplete)
    }
}