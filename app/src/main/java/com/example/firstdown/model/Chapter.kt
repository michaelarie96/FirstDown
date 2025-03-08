package com.example.firstdown.model

data class Chapter(
    val id: String,
    val courseId: String,  // Reference to parent course
    val title: String,
    val description: String,
    val lessons: List<Lesson>,  // Ordered list of lessons
    val isLocked: Boolean,
    val requiredChapterIds: List<String> = emptyList(),
    val quiz: Quiz? = null,
    val quizCompleted: Boolean = false,
    val index: Int              // Position within course
) {
    // Calculate progress based on completed lessons
    val progress: Int
        get() {
            if (lessons.isEmpty()) return 0

            // Count completed lessons
            val completedLessons = lessons.count { it.isCompleted }
            return (completedLessons * 100) / lessons.size
        }

    // Chapter is fully completed if all lessons are completed and quiz is completed
    val isCompleted: Boolean
        get() = progress == 100 && (quiz == null || quizCompleted)
}

