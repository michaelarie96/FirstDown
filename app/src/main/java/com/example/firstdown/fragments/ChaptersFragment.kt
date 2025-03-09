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
import com.example.firstdown.databinding.FragmentChaptersBinding
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.Lesson
import com.example.firstdown.viewmodel.ChaptersViewModel

class ChaptersFragment : Fragment(), ChapterAdapter.ChapterClickListener {

    private var _binding: FragmentChaptersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChaptersViewModel by viewModels()
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var course: Course

    // Will be used to retrieve arguments passed to the fragment
    private val args: ChaptersFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChaptersBinding.inflate(inflater, container, false)
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
        if (courseId.isNotEmpty()) {
            viewModel.getCourseById(courseId) { course ->
                if (course != null) {
                    setupCourseUI(course)
                } else {
                    viewModel.getDefaultCourse { defaultCourse ->
                        setupCourseUI(defaultCourse)
                    }
                }
            }
        } else {
            viewModel.getDefaultCourse { defaultCourse ->
                setupCourseUI(defaultCourse)
            }
        }
    }

    private fun setupCourseUI(course: Course) {
        this.course = course

        binding.tvCourseTitle.text = course.title

        binding.tvProgressPercent.text = "${course.progress}% Complete"
        binding.progressBar.progress = course.progress

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvChapters.layoutManager = LinearLayoutManager(requireContext())
        chapterAdapter = ChapterAdapter(course.chapters, this)
        binding.rvChapters.adapter = chapterAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Continue Learning button
        binding.btnContinueLearning.setOnClickListener {
            // Find the first non-completed chapter
            val nextChapter = course.chapters.firstOrNull { it.progress < 100 && !it.isLocked }

            if (nextChapter != null) {
                // Get the next lesson from the chapter
                viewModel.getNextLesson(nextChapter) { lesson ->
                    if (lesson != null) {
                        navigateToLesson(lesson)
                    }
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
        val action = ChaptersFragmentDirections.actionChaptersFragmentToLessonFragment(
            lessonId = lesson.id
        )
        findNavController().navigate(action)
    }

    override fun onChapterClicked(chapter: Chapter) {
        if (!chapter.isLocked) {
            if (chapter.progress == 100 && !chapter.quizCompleted) {
                // Chapter lessons complete but quiz not completed - go to quiz
                navigateToQuiz(chapter)
            } else {
                // Go to lesson with proper callback
                viewModel.getNextLesson(chapter) { lesson ->
                    if (lesson != null) {
                        navigateToLesson(lesson)
                    }
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
        if (!chapter.isLocked) {
            val action = ChaptersFragmentDirections.actionChaptersFragmentToQuizFragment(
                chapterId = chapter.id,
                chapterTitle = chapter.title
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}