package com.example.firstdown.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String? = null,
    val title: String = "Beginner",
    val streakDays: Int = 0,
    val lessonsCompleted: Int = 0,
    val quizScore: Int = 0,
    val timeSpent: Long = 0 // in minutes
)