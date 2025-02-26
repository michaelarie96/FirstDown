package com.example.firstdown

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.User

class ProfileActivity : AppCompatActivity() {

    private val currentUser = DataManager.getCurrentUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set up back button
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // Set up user information
        val tvUserName: TextView = findViewById(R.id.tv_user_name)
        tvUserName.text = currentUser.name

        val tvUserTitle: TextView = findViewById(R.id.tv_user_title)
        tvUserTitle.text = currentUser.title

        // Set up statistics
        val tvLessonsCount: TextView = findViewById(R.id.tv_lessons_count)
        tvLessonsCount.text = currentUser.lessonsCompleted.toString()

        val tvScorePercent: TextView = findViewById(R.id.tv_score_percent)
        tvScorePercent.text = "${currentUser.quizScore}%"

        val tvTimeSpent: TextView = findViewById(R.id.tv_time_spent)
        tvTimeSpent.text = "${currentUser.timeSpent / 60}h"

        // Setup settings button
        val btnSettings: ImageButton = findViewById(R.id.btn_settings)
        btnSettings.setOnClickListener {
            // Navigate to settings activity
            // This will be implemented in a future step
        }
    }
}