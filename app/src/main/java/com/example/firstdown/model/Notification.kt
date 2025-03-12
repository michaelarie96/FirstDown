package com.example.firstdown.model

data class Notification(
    val id: String = "",
    val recipientUserName: String = "",  // User who receives the notification
    val message: String = "",
    val postId: String = "",
    val likerName: String = "",  // Name of the user who liked
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false
)