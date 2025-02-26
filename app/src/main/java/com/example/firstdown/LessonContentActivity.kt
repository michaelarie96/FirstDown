package com.example.firstdown

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LessonContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_content)

        // Get lesson details from intent
        val lessonTitle = intent.getStringExtra("LESSON_TITLE") ?: "Basic Formations"
        val totalPages = intent.getIntExtra("TOTAL_PAGES", 10)
        val currentPage = intent.getIntExtra("CURRENT_PAGE", 4)

        // Set the lesson title
        val tvTitle: TextView = findViewById(R.id.tv_lesson_title)
        tvTitle.text = lessonTitle

        // Set progress text
        val tvProgress: TextView = findViewById(R.id.tv_progress)
        tvProgress.text = "$currentPage/$totalPages"

        // Setup back button
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // Setup navigation buttons
        val btnPrevious: Button = findViewById(R.id.btn_previous)
        btnPrevious.setOnClickListener {
            // Navigate to the previous page or lesson
            // For now, just go back if at first page
            if (currentPage <= 1) {
                finish()
            } else {
                // In a real app, you would load the previous page content
                // For now, we'll just finish the activity
                finish()
            }
        }

        val btnNext: Button = findViewById(R.id.btn_next)
        btnNext.setOnClickListener {
            // Navigate to the next page
            // In a real app, you would load the next page content or navigate to quiz
            // For now, we'll just finish and imagine going to the next page
            if (currentPage >= totalPages) {
                // If this is the last page, navigate to the quiz
                // For now, just finish
                finish()
            } else {
                // Load next page
                // For now, just finish
                finish()
            }
        }

        // Setup quiz button
        val btnPracticeQuiz: Button = findViewById(R.id.btn_practice_quiz)
        btnPracticeQuiz.setOnClickListener {
            // Navigate to the quiz activity
            // This will be implemented in a future step
            val intent = android.content.Intent(this, QuizActivity::class.java)
            intent.putExtra("LESSON_TITLE", lessonTitle)
            startActivity(intent)
        }
    }
}