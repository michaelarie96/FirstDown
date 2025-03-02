package com.example.firstdown.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstdown.adapters.ChapterAdapter
import com.example.firstdown.databinding.FragmentCourseProgressBinding
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.Lesson
import com.example.firstdown.viewmodel.CourseProgressViewModel

class CourseProgressFragment : Fragment(), ChapterAdapter.ChapterClickListener {

    private var _binding: FragmentCourseProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CourseProgressViewModel by viewModels()
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var course: Course

    // Will be used to retrieve arguments passed to the fragment
    private val args: CourseProgressFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Get course ID from navigation arguments
        val courseId = args.courseId

        // Get course from ViewModel
        course = if (courseId.isNotEmpty()) {
            viewModel.getCourseById(courseId) ?: viewModel.getDefaultCourse()
        } else {
            viewModel.getDefaultCourse()
        }

        // Set the course name in the title
        binding.tvCourseTitle.text = course.title

        // Set progress values
        binding.tvProgressPercent.text = "${course.progress}% Complete"
        binding.progressBar.progress = course.progress

        // Setup RecyclerView and adapter
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvChapters.layoutManager = LinearLayoutManager(requireContext())
        chapterAdapter = ChapterAdapter(course.chapters, this)
        binding.rvChapters.adapter = chapterAdapter
    }

    private fun setupListeners() {
        // Setup back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Continue Learning button
        binding.btnContinueLearning.setOnClickListener {
            // Find the first non-completed chapter
            val nextChapter = course.chapters.firstOrNull { it.progress < 100 && !it.isLocked }

            if (nextChapter != null) {
                // Get the next lesson from the chapter
                val lesson = viewModel.getNextLesson(nextChapter)
                if (lesson != null) {
                    navigateToLesson(lesson)
                }
            } else {
                // If all lessons are complete, find a chapter with an incomplete quiz
                val quizChapter = course.chapters.firstOrNull {
                    !it.isLocked && it.progress == 100 && !it.quizCompleted
                }

                if (quizChapter != null) {
                    navigateToQuiz(quizChapter)
                } else {
                    // If everything is complete, go to the first chapter for review
                    val firstChapter = course.chapters.firstOrNull { !it.isLocked }
                    if (firstChapter != null) {
                        val lesson = firstChapter.lessons.firstOrNull()
                        if (lesson != null) {
                            navigateToLesson(lesson)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToLesson(lesson: Lesson) {
        val action = CourseProgressFragmentDirections.actionCourseProgressFragmentToLessonContentFragment(
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            currentPage = 1, // Start at the first page
            totalPages = lesson.content.size
        )
        findNavController().navigate(action)
    }

    override fun onChapterClicked(chapter: Chapter) {
        if (!chapter.isLocked) {
            if (chapter.progress == 100 && !chapter.quizCompleted) {
                // Chapter lessons complete but quiz not completed - go to quiz
                navigateToQuiz(chapter)
            } else {
                // Go to lesson
                val lesson = viewModel.getNextLesson(chapter)
                if (lesson != null) {
                    navigateToLesson(lesson)
                }
            }
        }
    }

    override fun onChapterQuizClicked(chapter: Chapter) {
        if (!chapter.isLocked) {
            navigateToQuiz(chapter)
        }
    }

    private fun navigateToQuiz(chapter: Chapter) {
        // Get the first lesson ID in the chapter to associate with quizzes
        // (assuming quizzes are linked to lessons in data model)
        val lessonId = chapter.lessons.firstOrNull()?.id ?: return

        val action = CourseProgressFragmentDirections.actionCourseProgressFragmentToQuizFragment(
            lessonId = lessonId,
            lessonTitle = chapter.title,
            quizIndex = 0
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}