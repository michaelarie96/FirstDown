package com.example.firstdown.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.firstdown.databinding.FragmentProfileBinding
import com.example.firstdown.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Get current user from ViewModel
        val currentUser = viewModel.getCurrentUser()

        // Set up user information
        binding.tvUserName.text = currentUser.name
        binding.tvUserTitle.text = currentUser.title

        // Set up statistics
        binding.tvLessonsCount.text = currentUser.lessonsCompleted.toString()
        binding.tvScorePercent.text = "${currentUser.quizScore}%"
        binding.tvTimeSpent.text = "${currentUser.timeSpent / 60}h"
    }

    private fun setupListeners() {
        // Setup settings button
        binding.btnSettings.setOnClickListener {
            // In the future, this would navigate to a settings screen
            // For now, we can show a toast message
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Settings")
                .setMessage("Settings feature will be implemented in a future update.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}