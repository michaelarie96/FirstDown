package com.example.firstdown.model

data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageResId: Int = 0,
    val chapters: List<Chapter> = emptyList()
) {
    // Calculate overall course progress based on completed chapters
    val progress: Int
        get() {
            if (chapters.isEmpty()) return 0

            val totalLessons = chapters.sumOf { it.lessons.size }
            if (totalLessons == 0) return 0

            val completedLessons = chapters.sumOf { chapter ->
                chapter.lessons.count { lesson -> DataManager.isLessonCompletedSync(lesson.id) }
            }

            return (completedLessons * 100) / totalLessons
        }

    // Calculate total lessons in the course
    val totalLessons: Int
        get() = chapters.sumOf { it.lessons.size }

    val isCompleted: Boolean
        get() = chapters.all { DataManager.isChapterCompleted(it.id) }
}
