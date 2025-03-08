package com.example.firstdown.model

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val imageResId: Int,
    val chapters: List<Chapter>
) {
    // Calculate overall course progress based on completed chapters
    val progress: Int
        get() {
            if (chapters.isEmpty()) return 0

            // Count completed chapters
            val completedChapters = chapters.count { it.isCompleted }
            return (completedChapters * 100) / chapters.size
        }

    // Calculate total lessons in the course
    val totalLessons: Int
        get() = chapters.sumOf { it.lessons.size }

    val isCompleted: Boolean
        get() = chapters.all { it.isCompleted }
}
