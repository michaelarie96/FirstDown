package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Post

class CommunityViewModel : ViewModel() {

    // Get all posts
    fun getAllPosts(): List<Post> {
        return DataManager.getAllPosts()
    }

    // Get posts filtered by a search term
    fun searchPosts(query: String): List<Post> {
        if (query.isBlank()) return getAllPosts()

        return getAllPosts().filter { post ->
            post.content.contains(query, ignoreCase = true) ||
                    post.userName.contains(query, ignoreCase = true)
        }
    }

    // Filter posts by type (All, Questions, Tips)
    fun filterPostsByType(tabPosition: Int): List<Post> {
        return when (tabPosition) {
            0 -> getAllPosts() // All Posts
            1 -> getAllPosts().filter { it.content.contains("?") } // Questions (contains a question mark)
            2 -> getAllPosts().filter { it.content.startsWith("Pro tip:", ignoreCase = true) } // Tips
            else -> getAllPosts()
        }
    }

    fun toggleLike(postId: String): Int {
        // In future, this would update a database
        val post = DataManager.getPostById(postId)

        // For now, we'll just toggle the like locally
        post?.let {
            // Toggle like by adding 1 (simulating the current user liking the post)
            // In a real implementation, we'd track which posts the user has liked
            // and toggle the count properly
            return it.likes + 1
        }

        return 0
    }
}