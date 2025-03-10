package com.example.firstdown.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstdown.R
import com.example.firstdown.adapters.PostAdapter
import com.example.firstdown.databinding.FragmentCommunityBinding
import com.example.firstdown.model.Post
import com.example.firstdown.viewmodel.CommunityViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class CommunityFragment : Fragment(), PostAdapter.PostInteractionListener {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    // Current filter state
    private var currentTabPosition = 0
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
        displayPosts()
    }

    private fun setupUI() {
        // Initialize adapter
        postAdapter = PostAdapter()
        postAdapter.listener = this

        // Set up RecyclerView
        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
            setHasFixedSize(true)
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

        // New post button
        binding.fabNewPost.setOnClickListener {
            showAddPostDialog()
        }
    }

    private fun showAddPostDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_add_post_dialog, null)

        val etPostContent = dialogView.findViewById<TextInputEditText>(R.id.et_post_content)
        val rgPostType = dialogView.findViewById<RadioGroup>(R.id.rg_post_type)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val content = etPostContent.text.toString().trim()
                if (content.isNotEmpty()) {
                    // Get the selected post type
                    val selectedType = when (rgPostType.checkedRadioButtonId) {
                        R.id.rb_question -> "question"
                        R.id.rb_tip -> "tip"
                        else -> "general"
                    }

                    // Format content based on type
                    val formattedContent = when (selectedType) {
                        "question" -> if (!content.endsWith("?")) "$content?" else content
                        "tip" -> if (!content.startsWith("Pro tip:", ignoreCase = true)) "Pro tip: $content" else content
                        else -> content
                    }

                    createNewPost(formattedContent)
                } else {
                    Toast.makeText(requireContext(), "Post content cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun createNewPost(content: String) {
        // Get current user
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userName = currentUser?.displayName ?: "Anonymous"
        val userProfileImage = currentUser?.photoUrl?.toString() ?: ""

        // Create a new post with unique ID
        val postId = UUID.randomUUID().toString()

        val newPost = Post(
            id = postId,
            userProfileImage = userProfileImage,
            userName = userName,
            timeAgo = "Just now",
            content = content,
            imageUrl = null,
            likes = 0,
            comments = 0,
            likedByCurrentUser = false // Explicitly set to false for new posts
        )

        // Add the post through ViewModel
        viewModel.addNewPost(newPost) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Post created successfully!", Toast.LENGTH_SHORT).show()
                // Refresh the posts list
                viewModel.getAllPosts { allPosts ->
                    displayFilteredPosts(allPosts)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to create post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFiltersAndDisplayPosts() {
        viewModel.filterPostsByType(currentTabPosition) { filteredPosts ->
            displayFilteredPosts(filteredPosts)
        }
    }

    private fun displayPosts() {
        viewModel.getAllPosts { allPosts ->
            displayFilteredPosts(allPosts)
        }
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

    override fun onLikeClicked(post: Post, position: Int) {
        // Update like count through ViewModel
        viewModel.toggleLike(post.id) { updatedLikes ->
            // After toggling like, refresh the posts to update UI
            viewModel.getAllPosts { refreshedPosts ->
                displayFilteredPosts(refreshedPosts)
            }
        }
    }

    override fun onCommentClicked(post: Post, position: Int) {
        // In the future, this would navigate to a comment screen or show a comment dialog
        Toast.makeText(requireContext(), "Comments feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}