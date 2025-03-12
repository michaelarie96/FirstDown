package com.example.firstdown.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.R
import com.example.firstdown.adapters.NotificationDropdownAdapter
import com.example.firstdown.adapters.PostAdapter
import com.example.firstdown.databinding.FragmentCommunityBinding
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Notification
import com.example.firstdown.model.Post
import com.example.firstdown.utilities.TimeUtils
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

    private var currentTabPosition = 0

    private var popupWindow: PopupWindow? = null
    private lateinit var notificationAdapter: NotificationDropdownAdapter
    private var currentUserName: String = ""

    private val notificationHandler = Handler(Looper.getMainLooper())
    private val notificationRunnable = object : Runnable {
        override fun run() {
            checkNotifications()
            notificationHandler.postDelayed(this, 5000) // Check every 5 seconds
        }
    }

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

        currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"
        Log.d("CommunityFragment", "Current user name: $currentUserName")

        setupUI()
        setupListeners()
        displayPosts()
        checkNotifications()

        // Start polling as a fallback
        startNotificationPolling()
        startPostsRefresh()
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

        // Notifications button
        binding.btnNotifications.setOnClickListener {
            if (popupWindow?.isShowing == true) {
                popupWindow?.dismiss()
            } else {
                showNotificationsPopup()
            }
        }

        // Real-time notifications listener
        if (currentUserName.isNotEmpty() && currentUserName != "Anonymous") {
            Log.d("CommunityFragment", "Setting up notifications listener for: $currentUserName")
            DataManager.setupNotificationsListener(currentUserName) { notifications ->
                Log.d("CommunityFragment", "Received notification update. Unread count: ${notifications.count { !it.isRead }}")
                val unreadCount = notifications.count { !it.isRead }
                activity?.runOnUiThread {
                    binding.notificationIndicator.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE

                    // If notification popup is visible, update it
                    if (popupWindow?.isShowing == true) {
                        notificationAdapter.updateNotifications(notifications)
                    }
                }
            }
        } else {
            Log.d("CommunityFragment", "Not setting up notifications listener. currentUserName: $currentUserName")
        }
    }

    private fun checkNotifications() {
        if (currentUserName.isNotEmpty() && currentUserName != "Anonymous") {
            DataManager.getUserNotifications(currentUserName) { notifications ->
                val unreadCount = notifications.count { !it.isRead }

                // Update UI on main thread
                activity?.runOnUiThread {
                    binding.notificationIndicator.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showNotificationsPopup() {
        logNotificationStatus("Loading notifications for popup")

        // Create popup window
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.dropdown_notification, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 10f
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        // Setup adapter and recycler view
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.rv_notifications)
        val emptyView = popupView.findViewById<TextView>(R.id.tv_empty_state)
        val markAllReadBtn = popupView.findViewById<TextView>(R.id.tv_mark_all_read)

        notificationAdapter = NotificationDropdownAdapter(emptyList(), object : NotificationDropdownAdapter.NotificationClickListener {
            override fun onNotificationClicked(notification: Notification, position: Int) {
                handleNotificationClick(notification)
            }
        })

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }

        // Mark all read button
        markAllReadBtn.setOnClickListener {
            markAllNotificationsAsRead()
        }

        // Load notifications
        DataManager.getUserNotifications(currentUserName) { notifications ->
            logNotificationStatus("Loading notifications for popup")
            if (notifications.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                notificationAdapter.updateNotifications(notifications)
            }

            // Update notification indicator
            val unreadCount = notifications.count { !it.isRead }
            binding.notificationIndicator.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
        }

        // Show popup below the notifications button
        popupWindow?.showAsDropDown(binding.btnNotifications, -250, 0)
    }

    private fun handleNotificationClick(notification: Notification) {
        logNotificationStatus("Before marking notification ${notification.id} as read")

        // Mark notification as read
        DataManager.markNotificationAsRead(notification.id) { success ->
            if (success) {
                logNotificationStatus("After marking notification ${notification.id} as read")

                // After marking as read, completely reload notification data from source
                activity?.runOnUiThread {
                    // Force a complete refresh of notifications
                    DataManager.getUserNotifications(currentUserName) { freshNotifications ->
                        // Update adapter with completely fresh data
                        notificationAdapter.updateNotifications(freshNotifications)

                        // Update notification indicator
                        val unreadCount = freshNotifications.count { !it.isRead }
                        binding.notificationIndicator.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun markAllNotificationsAsRead() {
        logNotificationStatus("Before marking all notifications as read")

        DataManager.getUserNotifications(currentUserName) { notifications ->
            val unreadNotifications = notifications.filter { !it.isRead }

            if (unreadNotifications.isNotEmpty()) {
                var processedCount = 0

                for (notification in unreadNotifications) {
                    DataManager.markNotificationAsRead(notification.id) { success ->
                        processedCount++

                        if (processedCount == unreadNotifications.size) {
                            logNotificationStatus("After marking all notifications as read")

                            // Final refresh after all are processed
                            activity?.runOnUiThread {
                                // Force a complete refresh from server
                                DataManager.getUserNotifications(currentUserName) { updatedNotifications ->
                                    notificationAdapter.updateNotifications(updatedNotifications)
                                    binding.notificationIndicator.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
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

        // Create a new post with unique ID and timestamp
        val postId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val newPost = Post(
            id = postId,
            userProfileImage = userProfileImage,
            userName = userName,
            timeAgo = "Just now",
            content = content,
            imageUrl = null,
            likes = 0,
            comments = 0,
            likedByCurrentUser = false,
            timestamp = timestamp // Add timestamp here
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

            // Set up listeners for all visible posts
            allPosts.forEach { post ->
                DataManager.setupPostListener(post.id) { updatedPost ->
                    if (updatedPost != null) {
                        // Find and update the post in the adapter
                        val currentPosts = postAdapter.getCurrentPosts()
                        val updatedPosts = currentPosts.map {
                            if (it.id == updatedPost.id) updatedPost.copy(likedByCurrentUser = it.likedByCurrentUser) else it
                        }
                        postAdapter.updatePosts(updatedPosts)
                    }
                }
            }
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

    private fun startNotificationPolling() {
        notificationHandler.postDelayed(object : Runnable {
            override fun run() {
                // Use regular (non-listener) method to fetch notifications
                DataManager.getUserNotifications(currentUserName) { notifications ->
                    val unreadCount = notifications.count { !it.isRead }
                    activity?.runOnUiThread {
                        binding.notificationIndicator.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE

                        // If notification popup is visible, update it
                        if (popupWindow?.isShowing == true) {
                            notificationAdapter.updateNotifications(notifications)
                        }
                    }
                }

                // Continue polling
                notificationHandler.postDelayed(this, 5000) // Poll every 5 seconds
            }
        }, 3000) // Start after 3 seconds
    }

    private fun startPostsRefresh() {
        notificationHandler.postDelayed(object : Runnable {
            override fun run() {
                // Refresh posts list but also update timeAgo strings
                viewModel.getAllPosts { allPosts ->
                    val updatedPosts = allPosts.map { post ->
                        // Keep everything the same but update the timeAgo
                        post.copy(timeAgo = TimeUtils.getTimeAgo(post.timestamp))
                    }
                    displayFilteredPosts(updatedPosts)
                }

                // Continue refreshing
                notificationHandler.postDelayed(this, 60000) // Refresh every minute
            }
        }, 60000) // Start after a minute
    }

    private fun logNotificationStatus(message: String) {
        DataManager.getUserNotifications(currentUserName) { notifications ->
            val unreadCount = notifications.count { !it.isRead }
            val totalCount = notifications.size
            Log.d("NotificationDebug", "$message - Total: $totalCount, Unread: $unreadCount")

            // Log details of each notification
            notifications.forEachIndexed { index, notif ->
                Log.d("NotificationDebug", "[$index] ID: ${notif.id}, Read: ${notif.isRead}, Message: ${notif.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Start notification checks
        notificationHandler.post(notificationRunnable)

        // Refresh posts when returning to fragment
        displayPosts()
    }

    override fun onPause() {
        super.onPause()

        // Stop checking for notifications when leaving fragment
        notificationHandler.removeCallbacks(notificationRunnable)

        popupWindow?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Clean up all listeners
        DataManager.clearAllListeners()

        // Remove all callbacks from the handler
        notificationHandler.removeCallbacksAndMessages(null)

        _binding = null
    }
}