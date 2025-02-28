package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.example.firstdown.databinding.ActivityMainBinding
import com.example.firstdown.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        updateStartButtonText(currentLesson.id)

        // Set up checkbox state
        binding.cbGoalCompleted.isChecked = viewModel.isGoalCompleted()
    }

    private fun updateStartButtonText(lessonId: String) {
        if (viewModel.hasStartedLesson(lessonId)) {
            binding.btnStart.text = getString(R.string.continue_str)
        } else {
            binding.btnStart.text = getString(R.string.start_str)
        }
    }

    private fun setupListeners() {
        // Get current lesson for navigation
        val currentLesson = viewModel.getCurrentLesson()

        // Navigation listeners
        binding.ivProfile.setOnClickListener {
            navigateToActivity(ProfileActivity::class.java)
        }

        binding.cardNextLesson.setOnClickListener {
            val intent = Intent(this, CourseProgressActivity::class.java)
            intent.putExtra("COURSE_NAME", "Football Basics")
            startActivity(intent)
        }

        // Interactive element listeners
        binding.btnStart.setOnClickListener {
            navigateToLesson(currentLesson.id, currentLesson.title)
        }

        binding.cbGoalCompleted.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setGoalCompleted(isChecked)
            if (isChecked) {
                Toast.makeText(this, "Goal completed! Great job!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLesson(lessonId: String, lessonTitle: String) {
        // Create intent to navigate to the lesson
        val intent = Intent(this, LessonContentActivity::class.java)
        intent.putExtra("LESSON_ID", lessonId)
        intent.putExtra("LESSON_TITLE", lessonTitle)

        // Get the correct page based on progress
        val currentPage = if (viewModel.hasStartedLesson(lessonId)) {
            viewModel.getLessonProgress(lessonId)
        } else {
            1 // Start from the first page
        }

        intent.putExtra("CURRENT_PAGE", currentPage)
        intent.putExtra("TOTAL_PAGES", viewModel.getLessonContentSize(lessonId))

        startActivity(intent)
    }

    private fun navigateToActivity(activityClass: Class<*>, extras: Map<String, Any>? = null) {
        val intent = Intent(this, activityClass)
        extras?.forEach { (key, value) ->
            when (value) {
                is String -> intent.putExtra(key, value)
                is Int -> intent.putExtra(key, value)
                is Boolean -> intent.putExtra(key, value)
                // Add other types as needed
            }
        }
        startActivity(intent)
    }
}