package com.example.firstdown.model

data class Quiz(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val explanation: String = ""
)
