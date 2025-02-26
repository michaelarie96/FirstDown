package com.example.firstdown.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val earnedDate: Long // timestamp
)
