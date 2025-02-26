package com.example.firstdown.model

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val durationMinutes: Int,
    val content: List<LessonContent>,
    val quizzes: List<Quiz>
)

sealed class LessonContent {
    data class Text(val content: String) : LessonContent()
    data class Image(val imageResId: Int, val caption: String) : LessonContent()
    data class Video(val videoUrl: String, val thumbnailResId: Int) : LessonContent()
    data class Interactive(val type: String, val data: Map<String, Any>) : LessonContent()
}
