package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.User

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        setupNavigationListeners()
    }

    private fun setupUI() {
        // Get the current user from DataManager
        val currentUser = DataManager.getCurrentUser()

        // Get the current lesson the user is working on
        val currentLesson = DataManager.getCurrentLesson()

        // Set user greeting
        val tvGreeting: TextView = findViewById(R.id.tv_greeting)
        tvGreeting.text = getString(R.string.greeting, currentUser.name)

        // Set user streak
        val tvStreak: TextView = findViewById(R.id.tv_streak)
        tvStreak.text = getString(R.string.day_streak, currentUser.streakDays)

        // Set the goal description with current lesson title
        val tvGoalDescription: TextView = findViewById(R.id.tv_goal_description)
        tvGoalDescription.text = getString(R.string.complete_lesson_goal, currentLesson.title)

        // Set up the lesson card with actual lesson data
        val tvLessonTitle: TextView = findViewById(R.id.tv_lesson_title)
        tvLessonTitle.text = currentLesson.title

        // Set the lesson description from DataManager
        val tvLessonDescription: TextView = findViewById(R.id.tv_lesson_description)
        tvLessonDescription.text = currentLesson.description

        // Set lesson duration
        val tvLessonDuration: TextView = findViewById(R.id.tv_lesson_duration)
        tvLessonDuration.text = getString(R.string.min_lesson, currentLesson.durationMinutes)

        // Setup the action button based on lesson progress
        val btnAction: Button = findViewById(R.id.btn_start)

        if (DataManager.hasStartedLesson(currentLesson.id)) {
            btnAction.text = getString(R.string.continue_str)
        } else {
            btnAction.text = getString(R.string.start_str)
        }

        btnAction.setOnClickListener {
            // Create intent to navigate to the lesson
            val intent = Intent(this, LessonContentActivity::class.java)
            intent.putExtra("LESSON_ID", currentLesson.id)
            intent.putExtra("LESSON_TITLE", currentLesson.title)

            // Get the correct page based on progress
            val currentPage = if (DataManager.hasStartedLesson(currentLesson.id)) {
                DataManager.getLessonProgress(currentLesson.id)
            } else {
                1 // Start from the first page
            }

            intent.putExtra("CURRENT_PAGE", currentPage)
            intent.putExtra("TOTAL_PAGES", currentLesson.content.size)

            startActivity(intent)
        }

        // Setup goal completion checkbox
        val cbGoalCompleted: CheckBox = findViewById(R.id.cb_goal_completed)
        cbGoalCompleted.isChecked = DataManager.isGoalCompleted()

        cbGoalCompleted.setOnCheckedChangeListener { _, isChecked ->
            DataManager.setGoalCompleted(isChecked)
            if (isChecked) {
                // Perhaps update the user's streak or show a celebration animation
                Toast.makeText(this, "Goal completed! Great job!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigationListeners() {
        // Profile image click to navigate to profile
        val ivProfile: ImageView = findViewById(R.id.iv_profile)
        ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Add click listener for course progress (needs to be added to layout)
        // For now let's assume you have a button or card that leads to course progress
        val cardNextLesson: CardView = findViewById(R.id.card_next_lesson)
        cardNextLesson.setOnClickListener {
            val intent = Intent(this, CourseProgressActivity::class.java)
            intent.putExtra("COURSE_NAME", "Football Basics")
            startActivity(intent)
        }

        // Add community navigation (needs to be added to layout)
        // For demonstration, you could add this to your navigation menu or as a button
        // For example:
        /*
        val btnCommunity: Button = findViewById(R.id.btn_community)
        btnCommunity.setOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }
        */
    }
}