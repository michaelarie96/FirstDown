package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager

class CoursesViewModel : ViewModel() {

    // Get all available courses
    fun getAllCourses(): List<Course> {
        return DataManager.getAllCourses()
    }

    // Get a specific course by ID
    fun getCourseById(courseId: String): Course? {
        return DataManager.getCourseById(courseId)
    }
}