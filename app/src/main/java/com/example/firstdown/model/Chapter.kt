package com.example.firstdown.model

data class Chapter(
    val id: String,
    val courseId: String,  // Reference to parent course
    val title: String,
    val description: String,
    val lessons: List<Lesson>,  // Ordered list of lessons
    val isLocked: Boolean,
    val quiz: Quiz? = null,
    val quizCompleted: Boolean = false,
    val index: Int              // Position within course
) {
    // Calculate progress based on completed lessons
    val progress: Int
        get() {
            if (lessons.isEmpty()) return 0

            // Count lessons that are marked as completed in DataManager
            val completedLessonsCount = lessons.count { DataManager.isLessonCompleted(it.id) }
            return (completedLessonsCount * 100) / lessons.size
        }

    // Chapter is fully completed if all lessons are completed and quiz is completed
    val isCompleted: Boolean
        get() {
            val allLessonsCompleted = lessons.all { it.isCompleted }
            val quizCompleted = quiz == null || this.quizCompleted || DataManager.isQuizCompleted(id)
            return allLessonsCompleted && quizCompleted
        }
}

