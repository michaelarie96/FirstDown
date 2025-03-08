package com.example.firstdown.model

data class UserProgress(
    val userId: String = "",
    val completedLessons: MutableList<String> = mutableListOf(),
    val completedQuizzes: MutableList<String> = mutableListOf(),
    val completedChapters: MutableList<String> = mutableListOf(),
    val completedCourses: MutableList<String> = mutableListOf(),
    val startedLessons: MutableList<String> = mutableListOf(),
    val startedChapters: MutableList<String> = mutableListOf(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Empty constructor for Firestore
    constructor() : this("")
}