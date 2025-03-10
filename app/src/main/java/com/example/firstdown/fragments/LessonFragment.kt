package com.example.firstdown.fragments

import android.os.Bundle
import android.util.Log
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
        lessonId = args.lessonId

        // Try to get cached lesson for immediate display
        val cachedLesson = viewModel.getCachedLessonById(lessonId)
        if (cachedLesson != null) {
            binding.tvLessonTitle.text = cachedLesson.title
            displayLessonContent(cachedLesson)
        }

        // Always fetch the latest data
        viewModel.getLessonById(lessonId) { lesson ->
            if (lesson == null) {
                Toast.makeText(context, "Error loading lesson", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                return@getLessonById
            }

            binding.tvLessonTitle.text = lesson.title
            displayLessonContent(lesson)
        }
    }

    private fun displayLessonContent(lesson: Lesson) {
        viewModel.getLessonContent(lesson.id) { (contentText, imageResId) ->
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
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.ChaptersFragment, false)
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
        viewModel.getPreviousLesson(lessonId) { previousLesson ->
            if (previousLesson != null) {
                val action = LessonFragmentDirections.actionLessonFragmentSelf(
                    lessonId = previousLesson.id
                )
                findNavController().navigate(action)
            } else {
                // If no previous lesson, go back to chapter list
                findNavController().navigateUp()
            }
        }
    }

    private fun navigateToNextLesson() {
        // Mark current lesson as completed
        viewModel.markLessonComplete(lessonId)

        // Check if this is the last lesson in the chapter
        viewModel.isLastLessonInChapter(lessonId) { isLastLesson ->
            Log.d("LessonFragment", "isLastLessonInChapter result: $isLastLesson for lesson $lessonId")

            if (isLastLesson) {
                // Get the chapter quiz
                viewModel.getChapterForLesson(lessonId) { chapter ->
                    Log.d("LessonFragment", "Chapter for lesson: ${chapter?.id}, has quiz: ${chapter?.quiz != null}")

                    if (chapter?.quiz != null) {
                        navigateToQuiz()
                    } else {
                        // If there's no quiz (Shouldn't happen), go back to chapter list
                        Log.d("LessonFragment", "No quiz found for chapter ${chapter?.id}")
                        Toast.makeText(context, "No quiz available for this chapter", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            } else {
                // If not the last lesson, go to the next lesson
                viewModel.getNextLesson(lessonId) { nextLesson ->
                    Log.d("LessonFragment", "Next lesson: ${nextLesson?.id}")

                    if (nextLesson != null) {
                        // Navigate to next lesson
                        val action = LessonFragmentDirections.actionLessonFragmentSelf(
                            lessonId = nextLesson.id
                        )
                        findNavController().navigate(action)
                    } else {
                        // If no next lesson (Shouldn't happen), go back to chapter list
                        Log.d("LessonFragment", "No next lesson found")
                        Toast.makeText(context, "No next lesson found", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun navigateToQuiz() {
        viewModel.getChapterForLesson(lessonId) { chapter ->
            if (chapter != null) {
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
                Toast.makeText(requireContext(), "Quiz not available", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}