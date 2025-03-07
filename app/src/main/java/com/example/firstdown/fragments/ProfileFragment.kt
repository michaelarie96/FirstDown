package com.example.firstdown.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.firstdown.R
import com.example.firstdown.WelcomeActivity
import com.example.firstdown.databinding.FragmentProfileBinding
import com.example.firstdown.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

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
        setupSignOut()
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

    private val startForWelcomeActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Signed out successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSignOut() {
        binding.btnSettings.setOnClickListener {
            // Show a popup menu with options
            val popupMenu = PopupMenu(requireContext(), binding.btnSettings)
            popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_sign_out -> {
                        signOut()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        Log.d("ProfileFragment", "signOut: User signed out.")
        // Navigate back to WelcomeActivity
        val intent = Intent(requireActivity(), WelcomeActivity::class.java)
        startForWelcomeActivity.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}