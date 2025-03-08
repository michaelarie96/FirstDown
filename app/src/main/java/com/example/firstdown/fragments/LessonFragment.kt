package com.example.firstdown.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.firstdown.R
import com.example.firstdown.databinding.FragmentLessonBinding
import com.example.firstdown.model.ContentType
import com.example.firstdown.model.Lesson
import com.example.firstdown.viewmodel.LessonViewModel

class LessonFragment : Fragment() {

    private var _binding: FragmentLessonBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonViewModel by viewModels()

    // Get arguments using Safe Args
    private val args: LessonFragmentArgs by navArgs()

    // Lesson ID
    private lateinit var lessonId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonBinding.inflate(inflater, container, false)
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

        // Get lesson data
        val lesson = viewModel.getLessonById(lessonId)
        if (lesson == null) {
            Toast.makeText(context, "Error loading lesson", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        // Set lesson title
        binding.tvLessonTitle.text = lesson.title

        // Display lesson content
        displayLessonContent(lesson)
    }

    private fun displayLessonContent(lesson: Lesson) {
        // Get content data
        val (contentText, imageResId) = viewModel.getLessonContent(lesson.id)

        // Clear previous content
        binding.lessonContentLayout.removeAllViews()

        // Display content based on type
        when (lesson.contentType) {
            ContentType.TEXT -> {
                // Display text content
                val textView = TextView(requireContext()).apply {
                    text = contentText
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                binding.lessonContentLayout.addView(textView)
            }
            ContentType.IMAGE -> {
                // Display text and image content
                val textView = TextView(requireContext()).apply {
                    text = contentText
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val imageView = ImageView(requireContext()).apply {
                    if (imageResId != null) {
                        setImageResource(imageResId)
                    }
                    contentDescription = "Lesson image"
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        500 // Height in pixels
                    ).apply {
                        setMargins(0, 16, 0, 16) // Margins in pixels
                    }
                }

                binding.lessonContentLayout.addView(textView)
                binding.lessonContentLayout.addView(imageView)
            }
            ContentType.VIDEO -> {
                // Handle video content (placeholder for now)
                val textView = TextView(requireContext()).apply {
                    text = "Video content: $contentText"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
                binding.lessonContentLayout.addView(textView)
            }
            ContentType.INTERACTIVE -> {
                // Handle interactive content (placeholder for now)
                val textView = TextView(requireContext()).apply {
                    text = "Interactive content: $contentText"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
                binding.lessonContentLayout.addView(textView)
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
            navigateToPreviousLesson()
        }

        // Next button
        binding.btnNext.setOnClickListener {
            navigateToNextLesson()
        }
    }

    private fun navigateToPreviousLesson() {
        val previousLesson = viewModel.getPreviousLesson(lessonId)

        if (previousLesson != null) {
            // Navigate to previous lesson
            val action = LessonFragmentDirections.actionLessonFragmentSelf(
                lessonId = previousLesson.id
            )
            findNavController().navigate(action)
        } else {
            // If no previous lesson, go back to chapter list
            findNavController().navigateUp()
        }
    }

    private fun navigateToNextLesson() {
        // Mark current lesson as completed
        viewModel.markLessonComplete(lessonId)

        // Check if this is the last lesson in the chapter
        if (viewModel.isLastLessonInChapter(lessonId)) {
            // If it's the last lesson, navigate to the chapter quiz
            val chapter = viewModel.getChapterForLesson(lessonId)
            if (chapter?.quiz != null) {
                navigateToQuiz()
            } else {
                // If there's no quiz, go back to chapter list
                findNavController().navigateUp()
            }
        } else {
            // If not the last lesson, go to the next lesson
            val nextLesson = viewModel.getNextLesson(lessonId)
            if (nextLesson != null) {
                // Navigate to next lesson
                val action = LessonFragmentDirections.actionLessonFragmentSelf(
                    lessonId = nextLesson.id
                )
                findNavController().navigate(action)
            } else {
                // If no next lesson (unusual case), go back to chapter list
                findNavController().navigateUp()
            }
        }
    }
    private fun navigateToQuiz() {
        // Find which chapter this lesson belongs to
        val chapter = viewModel.getChapterForLesson(lessonId)

        if (chapter != null) {
            // Navigate to the chapter's quiz if it exists
            if (chapter.quiz != null) {
                val action = LessonFragmentDirections.actionLessonFragmentToQuizFragment(
                    chapterId = chapter.id,
                    chapterTitle = chapter.title
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "No quiz available for this chapter", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        } else {
            // Handle case where parent chapter couldn't be found
            Toast.makeText(requireContext(), "Quiz not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}