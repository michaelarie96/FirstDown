package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.adapters.CourseAdapter
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager

class CoursesActivity : AppCompatActivity(), CourseAdapter.OnCourseClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        // Set up the back button
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // Set up the RecyclerView
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_courses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get courses from DataManager
        val courses = DataManager.getAllCourses()

        // Create and set the adapter
        courseAdapter = CourseAdapter(courses, this)
        recyclerView.adapter = courseAdapter
    }

    // Handle course click events
    override fun onCourseClick(course: Course) {
        val intent = Intent(this, CourseProgressActivity::class.java)
        intent.putExtra("COURSE_ID", course.id)
        intent.putExtra("COURSE_NAME", course.title)
        startActivity(intent)
    }
}