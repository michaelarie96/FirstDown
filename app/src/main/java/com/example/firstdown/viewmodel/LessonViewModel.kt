package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson

class LessonViewModel : ViewModel() {

    fun getLessonById(lessonId: String): Lesson? {
        return DataManager.getLessonById(lessonId)
    }

    fun markLessonComplete(lessonId: String) {
        DataManager.markLessonComplete(lessonId)
    }

    fun getLessonContent(lessonId: String, onComplete: (Pair<String, Int?>) -> Unit) {
        DataManager.getLessonById(lessonId) { lesson ->
            if (lesson != null) {
                onComplete(Pair(lesson.contentData, lesson.imageResId))
            }
        }
    }

    fun getChapterForLesson(lessonId: String, onComplete: (Chapter?) -> Unit) {
        DataManager.getLessonById(lessonId) { lesson ->
            if (lesson == null) {
                onComplete(null)
                return@getLessonById
            }
            DataManager.getChapterById(lesson.chapterId, onComplete)
        }
    }

    fun getNextLesson(currentLessonId: String, onComplete: (Lesson?) -> Unit) {
        DataManager.getLessonById(currentLessonId) { currentLesson ->
            if (currentLesson == null) {
                onComplete(null)
                return@getLessonById
            }

            DataManager.getChapterById(currentLesson.chapterId) { chapter ->
                if (chapter == null) {
                    onComplete(null)
                    return@getChapterById
                }

                val currentIndex = currentLesson.index

                val nextLesson = chapter.lessons.firstOrNull { it.index > currentIndex }
                onComplete(nextLesson)
            }
        }
    }

    fun getPreviousLesson(currentLessonId: String, onComplete: (Lesson?) -> Unit) {
        DataManager.getLessonById(currentLessonId) { currentLesson ->
            if (currentLesson == null) {
                onComplete(null)
                return@getLessonById
            }

            DataManager.getChapterById(currentLesson.chapterId) { chapter ->
                if (chapter == null) {
                    onComplete(null)
                    return@getChapterById
                }

                val currentIndex = currentLesson.index

                val previousLesson = chapter.lessons
                    .filter { it.index < currentIndex }
                    .maxByOrNull { it.index }

                onComplete(previousLesson)
            }
        }
    }

    fun isLastLessonInChapter(lessonId: String, onComplete: (Boolean) -> Unit) {
        DataManager.isLastLessonInChapter(lessonId, onComplete)
    }
}