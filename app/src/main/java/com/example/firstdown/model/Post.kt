package com.example.firstdown.model

data class Post(
    val id: String = "",
    val userProfileImage: String = "",
    val userName: String = "",
    val timeAgo: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val likes: Int = 0,
    val comments: Int = 0,
    var likedByCurrentUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
