package com.example.firstdown.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Quiz

class QuizViewModel : ViewModel() {

    fun getChapterById(chapterId: String, onComplete: (Chapter?) -> Unit) {
        DataManager.getChapterById(chapterId, onComplete)
    }

    fun getQuizForChapter(chapterId: String, onComplete: (Quiz?) -> Unit) {
        getChapterById(chapterId) { chapter ->
            onComplete(chapter?.quiz)
        }
    }

    fun isAnswerCorrect(chapterId: String, selectedOptionIndex: Int, onComplete: (Boolean) -> Unit) {
        getQuizForChapter(chapterId) { quiz ->
            onComplete(quiz?.correctOptionIndex == selectedOptionIndex)
        }
    }

    fun getTotalQuizCount(chapterId: String, onComplete: (Int) -> Unit) {
        getQuizForChapter(chapterId) { quiz ->
            onComplete(if (quiz != null) 1 else 0)
        }
    }

    fun isQuizAvailable(chapterId: String, onComplete: (Boolean) -> Unit) {
        getQuizForChapter(chapterId) { quiz ->
            onComplete(quiz != null)
        }
    }

    fun markQuizCompleted(chapterId: String, score: Int) {
        DataManager.markQuizCompleted(chapterId, score)
    }}