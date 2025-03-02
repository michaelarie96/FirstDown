package com.example.firstdown.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firstdown.databinding.FragmentLessonContentBinding
import com.example.firstdown.model.LessonContent
import com.example.firstdown.viewmodel.LessonContentViewModel

class LessonContentFragment : Fragment() {

    private var _binding: FragmentLessonContentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonContentViewModel by viewModels()

    // Get arguments using Safe Args
    private val args: LessonContentFragmentArgs by navArgs()

    // Lesson data
    private lateinit var lessonId: String
    private lateinit var lessonTitle: String
    private var currentPage: Int = 1
    private var totalPages: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get data from arguments and setup UI
        setupUI()

        // Setup listeners
        setupListeners()
    }

    private fun setupUI() {
        // Extract data from arguments
        lessonId = args.lessonId
        lessonTitle = args.lessonTitle
        currentPage = args.currentPage
        totalPages = args.totalPages

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

        // Handle different content types (text, image, etc.)
        when (content) {
            is LessonContent.Text -> {
                // Display text content
                // Need to implement
            }
            is LessonContent.Image -> {
                // Display image content
                // Need to implement
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
            findNavController().navigateUp()
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
            findNavController().navigateUp()
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
        val action = LessonContentFragmentDirections.actionLessonContentFragmentToQuizFragment(
            lessonId = lessonId,
            lessonTitle = lessonTitle,
            quizIndex = 0 // Start with the first quiz
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}