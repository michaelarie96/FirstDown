package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson

class CourseProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_progress)

        // Get course ID from intent
        val courseId = intent.getStringExtra("COURSE_ID") ?: "football-basics"

        // Get course from DataManager
        val course = DataManager.getCourseById(courseId)

        if (course == null) {
            // If course not found, go back
            finish()
            return
        }

        // Get the course name from intent extras or from the course object
        val courseName = intent.getStringExtra("COURSE_NAME") ?: course.title

        // Set the course name in the title
        val tvTitle: TextView = findViewById(R.id.tv_course_title)
        tvTitle.text = courseName

        // Set progress values
        val tvProgress: TextView = findViewById(R.id.tv_progress_percent)
        tvProgress.text = "${course.progress}% Complete"

        val progressBar: ProgressBar = findViewById(R.id.progress_bar)
        progressBar.progress = course.progress

        // Setup back button to go to CoursesActivity
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            val intent = Intent(this, CoursesActivity::class.java)
            // Use FLAG_ACTIVITY_CLEAR_TOP to avoid creating multiple instances
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        // Setup chapter cards using the chapters from the course
        setupChapterCards(course.chapters)

        // Setup Continue Learning button
        val btnContinueLearning: Button = findViewById(R.id.btn_continue_learning)
        btnContinueLearning.setOnClickListener {
            // In a real app, this would navigate to the user's last accessed lesson
            // For now, just navigate to the first non-completed chapter
            val nextChapter = course.chapters.firstOrNull { it.progress < 100 && !it.isLocked }
            if (nextChapter != null) {
                // Navigate to the chapter's first incomplete lesson
                navigateToLesson(nextChapter.lessons.first())
            }
        }

        // Setup chapter continuation buttons
        setupChapterButtons()
    }

    private fun setupChapterCards(chapters: List<Chapter>) {
        // This method would need to be implemented based on your specific layout
        // It would add or update chapter card views based on the chapters list
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
        val btnContinueChapter: Button = findViewById(R.id.btn_continue_chapter)
        btnContinueChapter.setOnClickListener {
            // Navigate to the Basic Rules chapter
            // This will be implemented in a future step
        }

        val btnResumeChapter: Button = findViewById(R.id.btn_resume_learning)
        btnResumeChapter.setOnClickListener {
            // Navigate to the Player Positions chapter
            // This will be implemented in a future step
        }

        val btnStartChapter: Button = findViewById(R.id.btn_start_chapter)
        btnStartChapter.setOnClickListener {
            // Navigate to the Game Flow chapter
            // This will be implemented in a future step
        }
    }
}