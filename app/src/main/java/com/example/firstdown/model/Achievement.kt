package com.example.firstdown.model

enum class AchievementType {
    HIGH_QUIZ_SCORE,
    CHAPTER_COMPLETED,
    COURSE_COMPLETED
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val iconResId: Int,
    val earnedDate: Long, // timestamp
    val relatedId: String? = null // courseId or chapterId if applicable
)