// File: app/src/main/java/com/example/firstdown/CoursesActivity.kt
package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstdown.adapters.CourseAdapter
import com.example.firstdown.databinding.ActivityCoursesBinding
import com.example.firstdown.model.Course
import com.example.firstdown.viewmodel.CoursesViewModel

class CoursesActivity : AppCompatActivity(), CourseAdapter.OnCourseClickListener {

    private lateinit var binding: ActivityCoursesBinding
    private val viewModel: CoursesViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Set up the back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Set up the RecyclerView
        setupRecyclerView()

        // Get courses from ViewModel
        val courses = viewModel.getAllCourses()

        // Update the adapter with the courses
        courseAdapter.updateCourses(courses)
    }

    private fun setupRecyclerView() {
        binding.rvCourses.layoutManager = LinearLayoutManager(this)

        // Create and set the adapter
        courseAdapter = CourseAdapter(emptyList(), this)
        binding.rvCourses.adapter = courseAdapter
    }

    // Handle course click events
    override fun onCourseClick(course: Course) {
        val intent = Intent(this, CourseProgressActivity::class.java)
        intent.putExtra("COURSE_ID", course.id)
        intent.putExtra("COURSE_NAME", course.title)
        startActivity(intent)
    }
}