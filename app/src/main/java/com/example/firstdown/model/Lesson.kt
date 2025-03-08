package com.example.firstdown.model

data class Lesson(
    val id: String,
    val chapterId: String,  // Reference to parent chapter
    val title: String,
    val description: String,
    val contentType: ContentType,  // TEXT, IMAGE, VIDEO, etc.
    val contentData: String,       // For text or references to resources
    val imageResId: Int? = null,   // For image content
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val index: Int                 // Position within chapter
)

enum class ContentType {
    TEXT,
    IMAGE,
    VIDEO,
    INTERACTIVE
}