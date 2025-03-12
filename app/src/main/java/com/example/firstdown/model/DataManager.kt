package com.example.firstdown.model

import android.util.Log
import com.example.firstdown.R
import com.example.firstdown.utilities.FirestoreManager
import com.example.firstdown.utilities.SharedPreferencesManager
import com.example.firstdown.utilities.TimeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

object DataManager {
    // SharedPreferences Keys
    private object SPKeys {
        const val PROGRESS_LOADED = "progress_loaded"
        const val DATABASE_INITIALIZED = "database_initialized"
        const val HAS_STARTED_LEARNING = "has_started_learning"
        const val COMPLETED_LESSONS = "completed_lessons"
        const val COMPLETED_QUIZZES = "completed_quizzes"
        const val COMPLETED_CHAPTERS = "completed_chapters"
        const val COMPLETED_COURSES = "completed_courses"
        const val STARTED_LESSONS = "started_lessons"
        const val STARTED_CHAPTERS = "started_chapters"
        const val QUIZ_SCORES = "quiz_scores"
    }

    // Local cache
    private val completedLessons = mutableSetOf<String>()
    private val completedQuizzes = mutableSetOf<String>()
    private val completedChapters = mutableSetOf<String>()
    private val completedCourses = mutableSetOf<String>()
    private val startedLessons = mutableSetOf<String>()
    private val startedChapters = mutableSetOf<String>()
    private val quizScores = mutableListOf<Int>()
    private val coursesCache = mutableMapOf<String, Course>()
    private val chaptersCache = mutableMapOf<String, Chapter>()
    private val lessonsCache = mutableMapOf<String, Lesson>()
    private val quizzesCache = mutableMapOf<String, Quiz>()
    private val allCoursesCache = mutableListOf<Course>()
    private var allCoursesCacheTimestamp = 0L

    private val userNotifications = mutableListOf<Notification>()
    private var unreadNotificationCount = 0

    private var notificationsListener: ListenerRegistration? = null
    private var activePostListeners = mutableMapOf<String, ListenerRegistration>()

    private var hasStartedLearning = false
    private var isProgressLoaded = false

    private const val KEY_TODAY_GOAL_DATE = "today_goal_date"
    private const val KEY_TODAY_GOAL_CHAPTER_ID = "today_goal_chapter_id"

    private val quickTips = listOf(
        "Keep your eyes on the ball's laces while throwing",
        "When catching, form a triangle with your thumbs and index fingers",
        "Plant your feet shoulder-width apart for a stable stance",
        "Focus on your follow-through when throwing long passes",
        "Keep your non-throwing arm close to your body for better balance",
        "Study the defensive formation before the snap",
        "Communication is key - make sure your teammates hear your calls",
        "Always keep your head up when running with the ball",
        "When blocking, focus on footwork before using your hands",
        "Practice ball security with the 5-point contact method",
        "When making a tackle, drive through the opponent, not just to them",
        "In zone coverage, watch the quarterback's eyes, not the receivers",
        "When rushing the passer, use your hands to keep blockers from controlling you",
        "Develop explosive power with plyometric exercises",
        "Review game film to identify tendencies in your opponents"
    )

    fun initialize(onComplete: () -> Unit = {}) {
        val sharedPrefs = SharedPreferencesManager.getInstance()

        // Check if the database has been initialized already
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            Log.d("DataManager", "First time initialization - uploading data to Firestore")
            initializeFirestoreDatabase {
                sharedPrefs.putBoolean(SPKeys.DATABASE_INITIALIZED, true)

                loadUserProgress {
                    sharedPrefs.putBoolean(SPKeys.PROGRESS_LOADED, true)
                    onComplete()
                }
            }
        } else {
            Log.d("DataManager", "Database flag says initialized, verifying data exists")

            // Verify data actually exists in Firestore
            verifyDatabaseInitialized { dataExists ->
                if (!dataExists) {
                    Log.d("DataManager", "Database flag was true but no data found, reinitializing")
                    // Database flag is true but no data exists, reinitialize
                    initializeFirestoreDatabase {
                        loadUserProgress {
                            onComplete()
                        }
                    }
                } else {
                    Log.d("DataManager", "Database verified as initialized, loading user progress")
                    if (!isProgressLoaded) {
                        loadProgressFromSharedPreferences()

                        loadUserProgress {
                            onComplete()
                        }
                    } else {
                        onComplete()
                    }
                }
            }
        }
    }

    private fun verifyDatabaseInitialized(callback: (Boolean) -> Unit) {
        // Check for at least one course in the database
        FirestoreManager.getAllCourses { courses ->
            if (courses.isEmpty()) {
                // No courses found, database not initialized properly
                callback(false)
            } else {
                // Courses found, database is properly initialized
                callback(true)
            }
        }
    }

    // Initialize the database with the static data
    private fun initializeFirestoreDatabase(onComplete: () -> Unit) {
        FirestoreManager.initializeDatabase(
            courses = courses,
            chapters = chapters,
            lessons = getAllLessons(),
            quizzes = quizzes,
            posts = posts
        ) {
            Log.d("DataManager", "Firestore database initialized successfully")
            onComplete()
        }
    }

    fun loadUserProgress(onComplete: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val progress = FirestoreManager.loadUserProgress()

                if (progress != null) {
                    withContext(Dispatchers.Main) {
                        completedLessons.clear()
                        completedLessons.addAll(progress.completedLessons)

                        completedQuizzes.clear()
                        completedQuizzes.addAll(progress.completedQuizzes)

                        completedChapters.clear()
                        completedChapters.addAll(progress.completedChapters)

                        completedCourses.clear()
                        completedCourses.addAll(progress.completedCourses)

                        startedLessons.clear()
                        startedLessons.addAll(progress.startedLessons)

                        startedChapters.clear()
                        startedChapters.addAll(progress.startedChapters)

                        hasStartedLearning = progress.startedLessons.isNotEmpty()
                        isProgressLoaded = true

                        saveProgressToSharedPreferences()

                        loadQuizScoresFromFirestore {
                            Log.d(
                                "DataManager",
                                "User progress and quiz scores loaded successfully from Firestore"
                            )
                            onComplete()
                        }
                    }
                } else {
                    // No saved progress found, still try to load quiz scores
                    withContext(Dispatchers.Main) {
                        isProgressLoaded = true
                        loadQuizScoresFromFirestore {
                            onComplete()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DataManager", "Error loading user progress", e)
                withContext(Dispatchers.Main) {
                    isProgressLoaded = true
                    onComplete()
                }
            }
        }
    }

    private fun saveProgressToSharedPreferences() {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val gson = Gson()

        // Save learning state
        sharedPrefs.putBoolean(SPKeys.HAS_STARTED_LEARNING, hasStartedLearning)

        // Save progress data
        sharedPrefs.putString(SPKeys.COMPLETED_LESSONS, gson.toJson(completedLessons.toList()))
        sharedPrefs.putString(SPKeys.COMPLETED_QUIZZES, gson.toJson(completedQuizzes.toList()))
        sharedPrefs.putString(SPKeys.COMPLETED_CHAPTERS, gson.toJson(completedChapters.toList()))
        sharedPrefs.putString(SPKeys.COMPLETED_COURSES, gson.toJson(completedCourses.toList()))
        sharedPrefs.putString(SPKeys.STARTED_LESSONS, gson.toJson(startedLessons.toList()))
        sharedPrefs.putString(SPKeys.STARTED_CHAPTERS, gson.toJson(startedChapters.toList()))
    }

    private fun loadProgressFromSharedPreferences() {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val gson = Gson()

        // Load learning state
        hasStartedLearning = sharedPrefs.getBoolean(SPKeys.HAS_STARTED_LEARNING, false)

        // Load progress data
        val completedLessonsJson = sharedPrefs.getString(SPKeys.COMPLETED_LESSONS, "[]")
        completedLessons.clear()
        completedLessons.addAll(gson.fromJson(completedLessonsJson, Array<String>::class.java))

        val completedQuizzesJson = sharedPrefs.getString(SPKeys.COMPLETED_QUIZZES, "[]")
        completedQuizzes.clear()
        completedQuizzes.addAll(gson.fromJson(completedQuizzesJson, Array<String>::class.java))

        val completedChaptersJson = sharedPrefs.getString(SPKeys.COMPLETED_CHAPTERS, "[]")
        completedChapters.clear()
        completedChapters.addAll(gson.fromJson(completedChaptersJson, Array<String>::class.java))

        val completedCoursesJson = sharedPrefs.getString(SPKeys.COMPLETED_COURSES, "[]")
        completedCourses.clear()
        completedCourses.addAll(gson.fromJson(completedCoursesJson, Array<String>::class.java))

        val startedLessonsJson = sharedPrefs.getString(SPKeys.STARTED_LESSONS, "[]")
        startedLessons.clear()
        startedLessons.addAll(gson.fromJson(startedLessonsJson, Array<String>::class.java))

        val startedChaptersJson = sharedPrefs.getString(SPKeys.STARTED_CHAPTERS, "[]")
        startedChapters.clear()
        startedChapters.addAll(gson.fromJson(startedChaptersJson, Array<String>::class.java))

        val quizScoresJson = sharedPrefs.getString(SPKeys.QUIZ_SCORES, "[]")
        quizScores.clear()
        quizScores.addAll(gson.fromJson(quizScoresJson, Array<Int>::class.java))

        isProgressLoaded = true
    }

    // Save progress to both SharedPreferences and Firestore
    private fun saveProgress() {
        saveProgressToSharedPreferences()

        val userProgress = UserProgress(
            userId = FirestoreManager.getCurrentUserId() ?: "",
            completedLessons = completedLessons.toMutableList(),
            completedQuizzes = completedQuizzes.toMutableList(),
            completedChapters = completedChapters.toMutableList(),
            completedCourses = completedCourses.toMutableList(),
            startedLessons = startedLessons.toMutableList(),
            startedChapters = startedChapters.toMutableList(),
            lastUpdated = System.currentTimeMillis()
        )

        FirestoreManager.saveUserProgress(userProgress)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            // Create a User object from Firebase
            onComplete(
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Football Fan",
                    email = firebaseUser.email ?: "",
                    profileImage = firebaseUser.photoUrl?.toString(),
                    title = "Beginner",
                    streakDays = 0,
                    lessonsCompleted = completedLessons.size,
                    chaptersCompleted = completedChapters.size,
                    coursesCompleted = completedCourses.size,
                    quizScores = quizScores.toList(),
                    timeSpent = 0
                )
            )
        } else {
            // Fall back to default user
            onComplete(
                User(
                    id = "user123",
                    name = "John Thompson",
                    email = "john@example.com",
                    title = "Quarterback in Training",
                    streakDays = 12,
                    lessonsCompleted = completedLessons.size,
                    chaptersCompleted = completedChapters.size,
                    coursesCompleted = completedCourses.size,
                    quizScores = listOf(85, 90, 92, 88),
                    timeSpent = 24 * 60
                )
            )
        }
    }

    // Helper for initialization
    fun getAllLessons(): List<Lesson> {
        return lessons.values.toList()
    }

    fun getAllCourses(onComplete: (List<Course>) -> Unit) {
        // Check if cache is still valid (less than 5 minutes old)
        val currentTime = System.currentTimeMillis()
        if (allCoursesCache.isNotEmpty() && currentTime - allCoursesCacheTimestamp < 5 * 60 * 1000) {
            onComplete(allCoursesCache)
            return
        }

        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            onComplete(courses)
        } else {
            // Define the correct order of course IDs
            val courseOrder = listOf(
                "football-basics",
                "offensive-playbook",
                "quarterback-fundamentals",
                "offensive-strategies",
                "defensive-strategies"
            )

            FirestoreManager.getAllCourses { fetchedCourses ->
                val sortedCourses = fetchedCourses.sortedBy { course ->
                    courseOrder.indexOf(course.id).let { index ->
                        if (index == -1) Int.MAX_VALUE else index
                    }
                }

                // Update cache
                allCoursesCache.clear()
                allCoursesCache.addAll(sortedCourses)
                allCoursesCacheTimestamp = currentTime

                // Cache individual courses too
                sortedCourses.forEach { course ->
                    coursesCache[course.id] = course
                    course.chapters.forEach { chapter ->
                        chaptersCache[chapter.id] = chapter
                        chapter.lessons.forEach { lesson ->
                            lessonsCache[lesson.id] = lesson
                        }
                    }
                }

                onComplete(sortedCourses)
            }
        }
    }

    fun getCourseById(id: String, onComplete: (Course?) -> Unit) {
        // Check cache first
        val cachedCourse = coursesCache[id]
        if (cachedCourse != null) {
            onComplete(cachedCourse)
            return
        }

        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            val course = courses.find { it.id == id }
            onComplete(course)
        } else {
            FirestoreManager.getCourseById(id) { course ->
                if (course != null) {
                    // Cache the course
                    coursesCache[id] = course

                    // Cache chapters and lessons
                    course.chapters.forEach { chapter ->
                        chaptersCache[chapter.id] = chapter
                        chapter.lessons.forEach { lesson ->
                            lessonsCache[lesson.id] = lesson
                        }
                    }
                }
                onComplete(course)
            }
        }
    }

    fun getChapterById(id: String, onComplete: (Chapter?) -> Unit) {
        // Check cache first
        val cachedChapter = chaptersCache[id]
        if (cachedChapter != null) {
            onComplete(cachedChapter)
            return
        }

        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            val chapter = chapters.find { it.id == id }
            onComplete(chapter)
        } else {
            FirestoreManager.getChapterById(id) { chapter ->
                if (chapter != null) {
                    // Cache the chapter
                    chaptersCache[id] = chapter

                    // Cache lessons
                    chapter.lessons.forEach { lesson ->
                        lessonsCache[lesson.id] = lesson
                    }
                }
                onComplete(chapter)
            }
        }
    }

    fun getLessonById(id: String, onComplete: (Lesson?) -> Unit) {
        // Check cache first
        val cachedLesson = lessonsCache[id]
        if (cachedLesson != null) {
            onComplete(cachedLesson)
            return
        }

        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            val lesson = lessons[id]
            onComplete(lesson)
        } else {
            FirestoreManager.getLessonById(id) { lesson ->
                if (lesson != null) {
                    // Cache the lesson
                    lessonsCache[id] = lesson
                }
                onComplete(lesson)
            }
        }
    }

    fun getCachedLessonById(id: String): Lesson? {
        return lessons[id]
    }

    fun markLessonComplete(lessonId: String, onComplete: () -> Unit = {}) {
        completedLessons.add(lessonId)
        startedLessons.add(lessonId)

        // Mark the chapter as started
        getCachedLessonById(lessonId)?.let { lesson ->
            startedChapters.add(lesson.chapterId)
        }

        saveProgress()

        FirestoreManager.markLessonComplete(lessonId, onComplete)

        Log.d("DataManager", "Lesson $lessonId marked as completed")
    }

    fun markQuizCompleted(chapterId: String, score: Int, onComplete: () -> Unit = {}) {
        completedQuizzes.add(chapterId)

        recordQuizScore(chapterId, score)

        markChapterComplete(chapterId)

        saveProgress()

        FirestoreManager.markQuizCompleted(chapterId, score) {
            FirestoreManager.updateChapter(chapterId, mapOf("quizCompleted" to true)) {
                onComplete()
            }
        }
    }

    fun recordQuizScore(chapterId: String, score: Int) {
        quizScores.add(score)

        saveQuizScoresToPreferences()

        saveQuizScoresToFirestore(score)
    }

    private fun saveQuizScoresToPreferences() {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val gson = Gson()
        sharedPrefs.putString("quiz_scores", gson.toJson(quizScores))
    }

    private fun loadQuizScoresFromPreferences() {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val gson = Gson()
        val scoresJson = sharedPrefs.getString("quiz_scores", "[]")
        val loadedScores = gson.fromJson(scoresJson, Array<Int>::class.java)
        quizScores.clear()
        quizScores.addAll(loadedScores)
    }

    private fun saveQuizScoresToFirestore(score: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val quizScoreData = hashMapOf(
            "score" to score,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .collection("quiz_scores")
            .add(quizScoreData)
    }

    private fun loadQuizScoresFromFirestore(onComplete: () -> Unit = {}) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .collection("quiz_scores")
            .get()
            .addOnSuccessListener { documents ->
                val scores = documents.mapNotNull { doc ->
                    doc.getLong("score")?.toInt()
                }

                if (scores.isNotEmpty()) {
                    quizScores.clear()
                    quizScores.addAll(scores)
                    saveQuizScoresToPreferences() // Sync with SharedPreferences
                }

                onComplete()
            }
            .addOnFailureListener {
                // Fall back to local preferences if Firestore fails
                loadQuizScoresFromPreferences()
                onComplete()
            }
    }

    fun markChapterComplete(chapterId: String) {
        completedChapters.add(chapterId)

        // Check if all lessons in this chapter are also completed
        getChapterById(chapterId) { chapter ->
            if (chapter != null) {
                // Get all chapters for this course
                getCourseById(chapter.courseId) { course ->
                    if (course != null) {
                        // Check if all chapters in the course are now completed
                        val allChaptersCompleted = course.chapters.all {
                            completedChapters.contains(it.id) || isChapterCompleted(it.id)
                        }

                        if (allChaptersCompleted) {
                            completedCourses.add(course.id)
                        }

                        saveProgress()
                    }
                }
            }
        }
    }

    fun isQuizCompleted(chapterId: String): Boolean {
        return completedQuizzes.contains(chapterId)
    }

    fun isLessonCompleted(lessonId: String, onComplete: (Boolean) -> Unit) {
        onComplete(completedLessons.contains(lessonId))
    }

    fun isLessonCompletedSync(lessonId: String): Boolean {
        return completedLessons.contains(lessonId)
    }

    fun hasStartedChapter(chapterId: String, onComplete: (Boolean) -> Unit) {
        onComplete(startedChapters.contains(chapterId))
    }

    fun hasStartedLesson(lessonId: String, onComplete: (Boolean) -> Unit) {
        onComplete(startedLessons.contains(lessonId))
    }

    fun getLessonsForChapter(chapterId: String, onComplete: (List<Lesson>) -> Unit) {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            val chapterLessons = lessons.values.filter { it.chapterId == chapterId }
                .sortedBy { it.index }
            onComplete(chapterLessons)
        } else {
            // Database initialized, fetch from Firestore
            FirestoreManager.getLessonsForChapter(chapterId, onComplete)
        }
    }

    fun getQuizForChapter(chapterId: String, onComplete: (Quiz?) -> Unit) {
        // Check cache first
        val quizId = chapterId + "-quiz"
        val cachedQuiz = quizzesCache[quizId] ?: quizzesCache[chapterId]
        if (cachedQuiz != null) {
            Log.d("DataManager", "Found quiz in cache: ${cachedQuiz.id}")
            onComplete(cachedQuiz)
            return
        }

        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            val staticQuiz = quizzes[quizId] ?: quizzes[chapterId]
            if (staticQuiz != null) {
                quizzesCache[staticQuiz.id] = staticQuiz
                onComplete(staticQuiz)
            } else {
                onComplete(null)
            }
        } else {
            // Database initialized, fetch from Firestore
            FirestoreManager.getQuizForChapter(chapterId) { quiz ->
                if (quiz != null) {
                    // Cache the quiz
                    quizzesCache[quiz.id] = quiz
                    // Also cache by chapter ID for easier lookups
                    quizzesCache[chapterId] = quiz
                    onComplete(quiz)
                } else {
                    // Check static data as a fallback
                    val staticQuiz = quizzes[quizId] ?: quizzes[chapterId]
                    if (staticQuiz != null) {
                        quizzesCache[staticQuiz.id] = staticQuiz
                        onComplete(staticQuiz)
                    } else {
                        onComplete(null)
                    }
                }
            }
        }
    }

    fun getNextLessonInChapter(chapterId: String, onComplete: (Lesson?) -> Unit) {
        getLessonsForChapter(chapterId) { chapterLessons ->
            val nextLesson = chapterLessons.firstOrNull { !completedLessons.contains(it.id) }
            onComplete(nextLesson)
        }
    }

    fun isLastLessonInChapter(lessonId: String, onComplete: (Boolean) -> Unit) {
        getLessonById(lessonId) { lesson ->
            if (lesson == null) {
                Log.d("DataManager", "isLastLessonInChapter: lesson is null")
                onComplete(false)
                return@getLessonById
            }

            getLessonsForChapter(lesson.chapterId) { chapterLessons ->
                val sortedLessons = chapterLessons.sortedBy { it.index }
                val lastLesson = sortedLessons.lastOrNull()
                val isLast = lastLesson?.id == lessonId

                Log.d(
                    "DataManager", "isLastLessonInChapter: $isLast, lessonId=$lessonId, " +
                            "lastLesson=${lastLesson?.id}, total lessons=${chapterLessons.size}"
                )
                onComplete(isLast)
            }
        }
    }

    fun hasStartedLearning(onComplete: (Boolean) -> Unit) {
        onComplete(hasStartedLearning)
    }

    fun setStartedLearning(started: Boolean) {
        hasStartedLearning = started
        saveProgress()
    }

    fun wasLessonCompletedToday(lessonId: String, onComplete: (Boolean) -> Unit) {
        onComplete(completedLessons.contains(lessonId))
    }

    fun getNextLessonToComplete(onComplete: (Lesson?) -> Unit) {
        getAllCourses { courses ->
            // Try to find an incomplete lesson in any available courses
            for (course in courses) {
                for (chapter in course.chapters) {
                    if (chapter.isLocked) continue

                    for (lesson in chapter.lessons) {
                        if (!completedLessons.contains(lesson.id)) {
                            onComplete(lesson)
                            return@getAllCourses
                        }
                    }
                }
            }
            onComplete(null) // No incomplete lessons found
        }
    }

    fun hasStartedChapter(chapterId: String): Boolean {
        if (startedChapters.contains(chapterId)) return true

        val chapterLessons = lessons.values.filter { it.chapterId == chapterId }
        val hasStartedAnyLesson = chapterLessons.any { startedLessons.contains(it.id) }

        if (hasStartedAnyLesson) {
            startedChapters.add(chapterId)
            saveProgress()
        }

        return hasStartedAnyLesson
    }

    fun getCurrentOrNextChapter(onComplete: (Pair<Course, Chapter>?) -> Unit) {
        getAllCourses { courses ->
            if (courses.isEmpty()) {
                Log.d("DataManager", "No courses found")
                onComplete(null)
                return@getAllCourses
            }

            Log.d("DataManager", "Found ${courses.size} courses")

            // First try to find a course the user has started but not completed
            for (course in courses) {
                Log.d("DataManager", "Checking course: ${course.id}, title: ${course.title}")

                // Skip if course is already completed
                if (completedCourses.contains(course.id)) {
                    Log.d("DataManager", "Course ${course.id} is already completed, skipping")
                    continue
                }

                // Log the chapter completion status
                course.chapters.forEach { chapter ->
                    val started = hasStartedChapter(chapter.id)
                    val completed = isChapterCompleted(chapter.id)
                    Log.d("DataManager", "Chapter ${chapter.id}: started=$started, completed=$completed, locked=${chapter.isLocked}")
                }

                // Check if any chapter in this course is started
                val hasStartedCourse = course.chapters.any { hasStartedChapter(it.id) }

                Log.d("DataManager", "Has started course ${course.id}: $hasStartedCourse")

                if (hasStartedCourse) {
                    // This is the current course the user is working on
                    val nextChapter = findNextChapterInCourse(course)
                    if (nextChapter != null) {
                        Log.d("DataManager", "Found next chapter in started course: ${nextChapter.id}, title: ${nextChapter.title}")
                        onComplete(Pair(course, nextChapter))
                        return@getAllCourses
                    } else {
                        Log.d("DataManager", "No next chapter found in started course ${course.id}")
                    }
                }
            }

            Log.d("DataManager", "No started course with incomplete chapters found, checking all courses")

            // If no current course with incomplete chapters, find first incomplete chapter in any course
            for (course in courses) {
                Log.d("DataManager", "Checking for any incomplete chapter in course ${course.id}")
                val nextChapter = findNextChapterInCourse(course)
                if (nextChapter != null) {
                    Log.d("DataManager", "Found incomplete chapter: ${nextChapter.id} in course ${course.id}")
                    onComplete(Pair(course, nextChapter))
                    return@getAllCourses
                }
            }

            Log.d("DataManager", "All courses complete or no next chapter found, using first course/chapter")

            // If everything is completed or we couldn't find a next chapter, return first course/chapter
            val firstCourse = courses.first()
            val firstChapter = firstCourse.chapters.firstOrNull()

            if (firstChapter != null) {
                Log.d("DataManager", "Using first chapter ${firstChapter.id} of first course ${firstCourse.id}")
                onComplete(Pair(firstCourse, firstChapter))
            } else {
                Log.d("DataManager", "No chapters found in first course")
                onComplete(null)
            }
        }
    }

    private fun findNextChapterInCourse(course: Course): Chapter? {
        Log.d("DataManager", "Finding next chapter in course ${course.id}")

        // First check for any in-progress chapters
        val inProgressChapter = course.chapters
            .filter { !it.isLocked }
            .find { chapter ->
                val started = hasStartedChapter(chapter.id)
                val completed = isChapterCompleted(chapter.id)
                Log.d("DataManager", "Checking chapter ${chapter.id}: started=$started, completed=$completed")
                started && !completed
            }

        if (inProgressChapter != null) {
            Log.d("DataManager", "Found in-progress chapter: ${inProgressChapter.id}")
            return inProgressChapter
        }

        Log.d("DataManager", "No in-progress chapter found, looking for first uncompleted chapter")

        // No in-progress chapter, find first uncompleted chapter
        val firstUncompleted = course.chapters
            .filter {
                val unlocked = !it.isLocked
                val completed = isChapterCompleted(it.id)
                Log.d("DataManager", "Checking chapter ${it.id} for completion: unlocked=$unlocked, completed=$completed")
                unlocked && !completed
            }
            .minByOrNull { it.index }

        if (firstUncompleted != null) {
            Log.d("DataManager", "Found first uncompleted chapter: ${firstUncompleted.id}")
        } else {
            Log.d("DataManager", "No uncompleted chapters found in course ${course.id}")
        }

        return firstUncompleted
    }

    fun shouldChapterBeLocked(chapterId: String, onComplete: (Boolean) -> Unit) {
        // First, get this chapter to find its course and index
        getChapterById(chapterId) { chapter ->
            if (chapter == null) {
                onComplete(true) // If chapter not found, consider it locked
                return@getChapterById
            }

            // Get all chapters for this course
            getCourseById(chapter.courseId) { course ->
                if (course == null) {
                    onComplete(true) // If course not found, consider it locked
                    return@getCourseById
                }

                // Sort chapters by index
                val sortedChapters = course.chapters.sortedBy { it.index }

                // Find index of this chapter
                val chapterIndex = sortedChapters.indexOfFirst { it.id == chapterId }

                if (chapterIndex <= 0) {
                    // First chapter is never locked
                    onComplete(false)
                    return@getCourseById
                }

                // If this chapter has been started, it should not be locked regardless
                if (hasStartedChapter(chapterId)) {
                    Log.d("DataManager", "Chapter $chapterId has been started, so it should not be locked")
                    onComplete(false)
                    return@getCourseById
                }

                // Check if all previous chapters are completed
                val previousChaptersCompleted = (0 until chapterIndex).all { i ->
                    val prevChapterId = sortedChapters[i].id
                    completedChapters.contains(prevChapterId) || isChapterCompleted(prevChapterId)
                }

                // Chapter should be locked if any previous chapter is not completed
                onComplete(!previousChaptersCompleted)
            }
        }
    }

    fun isChapterCompleted(chapterId: String): Boolean {
        // Check both our local cache and the chapters actual content
        if (completedChapters.contains(chapterId)) return true

        // Get the chapter from cache to check if all lessons are completed
        val chapter = chaptersCache[chapterId] ?: return false

        // A chapter is completed when all its lessons are completed and its quiz is completed
        val allLessonsCompleted = chapter.lessons.all { isLessonCompletedSync(it.id) }
        val quizCompleted = chapter.quiz == null || isQuizCompleted(chapterId)

        return allLessonsCompleted && quizCompleted
    }

    fun shouldCourseBeLocked(courseId: String, onComplete: (Boolean) -> Unit) {
        getAllCourses { courses ->
            val courseIndex = courses.indexOfFirst { it.id == courseId }

            // First course is never locked
            if (courseIndex <= 0) {
                onComplete(false)
                return@getAllCourses
            }

            // Check if all previous courses are completed
            for (i in 0 until courseIndex) {
                val previousCourse = courses[i]
                if (!previousCourse.isCompleted) {
                    onComplete(true) // Lock if any previous course isn't completed
                    return@getAllCourses
                }
            }

            onComplete(false)
        }
    }

    fun getRandomQuickTip(onComplete: (String) -> Unit) {
        onComplete(quickTips.random())
    }

    fun getAllPosts(onComplete: (List<Post>) -> Unit) {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val likedPostsKey =
            "liked_posts_${FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"}"
        val likedPosts = sharedPrefs.getStringSet(likedPostsKey, mutableSetOf())

        val sharedPrefsInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!sharedPrefsInitialized) {
            // Database not yet initialized, use static data
            val postsWithLikedStatus = posts.map { post ->
                post.copy(likedByCurrentUser = likedPosts.contains(post.id))
            }
            onComplete(postsWithLikedStatus)
        } else {
            // Database initialized, fetch from Firestore
            FirestoreManager.getAllPosts { fetchedPosts ->
                val postsWithFormattedTime = fetchedPosts.map { post ->
                    post.copy(
                        timeAgo = TimeUtils.getTimeAgo(post.timestamp),
                        likedByCurrentUser = likedPosts.contains(post.id)
                    )
                }
                onComplete(postsWithFormattedTime)
            }
        }
    }

    fun getPostById(postId: String, onComplete: (Post?) -> Unit) {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            val post = posts.find { it.id == postId }
            onComplete(post)
        } else {
            // Database initialized, fetch from Firestore
            FirestoreManager.getPostById(postId, onComplete)
        }
    }

    fun addNewPost(post: Post, onComplete: (Boolean) -> Unit = {}) {
        FirestoreManager.addNewPost(post, onComplete)
    }

    fun toggleLike(postId: String, onComplete: (updatedLikes: Int, isLiked: Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val likedPostsKey = "liked_posts_$currentUserId"
        val likedPosts = sharedPrefs.getStringSet(likedPostsKey, mutableSetOf())

        // Check if the post is already liked
        val isCurrentlyLiked = likedPosts.contains(postId)
        val updatedLikedPosts = likedPosts.toMutableSet()

        getPostById(postId) { post ->
            if (post != null) {
                val updatedLikes = if (isCurrentlyLiked) {
                    // Unlike the post
                    updatedLikedPosts.remove(postId)
                    Math.max(0, post.likes - 1) // Ensure likes don't go below 0
                } else {
                    // Like the post
                    updatedLikedPosts.add(postId)

                    // Only create notification when liking (not unliking)
                    // Don't notify if user is liking their own post
                    if (post.userName != "Anonymous" && post.userName.isNotEmpty() && post.userName != currentUserName) {
                        // Create a notification
                        val notificationId = UUID.randomUUID().toString()
                        val notification = Notification(
                            id = notificationId,
                            recipientUserName = post.userName,
                            message = "$currentUserName liked your post",
                            postId = postId,
                            likerName = currentUserName,
                            timestamp = System.currentTimeMillis()
                        )

                        Log.d(
                            "DataManager",
                            "Creating notification for ${post.userName} from $currentUserName, notification ID: $notificationId"
                        )
                        createNotification(notification) { success ->
                            Log.d("DataManager", "Notification creation result: $success")
                        }
                    } else {
                        Log.d(
                            "DataManager",
                            "Not creating notification. post.userName: ${post.userName}, currentUserName: $currentUserName"
                        )
                    }

                    post.likes + 1
                }

                // Save the updated liked posts to SharedPreferences
                sharedPrefs.putStringSet(likedPostsKey, updatedLikedPosts)

                // Update the post in Firestore
                val updates = mapOf("likes" to updatedLikes)

                Log.d("DataManager", "Updating post $postId likes to $updatedLikes")
                FirestoreManager.updatePost(postId, updates) {
                    Log.d("DataManager", "Post $postId likes updated to $updatedLikes")
                    onComplete(updatedLikes, !isCurrentlyLiked)
                }
            } else {
                Log.e("DataManager", "Post $postId not found for like update")
                onComplete(0, false)
            }
        }
    }

    fun createNotification(notification: Notification, onComplete: (Boolean) -> Unit = {}) {
        FirestoreManager.addNotification(notification, onComplete)
    }

    fun getUserNotifications(userName: String, onComplete: (List<Notification>) -> Unit) {
        FirestoreManager.getNotificationsForUser(userName) { notifications ->
            userNotifications.clear()
            userNotifications.addAll(notifications)
            unreadNotificationCount = notifications.count { !it.isRead }
            onComplete(notifications)
        }
    }

    fun markNotificationAsRead(notificationId: String, onComplete: (Boolean) -> Unit = {}) {
        FirestoreManager.updateNotification(notificationId, mapOf("isRead" to true)) { success ->
            if (success) {
                // Find the notification index
                val index = userNotifications.indexOfFirst { it.id == notificationId }
                if (index != -1) {
                    // Update by creating a new object with the read flag set to true
                    val updatedNotification = userNotifications[index].copy(isRead = true)
                    userNotifications[index] = updatedNotification

                    // Recalculate unread count
                    unreadNotificationCount = userNotifications.count { !it.isRead }
                }
            }
            onComplete(success)
        }
    }

    fun getUnreadNotificationCount(): Int {
        return unreadNotificationCount
    }

    fun setupNotificationsListener(userName: String, onUpdate: (List<Notification>) -> Unit) {
        // Remove any existing listener
        notificationsListener?.remove()

        // Set up a new listener
        notificationsListener =
            FirestoreManager.addNotificationsListener(userName) { notifications ->
                userNotifications.clear()
                userNotifications.addAll(notifications)
                unreadNotificationCount = notifications.count { !it.isRead }
                onUpdate(notifications)
            }
    }

    fun setupPostListener(postId: String, onUpdate: (Post?) -> Unit) {
        // Remove any existing listener
        activePostListeners[postId]?.remove()

        // Set up a new listener
        activePostListeners[postId] = FirestoreManager.addPostListener(postId) { post ->
            onUpdate(post)
        }
    }

    fun clearAllListeners() {
        notificationsListener?.remove()
        notificationsListener = null

        activePostListeners.values.forEach { it.remove() }
        activePostListeners.clear()
    }

    fun getUserStreak(onComplete: (Int) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(0)

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val streak = document.getLong("streakDays")?.toInt() ?: 0
                onComplete(streak)
            }
            .addOnFailureListener {
                onComplete(0)
            }
    }

    fun getTodayGoal(onComplete: (Chapter?) -> Unit) {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val savedDate = sharedPrefs.getString(KEY_TODAY_GOAL_DATE, "")

        // If we already set a goal today, return that saved goal
        if (savedDate == today) {
            val chapterId = sharedPrefs.getString(KEY_TODAY_GOAL_CHAPTER_ID, "")
            if (chapterId.isNotEmpty()) {
                getChapterById(chapterId) { chapter ->
                    onComplete(chapter)
                }
                return
            }
        }

        // It's a new day or no goal was set - get a new goal from getCurrentOrNextChapter
        getCurrentOrNextChapter { courseChapterPair ->
            if (courseChapterPair != null) {
                val (_, chapter) = courseChapterPair

                // Save today's date and goal chapter
                sharedPrefs.putString(KEY_TODAY_GOAL_DATE, today)
                sharedPrefs.putString(KEY_TODAY_GOAL_CHAPTER_ID, chapter.id)

                onComplete(chapter)
            } else {
                onComplete(null)
            }
        }
    }
}