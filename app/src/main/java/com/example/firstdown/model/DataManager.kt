package com.example.firstdown.model

import com.example.firstdown.R
import com.google.firebase.auth.FirebaseAuth

class DataManager {
    companion object {
        // Sample user data
        private val currentUser = User(
            id = "user123",
            name = "John Thompson",
            email = "john@example.com",
            title = "Quarterback in Training",
            streakDays = 12,
            lessonsCompleted = 42,
            quizScore = 89,
            timeSpent = 24 * 60 // 24 hours in minutes
        )

        // Sample lessons data
        private val lessons = listOf(
            Lesson(
                id = "qb-mechanics",
                title = "Quarterback Mechanics",
                description = "Learn proper stance and grip",
                iconResId = R.drawable.ic_football,
                durationMinutes = 20,
                content = listOf(
                    LessonContent.Text("The quarterback is the leader of the offense..."),
                    LessonContent.Image(R.drawable.qb_stance, "Proper quarterback stance")
                ),
                quizzes = listOf(
                    Quiz(
                        id = "qb-quiz1",
                        question = "What is the correct way to grip a football?",
                        options = listOf(
                            "Fingers on the laces, thumb underneath",
                            "Palm directly on the laces",
                            "Two fingers on the point of the ball",
                            "However feels comfortable"
                        ),
                        correctOptionIndex = 0,
                        explanation = "The proper grip has your fingers on the laces with your thumb underneath for maximum control."
                    )
                )
            ),
            // More lessons would be defined here
        )

        // Sample chapters data
        private val chapters = listOf(
            Chapter(
                id = "basic-rules",
                title = "Basic Rules",
                description = "Learn the fundamental rules of American Football",
                lessons = if (lessons.size >= 5) lessons.subList(0, 5) else lessons,
                progress = 80, // 4/5 complete
                isLocked = false
            ),
            // More chapters would be defined here
        )

        private val courses = listOf(
            Course(
                id = "football-basics",
                title = "Football Basics",
                description = "Master the fundamentals of American Football with simple, interactive lessons designed for beginners.",
                imageResId = R.drawable.football_field_bg,
                chapters = listOf(
                    Chapter(
                        id = "basic-rules",
                        title = "Basic Rules",
                        description = "Learn the fundamental rules of American Football",
                        lessons = getLessonsByIds("downs", "scoring", "penalties", "timing", "field"),
                        progress = 80, // 4/5 complete
                        isLocked = false
                    ),
                    Chapter(
                        id = "player-positions",
                        title = "Player Positions",
                        description = "Understand the different positions and their roles",
                        lessons = getLessonsByIds("offense", "defense", "special", "qb", "linemen", "receivers"),
                        progress = 33, // 2/6 complete
                        isLocked = false
                    ),
                    Chapter(
                        id = "game-flow",
                        title = "Game Flow",
                        description = "Follow the flow of a football game from start to finish",
                        lessons = getLessonsByIds("kickoff", "drives", "timeouts", "halftime"),
                        progress = 0, // 0/4 complete
                        isLocked = false
                    ),
                    Chapter(
                        id = "advanced-tactics",
                        title = "Advanced Tactics",
                        description = "Learn advanced football strategies and plays",
                        lessons = getLessonsByIds("formations", "audibles", "blitzes", "coverages"),
                        progress = 0,
                        isLocked = true
                    )
                ),
                difficulty = "Beginner",
                estimatedHours = 10,
                isPopular = true
            ),
            Course(
                id = "offensive-playbook",
                title = "Offensive Playbook",
                description = "Master offense strategies with advanced play diagrams and concepts.",
                imageResId = R.drawable.offense_playbook_bg,
                chapters = emptyList(), // Would populate with real chapters in a full implementation
                difficulty = "Intermediate",
                estimatedHours = 12
            ),
            Course(
                id = "quarterback-fundamentals",
                title = "Quarterback Fundamentals",
                description = "Learn essential quarterback techniques from footwork to reading defenses.",
                imageResId = R.drawable.quarterback_bg,
                chapters = emptyList(), // Would populate with real chapters in a full implementation
                difficulty = "Intermediate",
                estimatedHours = 8
            )
        )

        private var hasStartedLearning = true
        private var goalCompleted = false

        private var currentLessonId = "qb-mechanics" // default lesson ID

        // Track lesson progress (mapping of lesson ID to page number)
        private val lessonProgress = mutableMapOf<String, Int>()

        // Track which lessons have been started
        private val startedLessons = mutableSetOf<String>()

        // Accessor methods

        fun getCurrentUser(): User {
            // Try to get Firebase user first
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (firebaseUser != null) {
                // Create a User object with data from Firebase
                return User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Football Fan",
                    email = firebaseUser.email ?: "",
                    // Use default values for app-specific properties
                    profileImage = firebaseUser.photoUrl?.toString(),
                    title = "Beginner",
                    streakDays = 0,
                    lessonsCompleted = 0,
                    quizScore = 0,
                    timeSpent = 0
                )
            } else {
                // Fall back to the default user for development
                return currentUser
            }
        }

        fun getCurrentLesson(): Lesson {
            return getLessonById(currentLessonId) ?: lessons.first()
        }

        fun getLessonById(id: String): Lesson? = lessons.find { it.id == id }

        private fun getLessonsByIds(vararg ids: String): List<Lesson> {
            return ids.mapNotNull { id ->
                when (id) {
                    "downs" -> Lesson(
                        id = "downs",
                        title = "Downs and Distances",
                        description = "Understanding the four downs system",
                        iconResId = R.drawable.ic_football,
                        durationMinutes = 15,
                        content = listOf(LessonContent.Text("In American football, a team has four attempts (downs) to advance the ball 10 yards.")),
                        quizzes = emptyList()
                    )
                    "qb" -> Lesson(
                        id = "qb-mechanics",
                        title = "Quarterback Mechanics",
                        description = "Learn proper stance and grip",
                        iconResId = R.drawable.ic_football,
                        durationMinutes = 20,
                        content = listOf(LessonContent.Text("The quarterback is the leader of the offense...")),
                        quizzes = emptyList()
                    )
                    "formations" -> Lesson(
                        id = "formations",
                        title = "Basic Formations",
                        description = "Learn about common offensive and defensive formations",
                        iconResId = R.drawable.ic_football,
                        durationMinutes = 25,
                        content = listOf(LessonContent.Text("Formations determine the positioning of players on the field...")),
                        quizzes = emptyList()
                    )
                    // Add more lessons as needed
                    else -> null
                }
            }
        }

        fun hasStartedLesson(lessonId: String): Boolean {
            return startedLessons.contains(lessonId)
        }

        fun getLessonProgress(lessonId: String): Int {
            return lessonProgress.getOrDefault(lessonId, 1)
        }

        fun updateLessonProgress(lessonId: String, pageNumber: Int) {
            lessonProgress[lessonId] = pageNumber
            startedLessons.add(lessonId)
            // In future, save this to SharedPreferences or database
        }

        fun getAllLessons(): List<Lesson> = lessons
        fun getAllChapters(): List<Chapter> = chapters
        fun getAllCourses(): List<Course> = courses
        fun getChapterById(id: String): Chapter? = chapters.find { it.id == id }
        fun getCourseById(id: String): Course? = courses.find { it.id == id }
        fun getCurrentCourse(): Course = getCourseById("football-basics") ?: courses.first()

        // State methods
        fun hasStartedLearning(): Boolean = hasStartedLearning
        fun setStartedLearning(started: Boolean) {
            hasStartedLearning = started
            // In future, save this to SharedPreferences or a database
        }

        fun isGoalCompleted(): Boolean = goalCompleted
        fun setGoalCompleted(completed: Boolean) {
            goalCompleted = completed
            // In future, save this to SharedPreferences or a database
        }

        private val posts = listOf(
            Post(
                id = "post1",
                userProfileImage = "https://placeholder.com/user1",
                userName = "John Cooper",
                timeAgo = "2 hours ago",
                content = "What's the best way to improve ball control during high-pressure situations? Any drills recommendations?",
                imageUrl = null,
                likes = 24,
                comments = 8
            ),
            Post(
                id = "post2",
                userProfileImage = "https://placeholder.com/user2",
                userName = "Sarah Wilson",
                timeAgo = "5 hours ago",
                content = "Pro tip: Here's a quick drill for improving your first touch. Practice this for 15 minutes daily.",
                imageUrl = "https://placeholder.com/football-drill",
                likes = 56,
                comments = 12
            )
        )

        fun getAllPosts(): List<Post> = posts
        fun getPostById(postId: String): Post? = posts.find { it.id == postId }

    }
}