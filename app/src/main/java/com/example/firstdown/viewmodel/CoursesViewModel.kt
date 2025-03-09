package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager

class CoursesViewModel : ViewModel() {

    fun getAllCourses(onComplete: (List<Course>) -> Unit) {
        DataManager.getAllCourses(onComplete)
    }

    fun getCourseById(courseId: String, onComplete: (Course?) -> Unit) {
        DataManager.getCourseById(courseId, onComplete)
    }
}