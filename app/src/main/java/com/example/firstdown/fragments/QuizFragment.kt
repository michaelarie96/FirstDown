package com.example.firstdown.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firstdown.R
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
        chapterId = args.chapterId
        chapterTitle = args.chapterTitle

        binding.tvBackToLesson.text = "Back to $chapterTitle"
        binding.tvQuestionCounter.text = "Quiz"

        binding.progressBar.max = 100
        binding.progressBar.progress = 50  // Since we have a single quiz, show 50% progress

        displayQuiz()
    }

    private fun displayQuiz() {
        Log.d("QuizFragment", "Attempting to display quiz for chapter: $chapterId")

        viewModel.getQuizForChapter(chapterId) { quiz ->
            Log.d("QuizFragment", "Quiz loaded for chapter $chapterId: ${quiz != null}")

            if (quiz != null) {
                binding.tvQuestion.text = quiz.question
                binding.radioGroup.clearCheck()
                selectedAnswerIndex = -1
                setupQuizOptions(quiz)
            } else {
                Log.e("QuizFragment", "Could not load quiz for chapter $chapterId")

                // Show a more descriptive error message
                binding.tvQuestion.text = "Error loading quiz. Please try again later."
                binding.radioGroup.visibility = View.GONE
                binding.btnSubmitAnswer.isEnabled = false

                Toast.makeText(requireContext(),
                    "Could not load quiz. Please try again later.",
                    Toast.LENGTH_LONG).show()

                // Log additional diagnostic info
                viewModel.getChapterById(chapterId) { chapter ->
                    Log.d("QuizFragment", "Chapter details - ID: ${chapter?.id}, " +
                            "Title: ${chapter?.title}, Has quiz: ${chapter?.quiz != null}")
                }
            }
        }
    }

    private fun setupQuizOptions(quiz: Quiz) {
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
            findNavController().popBackStack(R.id.ChaptersFragment, false)
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
        viewModel.isAnswerCorrect(chapterId, selectedAnswerIndex) { isCorrect ->
            val score = if (isCorrect) 100 else 0

            val feedbackMessage = if (isCorrect) "Correct!" else "Incorrect!"
            Toast.makeText(requireContext(), feedbackMessage, Toast.LENGTH_SHORT).show()

            // Mark quiz as completed
            viewModel.markQuizCompleted(chapterId, score) {
                Toast.makeText(requireContext(), "Quiz completed! Your score: $score%", Toast.LENGTH_LONG).show()

                // Get the course ID for this chapter
                viewModel.getChapterById(chapterId) { chapter ->
                    val courseId = chapter?.courseId ?: ""

                    // Create bundle with course ID
                    val bundle = Bundle().apply {
                        putString("courseId", courseId)
                    }

                    // Return to chapters screen with the correct course ID
                    findNavController().popBackStack(R.id.ChaptersFragment, false)

                    // Force refresh with the course ID
                    findNavController().currentDestination?.let { destination ->
                        if (destination.id == R.id.ChaptersFragment) {
                            findNavController().navigate(
                                R.id.ChaptersFragment,
                                bundle,
                                NavOptions.Builder()
                                    .setPopUpTo(R.id.ChaptersFragment, true)
                                    .build()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}