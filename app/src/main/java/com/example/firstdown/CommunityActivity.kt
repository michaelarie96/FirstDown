package com.example.firstdown

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.model.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class CommunityActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var searchEditText: EditText
    private lateinit var fabNewPost: FloatingActionButton

    // Sample posts data - in a real app, this would come from a database or API
    private val posts = listOf(
        Post(
            id = "post1",
            userProfileImage = "https://placeholder.com/user1",
            userName = "John Cooper",
            timeAgo = "2 hours ago",
            content = "What's the best way to improve ball control during high-pressure situations? Any drills recommendations?",
            imageUrl = null,
            likes = 24,
            comments = 8
        ),
        Post(
            id = "post2",
            userProfileImage = "https://placeholder.com/user2",
            userName = "Sarah Wilson",
            timeAgo = "5 hours ago",
            content = "Pro tip: Here's a quick drill for improving your first touch. Practice this for 15 minutes daily.",
            imageUrl = "https://placeholder.com/football-drill",
            likes = 56,
            comments = 12
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        // Initialize views
        tabLayout = findViewById(R.id.tab_layout)
        searchEditText = findViewById(R.id.et_search)
        fabNewPost = findViewById(R.id.fab_new_post)

        // Set up the recycler view
        setupPostsRecyclerView()

        // Set up tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Filter posts based on tab selection
                // For now, we'll just keep showing all posts
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Set up FAB click listener
        fabNewPost.setOnClickListener {
            // Open new post creation screen
            // This will be implemented in a future step
        }
    }

    private fun setupPostsRecyclerView() {
        // In a real app, you would implement a RecyclerView adapter for the posts
        // For simplicity, we'll just add the posts directly to the layout

        val postContainer = findViewById<CardView>(R.id.card_post1)
        val tvUserName = postContainer.findViewById<TextView>(R.id.comm_tv_user_name)
        val tvTimeAgo = postContainer.findViewById<TextView>(R.id.tv_time_ago)
        val tvContent = postContainer.findViewById<TextView>(R.id.tv_post_content)
        val tvLikes = postContainer.findViewById<TextView>(R.id.tv_likes)
        val tvComments = postContainer.findViewById<TextView>(R.id.tv_comments)

        // Set post 1 data
        tvUserName.text = posts[0].userName
        tvTimeAgo.text = posts[0].timeAgo
        tvContent.text = posts[0].content
        tvLikes.text = posts[0].likes.toString()
        tvComments.text = posts[0].comments.toString()

        // Set post 2 data
        val postContainer2 = findViewById<CardView>(R.id.card_post2)
        val tvUserName2 = postContainer2.findViewById<TextView>(R.id.comm_tv_user_name)
        val tvTimeAgo2 = postContainer2.findViewById<TextView>(R.id.tv_time_ago)
        val tvContent2 = postContainer2.findViewById<TextView>(R.id.tv_post_content)
        val tvLikes2 = postContainer2.findViewById<TextView>(R.id.tv_likes)
        val tvComments2 = postContainer2.findViewById<TextView>(R.id.tv_comments)

        tvUserName2.text = posts[1].userName
        tvTimeAgo2.text = posts[1].timeAgo
        tvContent2.text = posts[1].content
        tvLikes2.text = posts[1].likes.toString()
        tvComments2.text = posts[1].comments.toString()
    }
}