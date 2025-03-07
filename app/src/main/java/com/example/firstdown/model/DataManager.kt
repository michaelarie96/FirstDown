package com.example.firstdown.model

import com.example.firstdown.R
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

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
            chaptersCompleted = 7,
            coursesCompleted = 1,
            quizScores = listOf(85, 90, 92, 88),
            timeSpent = 24 * 60 // 24 hours in minutes
        )

        // Quick tips for football learning
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
            "Practice ball security with the 5-point contact method"
        )

        // Sample lesson content data
        private val lessonsContent = mapOf(
            "downs" to listOf(
                LessonContent.Text("In American football, a team has four attempts (downs) to advance the ball 10 yards."),
                LessonContent.Text("If they succeed, they get a new set of downs. If they fail, possession is turned over to the opposing team."),
                LessonContent.Image(R.drawable.football_field_bg, "Football field showing the yard markers")
            ),
            "qb-mechanics" to listOf(
                LessonContent.Text("The quarterback is the leader of the offense and is responsible for calling plays and executing passes."),
                LessonContent.Text("Proper stance begins with feet shoulder-width apart, knees slightly bent, and the ball gripped with fingers on the laces."),
                LessonContent.Image(R.drawable.qb_stance, "Proper quarterback stance"),
                LessonContent.Text("When throwing, transfer your weight from back foot to front foot while rotating your hips and shoulders toward the target.")
            ),
            "formations" to listOf(
                LessonContent.Text("Formations determine the positioning of players on the field before the snap."),
                LessonContent.Text("The basic offensive formation is called the 'I Formation', with the quarterback under center, a fullback directly behind, and a running back behind the fullback."),
                LessonContent.Image(R.drawable.offense_playbook_bg, "I Formation diagram"),
                LessonContent.Text("Defensive formations are typically named by the number of linemen and linebackers, such as the 4-3 (4 linemen, 3 linebackers) or 3-4 (3 linemen, 4 linebackers).")
            )
        )

        // Sample lessons data
        private val lessons = mapOf(
            "downs" to Lesson(
                id = "downs",
                title = "Downs and Distances",
                description = "Understanding the four downs system",
                iconResId = R.drawable.ic_football,
                durationMinutes = 15,
                content = lessonsContent["downs"] ?: emptyList(),
                isCompleted = true
            ),
            "scoring" to Lesson(
                id = "scoring",
                title = "Scoring Methods",
                description = "Learn different ways to score in football",
                iconResId = R.drawable.ic_football,
                durationMinutes = 12,
                content = listOf(LessonContent.Text("There are multiple ways to score in football...")),
                isCompleted = true
            ),
            "penalties" to Lesson(
                id = "penalties",
                title = "Common Penalties",
                description = "Understanding flags and penalties",
                iconResId = R.drawable.ic_flag,
                durationMinutes = 20,
                content = listOf(LessonContent.Text("Penalties are called when rules are broken...")),
                isCompleted = true
            ),
            "timing" to Lesson(
                id = "timing",
                title = "Game Timing",
                description = "How the game clock works",
                iconResId = R.drawable.ic_clock,
                durationMinutes = 10,
                content = listOf(LessonContent.Text("Football games are divided into four quarters...")),
                isCompleted = true
            ),
            "field" to Lesson(
                id = "field",
                title = "Field Layout",
                description = "Understanding the football field",
                iconResId = R.drawable.ic_football,
                durationMinutes = 8,
                content = listOf(LessonContent.Text("A football field is 100 yards long...")),
                isCompleted = false
            ),
            "offense" to Lesson(
                id = "offense",
                title = "Offensive Positions",
                description = "Key offensive player roles",
                iconResId = R.drawable.ic_football,
                durationMinutes = 18,
                content = listOf(LessonContent.Text("The offense consists of various positions...")),
                isCompleted = true
            ),
            "defense" to Lesson(
                id = "defense",
                title = "Defensive Positions",
                description = "Key defensive player roles",
                iconResId = R.drawable.ic_football,
                durationMinutes = 18,
                content = listOf(LessonContent.Text("The defense consists of various positions...")),
                isCompleted = true
            ),
            "special" to Lesson(
                id = "special",
                title = "Special Teams",
                description = "Kicking and returning units",
                iconResId = R.drawable.ic_football,
                durationMinutes = 15,
                content = listOf(LessonContent.Text("Special teams handle kicks and punts...")),
                isCompleted = false
            ),
            "qb-mechanics" to Lesson(
                id = "qb-mechanics",
                title = "Quarterback Mechanics",
                description = "Learn proper stance and grip",
                iconResId = R.drawable.ic_football,
                durationMinutes = 20,
                content = lessonsContent["qb-mechanics"] ?: emptyList(),
                isCompleted = false
            ),
            "linemen" to Lesson(
                id = "linemen",
                title = "Offensive Line Techniques",
                description = "Blocking fundamentals",
                iconResId = R.drawable.ic_football,
                durationMinutes = 22,
                content = listOf(LessonContent.Text("Offensive linemen protect the quarterback...")),
                isCompleted = false
            ),
            "receivers" to Lesson(
                id = "receivers",
                title = "Route Running",
                description = "Receiver route fundamentals",
                iconResId = R.drawable.ic_football,
                durationMinutes = 25,
                content = listOf(LessonContent.Text("Receivers run specific patterns called routes...")),
                isCompleted = false
            ),
            "kickoff" to Lesson(
                id = "kickoff",
                title = "Kickoffs",
                description = "How games begin and resume",
                iconResId = R.drawable.ic_football,
                durationMinutes = 12,
                content = listOf(LessonContent.Text("Games begin with a kickoff...")),
                isCompleted = false
            ),
            "drives" to Lesson(
                id = "drives",
                title = "Offensive Drives",
                description = "Moving the ball downfield",
                iconResId = R.drawable.ic_football,
                durationMinutes = 15,
                content = listOf(LessonContent.Text("An offensive drive is a series of plays...")),
                isCompleted = false
            ),
            "timeouts" to Lesson(
                id = "timeouts",
                title = "Timeouts",
                description = "Strategic use of time",
                iconResId = R.drawable.ic_clock,
                durationMinutes = 10,
                content = listOf(LessonContent.Text("Teams have three timeouts per half...")),
                isCompleted = false
            ),
            "halftime" to Lesson(
                id = "halftime",
                title = "Halftime",
                description = "The mid-game break",
                iconResId = R.drawable.ic_football,
                durationMinutes = 8,
                content = listOf(LessonContent.Text("Halftime provides a break between the second and third quarters...")),
                isCompleted = false
            ),
            "formations" to Lesson(
                id = "formations",
                title = "Basic Formations",
                description = "Learn about common offensive and defensive formations",
                iconResId = R.drawable.ic_football,
                durationMinutes = 25,
                content = lessonsContent["formations"] ?: emptyList(),
                isCompleted = false
            ),
            "audibles" to Lesson(
                id = "audibles",
                title = "Audibles",
                description = "Changing plays at the line",
                iconResId = R.drawable.ic_football,
                durationMinutes = 18,
                content = listOf(LessonContent.Text("Audibles are changes to the play call...")),
                isCompleted = false
            ),
            "blitzes" to Lesson(
                id = "blitzes",
                title = "Blitz Packages",
                description = "Advanced defensive pressure",
                iconResId = R.drawable.ic_football,
                durationMinutes = 20,
                content = listOf(LessonContent.Text("Blitzes are designed to pressure the quarterback...")),
                isCompleted = false
            ),
            "coverages" to Lesson(
                id = "coverages",
                title = "Coverage Schemes",
                description = "Defending against the pass",
                iconResId = R.drawable.ic_football,
                durationMinutes = 22,
                content = listOf(LessonContent.Text("Coverage schemes determine how defenders match up...")),
                isCompleted = false
            )
        )

        // Sample quizzes
        private val quizzes = mapOf(
            "basic-rules-quiz" to Quiz(
                id = "basic-rules-quiz",
                question = "How many downs does a team have to advance the ball 10 yards?",
                options = listOf("3", "4", "5", "6"),
                correctOptionIndex = 1,
                explanation = "A team has 4 downs (attempts) to advance the ball 10 yards. If successful, they receive a new set of downs."
            ),
            "positions-quiz" to Quiz(
                id = "positions-quiz",
                question = "Which position is responsible for calling offensive plays and throwing passes?",
                options = listOf("Running Back", "Wide Receiver", "Quarterback", "Center"),
                correctOptionIndex = 2,
                explanation = "The quarterback is the offensive leader who typically calls plays and is the primary passer."
            ),
            "game-flow-quiz" to Quiz(
                id = "game-flow-quiz",
                question = "How is a game of football started?",
                options = listOf("With a jump ball", "With a kickoff", "With a coin toss followed by kickoff", "With a handoff"),
                correctOptionIndex = 2,
                explanation = "Football games begin with a coin toss to determine which team kicks off, followed by the kickoff itself."
            ),
            "advanced-tactics-quiz" to Quiz(
                id = "advanced-tactics-quiz",
                question = "What is an audible in football?",
                options = listOf("A defensive signal", "Changing the play at the line of scrimmage", "A type of penalty", "A specific formation"),
                correctOptionIndex = 1,
                explanation = "An audible is when the quarterback changes the play at the line of scrimmage based on the defensive setup."
            )
        )

        // Sample chapters data
        private val chapters = listOf(
            Chapter(
                id = "basic-rules",
                title = "Basic Rules",
                description = "Learn the fundamental rules of American Football",
                lessons = listOf(
                    lessons["downs"]!!,
                    lessons["scoring"]!!,
                    lessons["penalties"]!!,
                    lessons["timing"]!!,
                    lessons["field"]!!
                ),
                isLocked = false,
                requiredChapterIds = emptyList(),
                quiz = quizzes["basic-rules-quiz"],
                quizCompleted = true
            ),
            Chapter(
                id = "player-positions",
                title = "Player Positions",
                description = "Understand the different positions and their roles",
                lessons = listOf(
                    lessons["offense"]!!,
                    lessons["defense"]!!,
                    lessons["special"]!!,
                    lessons["qb-mechanics"]!!,
                    lessons["linemen"]!!,
                    lessons["receivers"]!!
                ),
                isLocked = false,
                requiredChapterIds = listOf("basic-rules"),
                quiz = quizzes["positions-quiz"],
                quizCompleted = false
            ),
            Chapter(
                id = "game-flow",
                title = "Game Flow",
                description = "Follow the flow of a football game from start to finish",
                lessons = listOf(
                    lessons["kickoff"]!!,
                    lessons["drives"]!!,
                    lessons["timeouts"]!!,
                    lessons["halftime"]!!
                ),
                isLocked = false,
                requiredChapterIds = listOf("basic-rules", "player-positions"),
                quiz = quizzes["game-flow-quiz"],
                quizCompleted = false
            ),
            Chapter(
                id = "advanced-tactics",
                title = "Advanced Tactics",
                description = "Learn advanced football strategies and plays",
                lessons = listOf(
                    lessons["formations"]!!,
                    lessons["audibles"]!!,
                    lessons["blitzes"]!!,
                    lessons["coverages"]!!
                ),
                isLocked = true,
                requiredChapterIds = listOf("basic-rules", "player-positions", "game-flow"),
                quiz = quizzes["advanced-tactics-quiz"],
                quizCompleted = false
            )
        )

        // Sample courses
        private val courses = listOf(
            Course(
                id = "football-basics",
                title = "Football Basics",
                description = "Master the fundamentals of American Football with simple, interactive lessons designed for beginners.",
                imageResId = R.drawable.football_field_bg,
                chapters = chapters
            ),
            Course(
                id = "offensive-playbook",
                title = "Offensive Playbook",
                description = "Master offense strategies with advanced play diagrams and concepts.",
                imageResId = R.drawable.offense_playbook_bg,
                chapters = emptyList() // Would populate with real chapters in a full implementation
            ),
            Course(
                id = "quarterback-fundamentals",
                title = "Quarterback Fundamentals",
                description = "Learn essential quarterback techniques from footwork to reading defenses.",
                imageResId = R.drawable.quarterback_bg,
                chapters = emptyList() // Would populate with real chapters in a full implementation
            )
        )

        // Sample achievements
        private val achievements = mutableListOf(
            Achievement(
                id = "high-score-1",
                title = "Quiz Master",
                description = "Achieved a quiz score above 80%",
                type = AchievementType.HIGH_QUIZ_SCORE,
                iconResId = R.drawable.ic_trophy,
                earnedDate = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                relatedId = "basic-rules-quiz"
            ),
            Achievement(
                id = "chapter-complete-1",
                title = "Rules Expert",
                description = "Completed the Basic Rules chapter",
                type = AchievementType.CHAPTER_COMPLETED,
                iconResId = R.drawable.ic_flag,
                earnedDate = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                relatedId = "basic-rules"
            )
        )

        private var hasStartedLearning = true
        private var goalCompleted = false

        // Track lesson progress (mapping of lesson ID to page number)
        private val lessonProgress = mutableMapOf<String, Int>()

        // Track which lessons have been started
        private val startedLessons = mutableSetOf<String>()

        // Track which chapters have been started
        private val startedChapters = mutableSetOf<String>()

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
                    chaptersCompleted = 0,
                    coursesCompleted = 0,
                    quizScores = emptyList(),
                    timeSpent = 0
                )
            } else {
                // Fall back to the default user for development
                return currentUser
            }
        }

        fun getLessonById(id: String): Lesson? = lessons[id]

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

        fun getAllLessons(): List<Lesson> = lessons.values.toList()
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

        fun wasLessonCompletedToday(lessonId: String): Boolean {
            val lesson = getLessonById(lessonId) ?: return false

            // For now, just check if the lesson is completed
            // In future, we'd also check the completion date
            return lesson.isCompleted
        }

        // Get next lesson to complete (for Today's Goal)
        fun getNextLessonToComplete(): Lesson? {
            // First try to find an incomplete lesson in the current course
            val currentCourse = getCurrentCourse()

            for (chapter in currentCourse.chapters) {
                if (chapter.isLocked) continue

                for (lesson in chapter.lessons) {
                    if (!lesson.isCompleted) {
                        return lesson
                    }
                }
            }

            // If all lessons in current course are completed, find the next course
            val nextCourse = getAllCourses().find {
                !it.isCompleted && it.id != currentCourse.id
            }

            if (nextCourse != null) {
                for (chapter in nextCourse.chapters) {
                    if (chapter.isLocked) continue

                    for (lesson in chapter.lessons) {
                        if (!lesson.isCompleted) {
                            return lesson
                        }
                    }
                }
            }

            return null
        }

        fun getChapterForLesson(lessonId: String): Chapter? {
            return getAllChapters().find { chapter ->
                chapter.lessons.any { it.id == lessonId }
            }
        }

        // Get the current or next chapter to study
        fun getCurrentOrNextChapter(): Pair<Course, Chapter>? {
            val currentCourse = getCurrentCourse()

            // Try to find an incomplete chapter in the current course
            val currentChapter = currentCourse.chapters.find {
                !it.isLocked && !it.isCompleted
            }

            if (currentChapter != null) {
                return Pair(currentCourse, currentChapter)
            }

            // If all chapters in current course are completed, find the next course
            val nextCourse = getAllCourses().find {
                !it.isCompleted && it.id != currentCourse.id
            } ?: return null

            val nextChapter = nextCourse.chapters.find {
                !it.isLocked && !it.isCompleted
            } ?: return null

            return Pair(nextCourse, nextChapter)
        }

        // Check if a chapter has been started
        fun hasStartedChapter(chapterId: String): Boolean {
            // Check if chapter ID is in tracked started chapters
            if (startedChapters.contains(chapterId)) return true

            // Or check if any lessons in the chapter have been started
            val chapter = getChapterById(chapterId) ?: return false
            val hasStartedAnyLesson = chapter.lessons.any { hasStartedLesson(it.id) }

            // If any lesson has been started, mark the chapter as started too
            if (hasStartedAnyLesson) {
                startedChapters.add(chapterId)
            }

            return hasStartedAnyLesson
        }

        // Get random quick tip
        fun getRandomQuickTip(): String {
            return quickTips.random()
        }

        // Achievement methods
        fun addAchievement(achievement: Achievement) {
            achievements.add(achievement)
            // In future, save to database
        }

        fun getLatestAchievement(): Achievement? {
            return achievements.maxByOrNull { it.earnedDate }
        }

        // Community-related methods
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