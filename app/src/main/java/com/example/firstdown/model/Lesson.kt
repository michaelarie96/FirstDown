package com.example.firstdown.model

data class Lesson(
    val id: String = "",
    val chapterId: String = "",  // Reference to parent chapter
    val title: String = "",
    val description: String = "",
    val contentType: ContentType = ContentType.TEXT,  // TEXT, IMAGE, VIDEO, etc.
    val contentData: String = "",       // For text or references to resources
    val imageResId: Int? = null,   // For image content
    val durationMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val index: Int = 0                 // Position within chapter
)

enum class ContentType {
    TEXT,
    IMAGE,
    VIDEO,
    INTERACTIVE
}