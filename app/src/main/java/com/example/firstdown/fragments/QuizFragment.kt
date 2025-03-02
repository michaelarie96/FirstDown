package com.example.firstdown.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firstdown.databinding.FragmentQuizBinding
import com.example.firstdown.model.Quiz
import com.example.firstdown.viewmodel.QuizViewModel

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by viewModels()
    private val args: QuizFragmentArgs by navArgs()

    private lateinit var lessonId: String
    private lateinit var lessonTitle: String
    private var currentQuizIndex: Int = 0
    private var totalQuizzes: Int = 0
    private var selectedAnswerIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupListeners()
    }

    private fun setupUI() {
        // Extract data from arguments
        lessonId = args.lessonId
        lessonTitle = args.lessonTitle
        currentQuizIndex = args.quizIndex

        // Get total number of quizzes
        totalQuizzes = viewModel.getTotalQuizCount(lessonId)

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
            Toast.makeText(requireContext(), "Could not load quiz", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
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
                radioButtons[index].visibility = View.VISIBLE
            }
        }

        // Disable any unused radio buttons
        if (quiz.options.size < radioButtons.size) {
            for (i in quiz.options.size until radioButtons.size) {
                radioButtons[i].isEnabled = false
                radioButtons[i].visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Radio group listener
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.radioOption1.id -> selectedAnswerIndex = 0
                binding.radioOption2.id -> selectedAnswerIndex = 1
                binding.radioOption3.id -> selectedAnswerIndex = 2
                binding.radioOption4.id -> selectedAnswerIndex = 3
            }

            // Enable submit button when an option is selected
            binding.btnSubmitAnswer.isEnabled = selectedAnswerIndex != -1
        }

        // Submit answer button
        binding.btnSubmitAnswer.setOnClickListener {
            if (selectedAnswerIndex != -1) {
                handleAnswerSubmission()
            } else {
                Toast.makeText(requireContext(), "Please select an answer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAnswerSubmission() {
        // Check if answer is correct
        val isCorrect = viewModel.isAnswerCorrect(lessonId, currentQuizIndex, selectedAnswerIndex)

        // Show feedback
        val feedbackMessage = if (isCorrect) "Correct!" else "Incorrect!"
        Toast.makeText(requireContext(), feedbackMessage, Toast.LENGTH_SHORT).show()

        // Get current quiz for explanation
        val currentQuiz = viewModel.getQuizByIndex(lessonId, currentQuizIndex)

        // Move to next question or finish quiz
        if (viewModel.isLastQuiz(lessonId, currentQuizIndex)) {
            // This was the last question, show completion message
            Toast.makeText(requireContext(), "Quiz completed!", Toast.LENGTH_LONG).show()

            // Return to previous screen or navigate to results
            findNavController().navigateUp()
        } else {
            // Move to next question
            currentQuizIndex++
            updateProgressIndicators()
            displayCurrentQuiz()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}