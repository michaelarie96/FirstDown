package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Quiz

class QuizViewModel : ViewModel() {

    // Get chapter by ID
    fun getChapterById(chapterId: String): Chapter? {
        return DataManager.getChapterById(chapterId)
    }

    // Get quiz for a chapter
    fun getQuizForChapter(chapterId: String): Quiz? {
        return getChapterById(chapterId)?.quiz
    }

    // Check if the selected answer is correct
    fun isAnswerCorrect(chapterId: String, selectedOptionIndex: Int): Boolean {
        val quiz = getQuizForChapter(chapterId)
        return quiz?.correctOptionIndex == selectedOptionIndex
    }

    // Get total number of quizzes (now always 1 per chapter)
    fun getTotalQuizCount(chapterId: String): Int {
        return if (getQuizForChapter(chapterId) != null) 1 else 0
    }

    // Check if this is the only quiz (since we now have only one quiz per chapter)
    fun isQuizAvailable(chapterId: String): Boolean {
        return getQuizForChapter(chapterId) != null
    }

    fun markQuizCompleted(chapterId: String, score: Int) {
        DataManager.markQuizCompleted(chapterId, score)
    }}