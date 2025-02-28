package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdown.databinding.ActivityLessonContentBinding
import com.example.firstdown.model.LessonContent
import com.example.firstdown.viewmodel.LessonContentViewModel

class LessonContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonContentBinding
    private val viewModel: LessonContentViewModel by viewModels()

    // Lesson data
    private lateinit var lessonId: String
    private lateinit var lessonTitle: String
    private var currentPage: Int = 1
    private var totalPages: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent and Setup UI
        setupUI()

        // Setup listeners
        setupListeners()
    }

    private fun setupUI() {
        // Extract data from intent
        lessonId = intent.getStringExtra("LESSON_ID") ?: ""
        lessonTitle = intent.getStringExtra("LESSON_TITLE") ?: "Lesson"
        currentPage = intent.getIntExtra("CURRENT_PAGE", 1)

        // Get total pages from ViewModel or intent
        totalPages = intent.getIntExtra("TOTAL_PAGES",
            viewModel.getTotalPages(lessonId))

        // Update lesson progress in ViewModel
        viewModel.updateLessonProgress(lessonId, currentPage)

        // Set lesson title
        binding.tvLessonTitle.text = lessonTitle

        // Update progress information
        updateProgressUI()

        // Display lesson content for current page
        displayLessonContent()
    }

    private fun updateProgressUI() {
        // Update progress text
        binding.tvProgress.text = "$currentPage/$totalPages"

        // Update progress bar
        binding.progressBar.max = totalPages
        binding.progressBar.progress = currentPage
    }

    private fun displayLessonContent() {
        // Get content for current page
        val content = viewModel.getLessonContentForPage(lessonId, currentPage)

        // Display content based on type
        when (content) {
            is LessonContent.Text -> {
                // Display text content
                // Note: You'll need to adapt this based on your actual layout
                // binding.tvContentText.text = content.content
                // binding.tvContentText.visibility = View.VISIBLE
                // binding.ivContentImage.visibility = View.GONE
            }
            is LessonContent.Image -> {
                // Display image content
                // binding.ivContentImage.setImageResource(content.imageResId)
                // binding.ivContentImage.visibility = View.VISIBLE
                // binding.tvContentText.visibility = View.GONE
            }
            // Handle other content types as needed
            else -> {
                // Default case if needed
            }
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Previous button
        binding.btnPrevious.setOnClickListener {
            navigateToPreviousPage()
        }

        // Next button
        binding.btnNext.setOnClickListener {
            navigateToNextPage()
        }

        // Practice quiz button
        binding.btnPracticeQuiz.setOnClickListener {
            navigateToQuiz()
        }
    }

    private fun navigateToPreviousPage() {
        if (viewModel.isFirstPage(currentPage)) {
            // If at first page, go back to previous screen
            finish()
        } else {
            // Go to previous page
            currentPage--
            viewModel.updateLessonProgress(lessonId, currentPage)

            // Update UI
            updateProgressUI()
            displayLessonContent()
        }
    }

    private fun navigateToNextPage() {
        if (viewModel.isLastPage(lessonId, currentPage)) {
            // If this is the last page, we could either finish or go to quiz
            navigateToQuiz()
        } else {
            // Go to next page
            currentPage++
            viewModel.updateLessonProgress(lessonId, currentPage)

            // Update UI
            updateProgressUI()
            displayLessonContent()
        }
    }

    private fun navigateToQuiz() {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("LESSON_TITLE", lessonTitle)
        intent.putExtra("LESSON_ID", lessonId)
        startActivity(intent)
    }
}