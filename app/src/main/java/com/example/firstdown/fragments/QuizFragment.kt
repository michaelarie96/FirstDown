// app/src/main/java/com/example/firstdown/fragments/QuizFragment.kt
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

    private lateinit var chapterId: String
    private lateinit var chapterTitle: String
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
        chapterId = args.chapterId
        chapterTitle = args.chapterTitle

        // Set quiz title
        binding.tvBackToLesson.text = "Back to $chapterTitle"
        binding.tvQuestionCounter.text = "Quiz"

        // Update progress indicator
        binding.progressBar.max = 100
        binding.progressBar.progress = 50  // Since we have a single quiz, show 50% progress

        // Display the quiz
        displayQuiz()
    }

    private fun displayQuiz() {
        // Get the quiz for this chapter
        val quiz = viewModel.getQuizForChapter(chapterId)

        if (quiz != null) {
            // Set question text
            binding.tvQuestion.text = quiz.question

            // Clear radio group selection
            binding.radioGroup.clearCheck()
            selectedAnswerIndex = -1

            // Set up options
            setupQuizOptions(quiz)
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
        val isCorrect = viewModel.isAnswerCorrect(chapterId, selectedAnswerIndex)

        // Calculate a score (100 for correct, 0 for incorrect)
        val score = if (isCorrect) 100 else 0

        // Show feedback
        val feedbackMessage = if (isCorrect) "Correct!" else "Incorrect!"
        Toast.makeText(requireContext(), feedbackMessage, Toast.LENGTH_SHORT).show()

        // Get current quiz for explanation
        val quiz = viewModel.getQuizForChapter(chapterId)

        // Mark quiz as completed
        viewModel.markQuizCompleted(chapterId, score)

        // Show completion message
        Toast.makeText(requireContext(), "Quiz completed! Your score: $score%", Toast.LENGTH_LONG).show()

        // Return to previous screen
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}