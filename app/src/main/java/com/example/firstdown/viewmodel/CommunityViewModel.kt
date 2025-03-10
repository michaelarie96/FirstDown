package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Post

class CommunityViewModel : ViewModel() {

    fun getAllPosts(onComplete: (List<Post>) -> Unit) {
        DataManager.getAllPosts(onComplete)
    }

    // Get posts filtered by a search term
    fun searchPosts(query: String, onComplete: (List<Post>) -> Unit) {
        DataManager.getAllPosts { allPosts ->
            if (query.isBlank()) {
                onComplete(allPosts)
                return@getAllPosts
            }

            val filteredPosts = allPosts.filter { post ->
                post.content.contains(query, ignoreCase = true) ||
                        post.userName.contains(query, ignoreCase = true)
            }
            onComplete(filteredPosts)
        }
    }

    // Filter posts by type (All, Questions, Tips)
    fun filterPostsByType(tabPosition: Int, onComplete: (List<Post>) -> Unit) {
        DataManager.getAllPosts { allPosts ->
            val filteredPosts = when (tabPosition) {
                0 -> allPosts // All Posts
                1 -> allPosts.filter { it.content.contains("?") } // Questions (contains a question mark)
                2 -> allPosts.filter { it.content.startsWith("Pro tip:", ignoreCase = true) } // Tips
                else -> allPosts
            }
            onComplete(filteredPosts)
        }
    }


    fun toggleLike(postId: String, onComplete: (Int) -> Unit) {
        DataManager.toggleLike(postId) { updatedLikes, isLiked ->
            onComplete(updatedLikes)
        }
    }

    fun addNewPost(post: Post, onComplete: (Boolean) -> Unit) {
        DataManager.addNewPost(post, onComplete)
    }
}
