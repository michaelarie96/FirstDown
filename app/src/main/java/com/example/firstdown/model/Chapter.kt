package com.example.firstdown.model

data class Chapter(
    val id: String,
    val title: String,
    val description: String,
    val lessons: List<Lesson>,
    val progress: Int, // 0-100
    val isLocked: Boolean,
    val requiredChapterIds: List<String> = emptyList(),
    val quizCompleted: Boolean = false
)
