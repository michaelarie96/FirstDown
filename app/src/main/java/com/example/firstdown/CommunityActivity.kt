package com.example.firstdown

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstdown.adapters.PostAdapter
import com.example.firstdown.databinding.ActivityCommunityBinding
import com.example.firstdown.model.Post
import com.example.firstdown.viewmodel.CommunityViewModel
import com.google.android.material.tabs.TabLayout

class CommunityActivity : AppCompatActivity(), PostAdapter.PostInteractionListener {

    private lateinit var binding: ActivityCommunityBinding
    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    // Current filter state
    private var currentTabPosition = 0
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        displayPosts()
    }

    private fun setupUI() {
        // Set up tab layout
        binding.tabLayout.apply {
            addTab(newTab().setText("All Posts"))
            addTab(newTab().setText("Questions"))
            addTab(newTab().setText("Tips"))
        }

        // Initialize adapter
        postAdapter = PostAdapter()
        postAdapter.listener = this

        // Set up RecyclerView
        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(this@CommunityActivity)
            adapter = postAdapter
            setHasFixedSize(true) // Optimization if items have fixed size
        }
    }

    private fun setupListeners() {
        // Tab selection listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTabPosition = tab.position
                applyFiltersAndDisplayPosts()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Search input listener
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                applyFiltersAndDisplayPosts()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // New post button
        binding.fabNewPost.setOnClickListener {
            // Handle new post creation (to be implemented later)
            Toast.makeText(this, "New post feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFiltersAndDisplayPosts() {
        // Apply both tab filter and search query
        val filteredPosts = if (currentSearchQuery.isNotEmpty()) {
            viewModel.searchPosts(currentSearchQuery)
        } else {
            viewModel.filterPostsByType(currentTabPosition)
        }

        displayFilteredPosts(filteredPosts)
    }

    private fun displayPosts() {
        // Get all posts from ViewModel
        val allPosts = viewModel.getAllPosts()
        displayFilteredPosts(allPosts)
    }

    private fun displayFilteredPosts(posts: List<Post>) {
        if (posts.isEmpty()) {
            // Show empty state
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvPosts.visibility = View.GONE
        } else {
            // Show posts
            binding.tvEmptyState.visibility = View.GONE
            binding.rvPosts.visibility = View.VISIBLE

            // Update adapter with new posts
            postAdapter.updatePosts(posts)
        }
    }

    // PostInteractionListener implementation
    override fun onLikeClicked(post: Post, position: Int) {
        // Update like count through ViewModel
        val updatedLikes = viewModel.toggleLike(post.id)
        Toast.makeText(this, "Liked! Total likes: $updatedLikes", Toast.LENGTH_SHORT).show()

        // Refresh the post list (in a real app, you'd update just the changed item)
        displayPosts()
    }

    override fun onCommentClicked(post: Post, position: Int) {
        // Navigate to comments screen or show comments dialog
        Toast.makeText(this, "Comments feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun onBookmarkClicked(post: Post, position: Int) {
        // Handle bookmark/save functionality
        Toast.makeText(this, "Post saved!", Toast.LENGTH_SHORT).show()
    }
}