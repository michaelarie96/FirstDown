package com.example.firstdown.utilities

import android.util.Log
import com.example.firstdown.model.UserProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    companion object {
        private const val TAG = "FirestoreManager"
        private const val USERS_COLLECTION = "users"
        private const val PROGRESS_COLLECTION = "progress"

        private val auth = FirebaseAuth.getInstance()

        // Get current user ID or null if not logged in
        private fun getCurrentUserId(): String? {
            return auth.currentUser?.uid
        }

        // Reference to the user's progress document
        private fun getUserProgressRef() =
            getCurrentUserId()?.let { userId ->
                FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(PROGRESS_COLLECTION)
                    .document("user_progress")
            }

        // Save user progress to Firestore
        fun saveUserProgress(
            completedLessons: Set<String>,
            completedQuizzes: Set<String>,
            completedChapters: Set<String>,
            completedCourses: Set<String>,
            startedLessons: Set<String>,
            startedChapters: Set<String>,
            onSuccess: () -> Unit = {},
            onFailure: (Exception) -> Unit = {}
        ) {
            val userId = getCurrentUserId() ?: return

            val userProgress = UserProgress(
                userId = userId,
                completedLessons = completedLessons.toMutableList(),
                completedQuizzes = completedQuizzes.toMutableList(),
                completedChapters = completedChapters.toMutableList(),
                completedCourses = completedCourses.toMutableList(),
                startedLessons = startedLessons.toMutableList(),
                startedChapters = startedChapters.toMutableList(),
                lastUpdated = System.currentTimeMillis()
            )

            getUserProgressRef()?.set(userProgress)
                ?.addOnSuccessListener {
                    Log.d(TAG, "User progress saved successfully")
                    onSuccess()
                }
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "Error saving user progress", e)
                    onFailure(e)
                }
        }

        // Load user progress from Firestore
        suspend fun loadUserProgress(): UserProgress? {
            return try {
                getUserProgressRef()?.get()?.await()?.toObject(UserProgress::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user progress", e)
                null
            }
        }
    }
}