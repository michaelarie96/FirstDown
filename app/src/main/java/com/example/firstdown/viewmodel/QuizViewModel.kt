// File: app/src/main/java/com/example/firstdown/viewmodel/QuizViewModel.kt
package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Lesson
import com.example.firstdown.model.Quiz

class QuizViewModel : ViewModel() {

    // Get lesson by ID
    fun getLessonById(lessonId: String): Lesson? {
        return DataManager.getLessonById(lessonId)
    }

    // Get all quizzes for a lesson
    fun getQuizzesForLesson(lessonId: String): List<Quiz> {
        return getLessonById(lessonId)?.quizzes ?: emptyList()
    }

    // Get a specific quiz by index
    fun getQuizByIndex(lessonId: String, index: Int): Quiz? {
        val quizzes = getQuizzesForLesson(lessonId)
        return if (index >= 0 && index < quizzes.size) {
            quizzes[index]
        } else {
            null
        }
    }

    // Check if the selected answer is correct
    fun isAnswerCorrect(lessonId: String, quizIndex: Int, selectedOptionIndex: Int): Boolean {
        val quiz = getQuizByIndex(lessonId, quizIndex)
        return quiz?.correctOptionIndex == selectedOptionIndex
    }

    // Get total number of quizzes
    fun getTotalQuizCount(lessonId: String): Int {
        return getQuizzesForLesson(lessonId).size
    }

    // Check if this is the last quiz
    fun isLastQuiz(lessonId: String, currentQuizIndex: Int): Boolean {
        return currentQuizIndex >= getTotalQuizCount(lessonId) - 1
    }
}