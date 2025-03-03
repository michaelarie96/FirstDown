package com.example.firstdown.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.firstdown.R
import com.example.firstdown.databinding.FragmentHomeBinding
import com.example.firstdown.viewmodel.MainViewModel
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private var isNewUser = false

    companion object {
        var isNewUserStatic: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use the static variable instead of the argument bundle
        isNewUser = isNewUserStatic

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Get data from ViewModel
        val currentUser = viewModel.getCurrentUser()
        val currentLesson = viewModel.getCurrentLesson()

        // Set user information
        binding.tvGreeting.text = getString(R.string.greeting, currentUser.name)
        binding.tvStreak.text = getString(R.string.day_streak, currentUser.streakDays)

        // Set lesson information
        binding.tvGoalDescription.text = getString(R.string.complete_lesson_goal, currentLesson.title)
        binding.tvLessonTitle.text = currentLesson.title
        binding.tvLessonDescription.text = currentLesson.description
        binding.tvLessonDuration.text = getString(R.string.min_lesson, currentLesson.durationMinutes)

        // Set up button text based on lesson progress
        updateStartButtonText()

        // Set up checkbox state
        binding.cbGoalCompleted.isChecked = viewModel.isGoalCompleted()
    }

    private fun updateStartButtonText() {
        if (isNewUser) {
            binding.btnStart.text = getString(R.string.start_learning)
        } else {
            binding.btnStart.text = getString(R.string.continue_learning)
        }
    }

    private fun setupListeners() {
        // Get current lesson for navigation
        val currentLesson = viewModel.getCurrentLesson()

        // Navigation listeners
        binding.ivProfile.setOnClickListener {
            // In fragments, we use Navigation Component to navigate
            findNavController().navigate(R.id.navigation_profile)
        }

        // Interactive element listeners
        binding.btnStart.setOnClickListener {
            navigateToLesson(currentLesson.id, currentLesson.title)
        }

        binding.cbGoalCompleted.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setGoalCompleted(isChecked)
            if (isChecked) {
                Toast.makeText(requireContext(), "Goal completed! Great job!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLesson(lessonId: String, lessonTitle: String) {
        // Get the correct page based on progress
        val currentPage = if (viewModel.hasStartedLesson(lessonId)) {
            viewModel.getLessonProgress(lessonId)
        } else {
            1 // Start from the first page
        }

        // Use Navigation Component to navigate to the LessonContentFragment
        val totalPages = viewModel.getLessonContentSize(lessonId)
        val action = HomeFragmentDirections.actionNavigationHomeToLessonContentFragment(
            lessonId = lessonId,
            lessonTitle = lessonTitle,
            currentPage = currentPage,
            totalPages = totalPages
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}