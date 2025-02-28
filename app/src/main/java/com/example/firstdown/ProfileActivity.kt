package com.example.firstdown

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdown.databinding.ActivityProfileBinding
import com.example.firstdown.viewmodel.ProfileViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Get current user from ViewModel
        val currentUser = viewModel.getCurrentUser()

        // Set up user information
        binding.tvUserName.text = currentUser.name
        binding.tvUserTitle.text = currentUser.title

        // Set up statistics
        binding.tvLessonsCount.text = currentUser.lessonsCompleted.toString()
        binding.tvScorePercent.text = "${currentUser.quizScore}%"
        binding.tvTimeSpent.text = "${currentUser.timeSpent / 60}h"

        // Setup settings button
        binding.btnSettings.setOnClickListener {
            // Navigate to settings activity in a future step
        }
    }
}