package com.example.firstdown.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstdown.adapters.PostAdapter
import com.example.firstdown.databinding.FragmentCommunityBinding
import com.example.firstdown.model.Post
import com.example.firstdown.viewmodel.CommunityViewModel
import com.google.android.material.tabs.TabLayout

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
            layoutManager = LinearLayoutManager(requireContext())
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
            // This would navigate to a new post creation screen in the future
            Toast.makeText(requireContext(), "New post feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFiltersAndDisplayPosts() {
        // Apply both tab filter and search query
        if (currentSearchQuery.isNotEmpty()) {
            viewModel.searchPosts(currentSearchQuery) { filteredPosts ->
                displayFilteredPosts(filteredPosts)
            }
        } else {
            viewModel.filterPostsByType(currentTabPosition) { filteredPosts ->
                displayFilteredPosts(filteredPosts)
            }
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
            Toast.makeText(requireContext(), "Liked! Total likes: $updatedLikes", Toast.LENGTH_SHORT).show()

            // Refresh the post list
            displayPosts()
        }
    }

    override fun onCommentClicked(post: Post, position: Int) {
        // In the future, this would navigate to a comment screen or show a comment dialog
        Toast.makeText(requireContext(), "Comments feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun onBookmarkClicked(post: Post, position: Int) {
        // Handle bookmark/save functionality
        Toast.makeText(requireContext(), "Post saved!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}