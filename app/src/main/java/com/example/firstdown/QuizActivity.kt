package com.example.firstdown

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdown.databinding.ActivityQuizBinding
import com.example.firstdown.model.Quiz
import com.example.firstdown.viewmodel.QuizViewModel

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private val viewModel: QuizViewModel by viewModels()

    private lateinit var lessonId: String
    private lateinit var lessonTitle: String
    private var currentQuizIndex: Int = 0
    private var totalQuizzes: Int = 0
    private var selectedAnswerIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup UI
        setupUI()

        // Setup listeners
        setupListeners()
    }

    private fun setupUI() {
        // Extract data from intent
        lessonId = intent.getStringExtra("LESSON_ID") ?: ""
        lessonTitle = intent.getStringExtra("LESSON_TITLE") ?: "Quiz"

        // Get total number of quizzes
        totalQuizzes = viewModel.getTotalQuizCount(lessonId)

        // Set current quiz index (default to 0)
        currentQuizIndex = intent.getIntExtra("QUIZ_INDEX", 0)

        // Set quiz title and progress
        binding.tvBackToLesson.text = "Back to $lessonTitle"
        updateProgressIndicators()

        // Display current quiz question and options
        displayCurrentQuiz()
    }

    private fun updateProgressIndicators() {
        // Update question counter text
        binding.tvQuestionCounter.text = "Question ${currentQuizIndex + 1}/$totalQuizzes"

        // Update progress bar
        binding.progressBar.max = 100
        binding.progressBar.progress = ((currentQuizIndex + 1) * 100) / totalQuizzes
    }

    private fun displayCurrentQuiz() {
        // Get the current quiz
        val currentQuiz = viewModel.getQuizByIndex(lessonId, currentQuizIndex)

        if (currentQuiz != null) {
            // Set question text
            binding.tvQuestion.text = currentQuiz.question

            // Clear radio group selection
            binding.radioGroup.clearCheck()
            selectedAnswerIndex = -1

            // Set up options
            setupQuizOptions(currentQuiz)
        } else {
            // Handle case where quiz couldn't be loaded
            Toast.makeText(this, "Could not load quiz", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupQuizOptions(quiz: Quiz) {
        // Get all radio buttons
        val radioButtons = listOf(
            binding.radioOption1,
            binding.radioOption2,
            binding.radioOption3,
            binding.radioOption4
        )

        // Set text for each option
        quiz.options.forEachIndexed { index, option ->
            if (index < radioButtons.size) {
                radioButtons[index].text = option
                radioButtons[index].isEnabled = true
            }
        }

        // Disable any unused radio buttons
        if (quiz.options.size < radioButtons.size) {
            for (i in quiz.options.size until radioButtons.size) {
                radioButtons[i].isEnabled = false
                radioButtons[i].visibility = RadioButton.GONE
            }
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Radio group listener
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_option1 -> selectedAnswerIndex = 0
                R.id.radio_option2 -> selectedAnswerIndex = 1
                R.id.radio_option3 -> selectedAnswerIndex = 2
                R.id.radio_option4 -> selectedAnswerIndex = 3
            }

            // Enable submit button when an option is selected
            binding.btnSubmitAnswer.isEnabled = selectedAnswerIndex != -1
        }

        // Submit answer button
        binding.btnSubmitAnswer.setOnClickListener {
            if (selectedAnswerIndex != -1) {
                handleAnswerSubmission()
            } else {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAnswerSubmission() {
        // Check if answer is correct
        val isCorrect = viewModel.isAnswerCorrect(lessonId, currentQuizIndex, selectedAnswerIndex)

        // Show feedback
        val feedbackMessage = if (isCorrect) "Correct!" else "Incorrect!"
        Toast.makeText(this, feedbackMessage, Toast.LENGTH_SHORT).show()

        // Get current quiz for explanation
        val currentQuiz = viewModel.getQuizByIndex(lessonId, currentQuizIndex)

        // You could display an explanation here if desired
        // binding.tvExplanation.text = currentQuiz?.explanation
        // binding.tvExplanation.visibility = View.VISIBLE

        // Move to next question or finish quiz
        if (viewModel.isLastQuiz(lessonId, currentQuizIndex)) {
            // This was the last question, show completion message
            Toast.makeText(this, "Quiz completed!", Toast.LENGTH_LONG).show()

            // Return to lesson or show results
            // You could navigate to a results screen here
            finish()
        } else {
            // Move to next question
            currentQuizIndex++
            updateProgressIndicators()
            displayCurrentQuiz()
        }
    }
}