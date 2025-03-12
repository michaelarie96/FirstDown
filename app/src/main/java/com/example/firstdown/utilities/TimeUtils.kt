package com.example.firstdown.utilities

import java.util.concurrent.TimeUnit

object TimeUtils {

    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        if (timestamp > now || timestamp <= 0) {
            return "Just now"
        }

        val diff = now - timestamp
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)} minutes ago"
            diff < TimeUnit.HOURS.toMillis(2) -> "1 hour ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)} hours ago"
            diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            diff < TimeUnit.DAYS.toMillis(7) -> "${diff / TimeUnit.DAYS.toMillis(1)} days ago"
            diff < TimeUnit.DAYS.toMillis(30) -> "${diff / TimeUnit.DAYS.toMillis(7)} weeks ago"
            diff < TimeUnit.DAYS.toMillis(365) -> "${diff / TimeUnit.DAYS.toMillis(30)} months ago"
            else -> "${diff / TimeUnit.DAYS.toMillis(365)} years ago"
        }
    }
}