package com.example.firstdown.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.firstdown.model.DataManager
import com.example.firstdown.model.Chapter
import com.example.firstdown.model.Quiz

class QuizViewModel : ViewModel() {

    fun getChapterById(chapterId: String, onComplete: (Chapter?) -> Unit) {
        DataManager.getChapterById(chapterId, onComplete)
    }

    fun getQuizForChapter(chapterId: String, onComplete: (Quiz?) -> Unit) {
        Log.d("QuizViewModel", "Attempting to get quiz for chapter: $chapterId")

        // First, check if the chapter has the quiz property set
        getChapterById(chapterId) { chapter ->
            if (chapter == null) {
                Log.e("QuizViewModel", "Could not find chapter with ID: $chapterId")
                // Try to get quiz directly from DataManager
                DataManager.getQuizForChapter(chapterId, onComplete)
                return@getChapterById
            }

            Log.d("QuizViewModel", "Got chapter for quiz: ${chapter.id}, has quiz: ${chapter.quiz != null}")

            if (chapter.quiz != null) {
                Log.d("QuizViewModel", "Using quiz from chapter object")
                onComplete(chapter.quiz)
            } else {
                // Try to get the quiz from DataManager
                Log.d("QuizViewModel", "Chapter.quiz is null, trying to get quiz from DataManager")
                DataManager.getQuizForChapter(chapterId) { quiz ->
                    if (quiz != null) {
                        Log.d("QuizViewModel", "Found quiz via DataManager")
                    } else {
                        Log.e("QuizViewModel", "No quiz found for chapter via DataManager")
                    }
                    onComplete(quiz)
                }
            }
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

    fun markQuizCompleted(chapterId: String, score: Int, onComplete: () -> Unit = {}) {
        DataManager.markQuizCompleted(chapterId, score, onComplete)
    }
}