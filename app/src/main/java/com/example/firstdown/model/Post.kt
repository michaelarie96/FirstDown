package com.example.firstdown.model

data class Post(
    val id: String,
    val userProfileImage: String,
    val userName: String,
    val timeAgo: String,
    val content: String,
    val imageUrl: String?,
    val likes: Int,
    val comments: Int
)
