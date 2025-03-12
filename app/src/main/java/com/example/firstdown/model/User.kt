package com.example.firstdown.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String? = null,
    val title: String = "Beginner",
    val streakDays: Int = 0,
    val lessonsCompleted: Int = 0,
    val chaptersCompleted: Int = 0,
    val coursesCompleted: Int = 0,
    val quizScores: List<Int> = emptyList(),
    val timeSpent: Long = 0 // in minutes

) {
    // Calculate the average quiz score
    val averageQuizScore: Int
        get() = if (quizScores.isEmpty()) 0 else quizScores.sum() / quizScores.size
}