package com.example.firstdown.model

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val imageResId: Int,
    val chapters: List<Chapter>,
    val difficulty: String = "Beginner",
    val estimatedHours: Int = 0,
    val isPopular: Boolean = false,
    val authorName: String = "Football Experts"
) {
    // Calculate overall course progress based on chapter progress
    val progress: Int
        get() {
            if (chapters.isEmpty()) return 0

            // Sum up all chapter progress and divide by chapter count
            return chapters.sumOf { it.progress } / chapters.size
        }

    // Calculate total lessons in the course
    val totalLessons: Int
        get() = chapters.sumOf { it.lessons.size }

    // Check if the course is completed (all chapters 100% complete)
    val isCompleted: Boolean
        get() = chapters.all { it.progress == 100 }
}
