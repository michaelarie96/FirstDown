// File: app/src/main/java/com/example/firstdown/CourseProgressActivity.kt
package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdown.databinding.ActivityCourseProgressBinding
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.Lesson
import com.example.firstdown.viewmodel.CourseProgressViewModel

class CourseProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseProgressBinding
    private val viewModel: CourseProgressViewModel by viewModels()
    private lateinit var course: Course

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the UI and Load the course data
        setupUI()

        // Set up navigation and button click listeners
        setupListeners()
    }

    private fun setupUI() {
        // Get course ID from intent
        val courseId = intent.getStringExtra("COURSE_ID")

        // Get course from ViewModel
        course = if (courseId != null) {
            viewModel.getCourseById(courseId) ?: viewModel.getDefaultCourse()
        } else {
            viewModel.getDefaultCourse()
        }

        // Get the course name from intent extras or from the course object
        val courseName = intent.getStringExtra("COURSE_NAME") ?: course.title

        // Set the course name in the title
        binding.tvCourseTitle.text = courseName

        // Set progress values
        binding.tvProgressPercent.text = "${course.progress}% Complete"
        binding.progressBar.progress = course.progress

        // Setup chapter cards using the chapters from the course
        setupChapterCards(course.chapters)
    }

    private fun setupListeners() {
        // Setup back button
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, CoursesActivity::class.java)
            // Use FLAG_ACTIVITY_CLEAR_TOP to avoid creating multiple instances
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        // Setup Continue Learning button
        binding.btnContinueLearning.setOnClickListener {
            // Find the first non-completed chapter
            val nextChapter = course.chapters.firstOrNull { it.progress < 100 && !it.isLocked }
            if (nextChapter != null) {
                // Get the first lesson from the chapter
                val lesson = viewModel.getFirstLesson(nextChapter)
                if (lesson != null) {
                    navigateToLesson(lesson)
                }
            }
        }

        // Setup chapter continuation buttons
        setupChapterButtons()
    }

    private fun setupChapterCards(chapters: List<Chapter>) {
        // Implementation depends on your specific layout
        // This would add or update chapter card views based on the chapters list
    }

    private fun navigateToLesson(lesson: Lesson) {
        val intent = Intent(this, LessonContentActivity::class.java)
        intent.putExtra("LESSON_ID", lesson.id)
        intent.putExtra("LESSON_TITLE", lesson.title)
        intent.putExtra("CURRENT_PAGE", 1) // Start at the first page
        intent.putExtra("TOTAL_PAGES", lesson.content.size)
        startActivity(intent)
    }

    private fun setupChapterButtons() {
        binding.btnContinueChapter.setOnClickListener {
            // Navigate to the Basic Rules chapter
            val chapter = viewModel.getChapterById("basic-rules")
            if (chapter != null) {
                val lesson = viewModel.getFirstLesson(chapter)
                if (lesson != null) {
                    navigateToLesson(lesson)
                }
            }
        }

        binding.btnResumeChapter.setOnClickListener {
            // Navigate to the Player Positions chapter
            val chapter = viewModel.getChapterById("player-positions")
            if (chapter != null) {
                val lesson = viewModel.getFirstLesson(chapter)
                if (lesson != null) {
                    navigateToLesson(lesson)
                }
            }
        }

        binding.btnStartChapter.setOnClickListener {
            // Navigate to the Game Flow chapter
            val chapter = viewModel.getChapterById("game-flow")
            if (chapter != null) {
                val lesson = viewModel.getFirstLesson(chapter)
                if (lesson != null) {
                    navigateToLesson(lesson)
                }
            }
        }
    }
}