package com.example.firstdown.utilities

import android.util.Log
import com.example.firstdown.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    companion object {
        private const val TAG = "FirestoreManager"

        // Collection names
        private const val USERS_COLLECTION = "users"
        private const val PROGRESS_COLLECTION = "progress"
        private const val COURSES_COLLECTION = "courses"
        private const val CHAPTERS_COLLECTION = "chapters"
        private const val LESSONS_COLLECTION = "lessons"
        private const val QUIZZES_COLLECTION = "quizzes"
        private const val POSTS_COLLECTION = "posts"
        private const val NOTIFICATIONS_COLLECTION = "notifications"

        private val db = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()

        fun getCurrentUserId(): String? {
            return auth.currentUser?.uid
        }

        // Initialize database with the static data
        fun initializeDatabase(
            courses: List<Course>,
            chapters: List<Chapter>,
            lessons: List<Lesson>,
            quizzes: Map<String, Quiz>,
            posts: List<Post>,
            onComplete: () -> Unit
        ) {
            // Check if data already exists
            db.collection(COURSES_COLLECTION).limit(1).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        // Database is empty, initialize it with our static data
                        Log.d(TAG, "Database empty, uploading all static data")
                        uploadAllDataToFirestore(courses, chapters, lessons, quizzes, posts, onComplete)
                    } else {
                        // Database already has data
                        Log.d(TAG, "Database already initialized with data")
                        onComplete()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking database: $e")
                    onComplete()
                }
        }

        private fun uploadAllDataToFirestore(
            courses: List<Course>,
            chapters: List<Chapter>,
            lessons: List<Lesson>,
            quizzes: Map<String, Quiz>,
            posts: List<Post>,
            onComplete: () -> Unit
        ) {
            val batch = db.batch()

            // Courses
            courses.forEach { course ->
                val courseRef = db.collection(COURSES_COLLECTION).document(course.id)

                val courseData = mapOf(
                    "id" to course.id,
                    "title" to course.title,
                    "description" to course.description,
                    "imageResId" to course.imageResId
                )

                batch.set(courseRef, courseData)
            }

            // Chapters
            chapters.forEach { chapter ->
                val chapterRef = db.collection(CHAPTERS_COLLECTION).document(chapter.id)

                val chapterData = mapOf(
                    "id" to chapter.id,
                    "courseId" to chapter.courseId,
                    "title" to chapter.title,
                    "description" to chapter.description,
                    "isLocked" to chapter.isLocked,
                    "quizCompleted" to chapter.quizCompleted,
                    "index" to chapter.index
                )

                batch.set(chapterRef, chapterData)
            }

            // Lessons
            lessons.forEach { lesson ->
                val lessonRef = db.collection(LESSONS_COLLECTION).document(lesson.id)
                batch.set(lessonRef, lesson)
            }

            // Quizzes
            quizzes.forEach { (quizId, quiz) ->
                val documentId = if (quiz.id.isNotEmpty()) quiz.id else quizId
                val quizRef = db.collection(QUIZZES_COLLECTION).document(documentId)

                Log.d(TAG, "Adding quiz with ID: $documentId")
                batch.set(quizRef, quiz)
            }

            // Posts
            posts.forEach { post ->
                val postRef = db.collection(POSTS_COLLECTION).document(post.id)
                batch.set(postRef, post)
            }

            // Commit the batch to save everything at once
            batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "All data successfully uploaded to Firestore")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading data to Firestore: $e")
                    onComplete()
                }
        }

        fun saveUserProgress(
            userProgress: UserProgress,
            onSuccess: () -> Unit = {},
            onFailure: (Exception) -> Unit = {}
        ) {
            val currentUserId = getCurrentUserId() ?: return

            db.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(PROGRESS_COLLECTION)
                .document("user_progress")
                .set(userProgress)
                .addOnSuccessListener {
                    Log.d(TAG, "User progress saved successfully")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving user progress: $e")
                    onFailure(e)
                }
        }

        suspend fun loadUserProgress(): UserProgress? {
            val currentUserId = getCurrentUserId() ?: return null

            return try {
                val document = db.collection(USERS_COLLECTION)
                    .document(currentUserId)
                    .collection(PROGRESS_COLLECTION)
                    .document("user_progress")
                    .get()
                    .await()

                document.toObject(UserProgress::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user progress: $e")
                null
            }
        }

        fun getAllCourses(onComplete: (List<Course>) -> Unit) {
            // Define the correct order of course IDs
            val courseOrder = listOf(
                "football-basics",
                "offensive-playbook",
                "quarterback-fundamentals",
                "offensive-strategies",
                "defensive-strategies"
            )

            db.collection(COURSES_COLLECTION)
                .get()
                .addOnSuccessListener { coursesSnapshot ->
                    if (coursesSnapshot.isEmpty) {
                        Log.d(TAG, "No courses found in Firestore database")
                        onComplete(emptyList())
                        return@addOnSuccessListener
                    }

                    val coursesMap = mutableMapOf<String, MutableMap<String, Any>>()

                    // Create basic course objects
                    coursesSnapshot.documents.forEach { doc ->
                        val courseId = doc.id
                        val courseData = mutableMapOf<String, Any>()

                        courseData["id"] = courseId
                        courseData["title"] = doc.getString("title") ?: ""
                        courseData["description"] = doc.getString("description") ?: ""
                        courseData["imageResId"] = doc.getLong("imageResId")?.toInt() ?: 0
                        courseData["chapters"] = mutableListOf<Chapter>()

                        coursesMap[courseId] = courseData
                    }

                    // If no courses, return empty list
                    if (coursesMap.isEmpty()) {
                        onComplete(emptyList())
                        return@addOnSuccessListener
                    }

                    // Get chapters for all courses
                    db.collection(CHAPTERS_COLLECTION)
                        .get()
                        .addOnSuccessListener { chaptersSnapshot ->
                            val chapters = mutableListOf<Chapter>()

                            // Create chapter objects and group by course
                            chaptersSnapshot.documents.forEach { doc ->
                                val chapterId = doc.id
                                val courseId = doc.getString("courseId") ?: ""

                                if (coursesMap.containsKey(courseId)) {
                                    // Get the chapters list for this course
                                    val chaptersList = coursesMap[courseId]?.get("chapters") as MutableList<Chapter>

                                    // Add chapter data
                                    val chapterData = mapOf(
                                        "id" to chapterId,
                                        "courseId" to courseId,
                                        "title" to (doc.getString("title") ?: ""),
                                        "description" to (doc.getString("description") ?: ""),
                                        "isLocked" to (doc.getBoolean("isLocked") ?: false),
                                        "quizCompleted" to (doc.getBoolean("quizCompleted") ?: false),
                                        "index" to (doc.getLong("index")?.toInt() ?: 0)
                                    )

                                    // Get lessons for this chapter
                                    getLessonsForChapter(chapterId) { lessons ->
                                        // Get the quiz for this chapter
                                        getQuizForChapter(chapterId) { quiz ->
                                            val chapter = Chapter(
                                                id = chapterId,
                                                courseId = courseId,
                                                title = chapterData["title"] as String,
                                                description = chapterData["description"] as String,
                                                lessons = lessons,
                                                isLocked = chapterData["isLocked"] as Boolean,
                                                quiz = quiz,
                                                quizCompleted = chapterData["quizCompleted"] as Boolean,
                                                index = chapterData["index"] as Int
                                            )

                                            chaptersList.add(chapter)
                                            chapters.add(chapter)

                                            // If we've processed all chapters, build the courses list
                                            if (chapters.size == chaptersSnapshot.size()) {
                                                val coursesList = coursesMap.values.map { courseData ->
                                                    Course(
                                                        id = courseData["id"] as String,
                                                        title = courseData["title"] as String,
                                                        description = courseData["description"] as String,
                                                        imageResId = courseData["imageResId"] as Int,
                                                        chapters = (courseData["chapters"] as List<Chapter>)
                                                            .sortedBy { it.index }
                                                    )
                                                }

                                                val sortedCourses = coursesList.sortedBy { course ->
                                                    courseOrder.indexOf(course.id).let { index ->
                                                        if (index == -1) Int.MAX_VALUE else index // Handle any IDs not in the list
                                                    }
                                                }

                                                onComplete(sortedCourses)
                                            }
                                        }
                                    }
                                }
                            }

                            // If there are no chapters, return courses without chapters
                            if (chaptersSnapshot.isEmpty) {
                                val coursesList = coursesMap.values.map { courseData ->
                                    Course(
                                        id = courseData["id"] as String,
                                        title = courseData["title"] as String,
                                        description = courseData["description"] as String,
                                        imageResId = courseData["imageResId"] as Int,
                                        chapters = emptyList()
                                    )
                                }

                                // Sort courses by the index in the courseOrder list
                                val sortedCourses = coursesList.sortedBy { course ->
                                    courseOrder.indexOf(course.id).let { index ->
                                        if (index == -1) Int.MAX_VALUE else index // Handle any IDs not in the list
                                    }
                                }

                                onComplete(sortedCourses)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error getting chapters: $exception")
                            onComplete(emptyList())
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting courses: $exception")
                    onComplete(emptyList())
                }
        }

        fun getCourseById(courseId: String, onComplete: (Course?) -> Unit) {
            db.collection(COURSES_COLLECTION)
                .document(courseId)
                .get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        onComplete(null)
                        return@addOnSuccessListener
                    }

                    val courseData = mapOf(
                        "id" to courseId,
                        "title" to (doc.getString("title") ?: ""),
                        "description" to (doc.getString("description") ?: ""),
                        "imageResId" to (doc.getLong("imageResId")?.toInt() ?: 0)
                    )

                    // Get chapters for this course
                    db.collection(CHAPTERS_COLLECTION)
                        .whereEqualTo("courseId", courseId)
                        .orderBy("index")
                        .get()
                        .addOnSuccessListener { chaptersSnapshot ->
                            val chapters = mutableListOf<Chapter>()
                            var chaptersProcessed = 0

                            if (chaptersSnapshot.isEmpty) {
                                // No chapters, return course without chapters
                                val course = Course(
                                    id = courseData["id"] as String,
                                    title = courseData["title"] as String,
                                    description = courseData["description"] as String,
                                    imageResId = courseData["imageResId"] as Int,
                                    chapters = emptyList()
                                )

                                onComplete(course)
                                return@addOnSuccessListener
                            }

                            // Process each chapter
                            chaptersSnapshot.documents.forEach { chapterDoc ->
                                val chapterId = chapterDoc.id

                                // Get lessons for this chapter
                                getLessonsForChapter(chapterId) { lessons ->
                                    // Get quiz for this chapter
                                    getQuizForChapter(chapterId) { quiz ->
                                        val chapter = Chapter(
                                            id = chapterId,
                                            courseId = courseId,
                                            title = chapterDoc.getString("title") ?: "",
                                            description = chapterDoc.getString("description") ?: "",
                                            lessons = lessons,
                                            isLocked = chapterDoc.getBoolean("isLocked") ?: false,
                                            quiz = quiz,
                                            quizCompleted = chapterDoc.getBoolean("quizCompleted") ?: false,
                                            index = chapterDoc.getLong("index")?.toInt() ?: 0
                                        )

                                        chapters.add(chapter)
                                        chaptersProcessed++

                                        // If all chapters processed, return the course
                                        if (chaptersProcessed == chaptersSnapshot.size()) {
                                            val course = Course(
                                                id = courseData["id"] as String,
                                                title = courseData["title"] as String,
                                                description = courseData["description"] as String,
                                                imageResId = courseData["imageResId"] as Int,
                                                chapters = chapters.sortedBy { it.index }
                                            )

                                            onComplete(course)
                                        }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error getting chapters for course: $exception")
                            onComplete(null)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting course: $exception")
                    onComplete(null)
                }
        }

        fun getChapterById(chapterId: String, onComplete: (Chapter?) -> Unit) {
            db.collection(CHAPTERS_COLLECTION)
                .document(chapterId)
                .get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        onComplete(null)
                        return@addOnSuccessListener
                    }

                    val courseId = doc.getString("courseId") ?: ""

                    // Get lessons for this chapter
                    getLessonsForChapter(chapterId) { lessons ->
                        // Get quiz for this chapter
                        getQuizForChapter(chapterId) { quiz ->
                            val chapter = Chapter(
                                id = chapterId,
                                courseId = courseId,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                lessons = lessons,
                                isLocked = doc.getBoolean("isLocked") ?: false,
                                quiz = quiz,
                                quizCompleted = doc.getBoolean("quizCompleted") ?: false,
                                index = doc.getLong("index")?.toInt() ?: 0
                            )

                            onComplete(chapter)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting chapter: $exception")
                    onComplete(null)
                }
        }

        fun updateChapterLockStatus(chapterId: String, isLocked: Boolean, onComplete: () -> Unit = {}) {
            db.collection(CHAPTERS_COLLECTION)
                .document(chapterId)
                .update("isLocked", isLocked)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully updated lock status for chapter $chapterId to $isLocked")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating chapter lock status: $e")
                    onComplete()
                }
        }

        fun updateChapter(chapterId: String, updates: Map<String, Any>, onComplete: () -> Unit = {}) {
            db.collection(CHAPTERS_COLLECTION)
                .document(chapterId)
                .update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully updated chapter $chapterId with $updates")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating chapter: $e")
                    onComplete()
                }
        }

        fun getLessonsForChapter(chapterId: String, onComplete: (List<Lesson>) -> Unit) {
            db.collection(LESSONS_COLLECTION)
                .whereEqualTo("chapterId", chapterId)
                .get()
                .addOnSuccessListener { lessonsSnapshot ->
                    val lessons = lessonsSnapshot.toObjects(Lesson::class.java)
                    val sortedLessons = lessons.sortedBy { it.index }
                    onComplete(sortedLessons)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting lessons: $exception")
                    onComplete(emptyList())
                }
        }

        fun getLessonById(lessonId: String, onComplete: (Lesson?) -> Unit) {
            db.collection(LESSONS_COLLECTION)
                .document(lessonId)
                .get()
                .addOnSuccessListener { doc ->
                    val lesson = doc.toObject(Lesson::class.java)
                    onComplete(lesson)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting lesson: $exception")
                    onComplete(null)
                }
        }

        fun getQuizForChapter(chapterId: String, onComplete: (Quiz?) -> Unit) {
            val quizDocId = chapterId + "-quiz"

            Log.d(TAG, "Looking for quiz with document ID: $quizDocId")

            db.collection(QUIZZES_COLLECTION)
                .document(quizDocId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val quiz = doc.toObject(Quiz::class.java)
                        Log.d(TAG, "Successfully retrieved quiz for chapter $chapterId with ID $quizDocId")
                        onComplete(quiz)
                    } else {
                        // Try with the original chapter ID as fallback
                        db.collection(QUIZZES_COLLECTION)
                            .document(chapterId)
                            .get()
                            .addOnSuccessListener { fallbackDoc ->
                                if (fallbackDoc.exists()) {
                                    val quiz = fallbackDoc.toObject(Quiz::class.java)
                                    Log.d(TAG, "Retrieved quiz using fallback ID for chapter $chapterId")
                                    onComplete(quiz)
                                } else {
                                    Log.e(TAG, "No quiz document found for IDs $quizDocId or $chapterId")
                                    // Instead of returning null, let's check the static data
                                    onComplete(null)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error getting fallback quiz for chapter $chapterId: $e")
                                onComplete(null)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting quiz for chapter $chapterId: $exception")
                    onComplete(null)
                }
        }

        fun markLessonComplete(lessonId: String, onComplete: () -> Unit = {}) {
            val currentUserId = getCurrentUserId() ?: return

            // Update the completedLessons array in user progress
            db.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(PROGRESS_COLLECTION)
                .document("user_progress")
                .update("completedLessons", com.google.firebase.firestore.FieldValue.arrayUnion(lessonId))
                .addOnSuccessListener {
                    Log.d(TAG, "Lesson $lessonId marked as completed")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error marking lesson as completed: $e")

                    // If document doesn't exist yet, create it
                    val userProgress = UserProgress(
                        userId = currentUserId,
                        completedLessons = mutableListOf(lessonId)
                    )

                    db.collection(USERS_COLLECTION)
                        .document(currentUserId)
                        .collection(PROGRESS_COLLECTION)
                        .document("user_progress")
                        .set(userProgress)
                        .addOnSuccessListener {
                            Log.d(TAG, "Created new user progress document")
                            onComplete()
                        }
                        .addOnFailureListener { ex ->
                            Log.e(TAG, "Error creating user progress document: $ex")
                            onComplete()
                        }
                }
        }

        fun markQuizCompleted(chapterId: String, score: Int, onComplete: () -> Unit = {}) {
            val currentUserId = getCurrentUserId() ?: return

            // Update user progress
            db.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(PROGRESS_COLLECTION)
                .document("user_progress")
                .update("completedQuizzes", com.google.firebase.firestore.FieldValue.arrayUnion(chapterId))
                .addOnSuccessListener {
                    Log.d(TAG, "Quiz for chapter $chapterId marked as completed with score $score")

                    // Also update the quiz completed flag in the chapter
                    db.collection(CHAPTERS_COLLECTION)
                        .document(chapterId)
                        .update("quizCompleted", true)
                        .addOnSuccessListener {
                            Log.d(TAG, "Chapter $chapterId quiz marked as completed")
                            onComplete()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating chapter quiz status: $e")
                            onComplete()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error marking quiz as completed: $e")
                    onComplete()
                }
        }

        fun getAllPosts(onComplete: (List<Post>) -> Unit) {
            db.collection(POSTS_COLLECTION)
                .orderBy("timeAgo", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val posts = result.toObjects(Post::class.java)
                    onComplete(posts)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting posts: $exception")
                    onComplete(emptyList())
                }
        }

        fun getPostById(postId: String, onComplete: (Post?) -> Unit) {
            db.collection(POSTS_COLLECTION)
                .document(postId)
                .get()
                .addOnSuccessListener { doc ->
                    val post = doc.toObject(Post::class.java)
                    onComplete(post)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting post: $exception")
                    onComplete(null)
                }
        }

        fun updatePost(postId: String, updates: Map<String, Any>, onComplete: () -> Unit = {}) {
            db.collection(POSTS_COLLECTION)
                .document(postId)
                .update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Post $postId updated successfully")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating post: $e")
                    onComplete()
                }
        }

        fun addNewPost(post: Post, onComplete: (Boolean) -> Unit = {}) {
            Log.d(TAG, "Adding new post with ID: ${post.id}")
            db.collection(POSTS_COLLECTION)
                .document(post.id)
                .set(post)
                .addOnSuccessListener {
                    Log.d(TAG, "Post added successfully with ID: ${post.id}")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding post: $e")
                    onComplete(false)
                }
        }

        fun addNotification(notification: Notification, onComplete: (Boolean) -> Unit) {
            db.collection(NOTIFICATIONS_COLLECTION)
                .document(notification.id)
                .set(notification)
                .addOnSuccessListener {
                    Log.d(TAG, "Notification added successfully with ID: ${notification.id}")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding notification: $e")
                    onComplete(false)
                }
        }

        fun createNotification(notification: Notification, onComplete: (Boolean) -> Unit = {}) {
            Log.d("DataManager", "Creating notification in Firestore: ${notification.id}, recipient: ${notification.recipientUserName}")
            FirestoreManager.addNotification(notification) { success ->
                Log.d("DataManager", "Notification created in Firestore: $success")
                onComplete(success)
            }
        }

        fun getNotificationsForUser(userName: String, onComplete: (List<Notification>) -> Unit) {
            db.collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("recipientUserName", userName)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val notifications = result.toObjects(Notification::class.java)
                    onComplete(notifications)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting notifications: $exception")
                    onComplete(emptyList())
                }
        }

        fun updateNotification(notificationId: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit) {
            db.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Notification $notificationId updated successfully")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating notification: $e")
                    onComplete(false)
                }
        }

        fun addPostListener(postId: String, onUpdate: (Post?) -> Unit): ListenerRegistration {
            Log.d(TAG, "Setting up real-time listener for post: $postId")
            return db.collection(POSTS_COLLECTION)
                .document(postId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed for post: $postId", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "Real-time update received for post: $postId")
                            val post = snapshot.toObject(Post::class.java)
                            onUpdate(post)
                        } else {
                            Log.d(TAG, "Post document doesn't exist: $postId")
                        }
                    } else {
                        Log.d(TAG, "Null snapshot received for post: $postId")
                    }
                }
        }

        fun addNotificationsListener(userName: String, onUpdate: (List<Notification>) -> Unit): ListenerRegistration {
            return db.collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("recipientUserName", userName)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed for notifications", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val notifications = snapshot.toObjects(Notification::class.java)
                            .sortedByDescending { it.timestamp } // Sort in memory instead of in the query
                        onUpdate(notifications)
                    }
                }
        }
    }
}