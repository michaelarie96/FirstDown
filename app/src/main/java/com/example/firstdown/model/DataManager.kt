package com.example.firstdown.model

import android.util.Log
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

        // Sample lessons data - updated to the new structure
        private val lessons = mapOf(
            "downs" to Lesson(
                id = "downs",
                chapterId = "basic-rules",
                title = "Downs and Distances",
                description = "Understanding the four downs system",
                contentType = ContentType.TEXT,
                contentData = "In American football, a team has four attempts (downs) to advance the ball 10 yards. If they succeed, they get a new set of downs. If they fail, possession is turned over to the opposing team.",
                imageResId = R.drawable.football_field_bg,
                durationMinutes = 15,
                isCompleted = true,
                index = 0
            ),
            "scoring" to Lesson(
                id = "scoring",
                chapterId = "basic-rules",
                title = "Scoring Methods",
                description = "Learn different ways to score in football",
                contentType = ContentType.TEXT,
                contentData = "There are multiple ways to score in football including touchdowns (6 points), extra points (1 or 2 points), field goals (3 points), and safeties (2 points).",
                imageResId = null,
                durationMinutes = 12,
                isCompleted = true,
                index = 1
            ),
            "penalties" to Lesson(
                id = "penalties",
                chapterId = "basic-rules",
                title = "Common Penalties",
                description = "Understanding flags and penalties",
                contentType = ContentType.TEXT,
                contentData = "Penalties are called when rules are broken. Common penalties include offside, holding, pass interference, and false start. Most result in yardage penalties.",
                imageResId = R.drawable.ic_flag,
                durationMinutes = 20,
                isCompleted = true,
                index = 2
            ),
            "timing" to Lesson(
                id = "timing",
                chapterId = "basic-rules",
                title = "Game Timing",
                description = "How the game clock works",
                contentType = ContentType.TEXT,
                contentData = "Football games are divided into four quarters of 15 minutes each. The clock stops for incomplete passes, when a player goes out of bounds, and during timeouts.",
                imageResId = R.drawable.ic_clock,
                durationMinutes = 10,
                isCompleted = true,
                index = 3
            ),
            "field" to Lesson(
                id = "field",
                chapterId = "basic-rules",
                title = "Field Layout",
                description = "Understanding the football field",
                contentType = ContentType.IMAGE,
                contentData = "A football field is 100 yards long with 10-yard end zones on each end. The field is marked with yard lines every 5 yards and hash marks to indicate where the ball should be placed.",
                imageResId = R.drawable.football_field_bg,
                durationMinutes = 8,
                isCompleted = false,
                index = 4
            ),
            "offense" to Lesson(
                id = "offense",
                chapterId = "player-positions",
                title = "Offensive Positions",
                description = "Key offensive player roles",
                contentType = ContentType.TEXT,
                contentData = "The offense consists of various positions including quarterback, running back, wide receiver, tight end, and offensive linemen. Each position has specific responsibilities in moving the ball down the field.",
                imageResId = null,
                durationMinutes = 18,
                isCompleted = true,
                index = 0
            ),
            "defense" to Lesson(
                id = "defense",
                chapterId = "player-positions",
                title = "Defensive Positions",
                description = "Key defensive player roles",
                contentType = ContentType.TEXT,
                contentData = "The defense consists of various positions including defensive linemen, linebackers, cornerbacks, and safeties. Each position has specific responsibilities in stopping the offense.",
                imageResId = null,
                durationMinutes = 18,
                isCompleted = true,
                index = 1
            ),
            "special" to Lesson(
                id = "special",
                chapterId = "player-positions",
                title = "Special Teams",
                description = "Kicking and returning units",
                contentType = ContentType.TEXT,
                contentData = "Special teams handle kicks and punts. These units include kickers, punters, long snappers, and returners, as well as blockers and tacklers.",
                imageResId = null,
                durationMinutes = 15,
                isCompleted = false,
                index = 2
            ),
            "qb-mechanics" to Lesson(
                id = "qb-mechanics",
                chapterId = "player-positions",
                title = "Quarterback Mechanics",
                description = "Learn proper stance and grip",
                contentType = ContentType.IMAGE,
                contentData = "The quarterback is the leader of the offense and is responsible for calling plays and executing passes. Proper stance begins with feet shoulder-width apart, knees slightly bent, and the ball gripped with fingers on the laces.",
                imageResId = R.drawable.qb_stance,
                durationMinutes = 20,
                isCompleted = false,
                index = 3
            ),
            "linemen" to Lesson(
                id = "linemen",
                chapterId = "player-positions",
                title = "Offensive Line Techniques",
                description = "Blocking fundamentals",
                contentType = ContentType.TEXT,
                contentData = "Offensive linemen protect the quarterback and create running lanes. Key techniques include proper stance, hand placement, footwork, and leverage.",
                imageResId = null,
                durationMinutes = 22,
                isCompleted = false,
                index = 4
            ),
            "receivers" to Lesson(
                id = "receivers",
                chapterId = "player-positions",
                title = "Route Running",
                description = "Receiver route fundamentals",
                contentType = ContentType.TEXT,
                contentData = "Receivers run specific patterns called routes. Common routes include slants, outs, posts, and go routes. Proper technique involves clean breaks and precise timing.",
                imageResId = null,
                durationMinutes = 25,
                isCompleted = false,
                index = 5
            ),
            "kickoff" to Lesson(
                id = "kickoff",
                chapterId = "game-flow",
                title = "Kickoffs",
                description = "How games begin and resume",
                contentType = ContentType.TEXT,
                contentData = "Games begin with a kickoff. The kicking team kicks from their own 35-yard line, and the receiving team attempts to return the ball for good field position.",
                imageResId = null,
                durationMinutes = 12,
                isCompleted = false,
                index = 0
            ),
            "drives" to Lesson(
                id = "drives",
                chapterId = "game-flow",
                title = "Offensive Drives",
                description = "Moving the ball downfield",
                contentType = ContentType.TEXT,
                contentData = "An offensive drive is a series of plays where the offense attempts to move the ball downfield. A successful drive results in a score, while an unsuccessful drive ends in a turnover or punt.",
                imageResId = null,
                durationMinutes = 15,
                isCompleted = false,
                index = 1
            ),
            "timeouts" to Lesson(
                id = "timeouts",
                chapterId = "game-flow",
                title = "Timeouts",
                description = "Strategic use of time",
                contentType = ContentType.TEXT,
                contentData = "Teams have three timeouts per half. Timeouts stop the game clock and are used to manage time, discuss strategy, or stop the clock in late-game situations.",
                imageResId = R.drawable.ic_clock,
                durationMinutes = 10,
                isCompleted = false,
                index = 2
            ),
            "halftime" to Lesson(
                id = "halftime",
                chapterId = "game-flow",
                title = "Halftime",
                description = "The mid-game break",
                contentType = ContentType.TEXT,
                contentData = "Halftime provides a break between the second and third quarters. Teams use this time to rest, adjust strategies, and make tactical changes for the second half.",
                imageResId = null,
                durationMinutes = 8,
                isCompleted = false,
                index = 3
            ),
            "formations" to Lesson(
                id = "formations",
                chapterId = "advanced-tactics",
                title = "Basic Formations",
                description = "Learn about common offensive and defensive formations",
                contentType = ContentType.IMAGE,
                contentData = "Formations determine the positioning of players on the field before the snap. The basic offensive formation is called the 'I Formation', with the quarterback under center, a fullback directly behind, and a running back behind the fullback.",
                imageResId = R.drawable.offense_playbook_bg,
                durationMinutes = 25,
                isCompleted = false,
                index = 0
            ),
            "audibles" to Lesson(
                id = "audibles",
                chapterId = "advanced-tactics",
                title = "Audibles",
                description = "Changing plays at the line",
                contentType = ContentType.TEXT,
                contentData = "Audibles are changes to the play call made by the quarterback at the line of scrimmage. They are typically called when the quarterback recognizes a defensive formation that would make the original play ineffective.",
                imageResId = null,
                durationMinutes = 18,
                isCompleted = false,
                index = 1
            ),
            "blitzes" to Lesson(
                id = "blitzes",
                chapterId = "advanced-tactics",
                title = "Blitz Packages",
                description = "Advanced defensive pressure",
                contentType = ContentType.TEXT,
                contentData = "Blitzes are designed to pressure the quarterback by sending more defenders than the offense can block. Different blitz packages target different gaps and use various timing to confuse the offensive line.",
                imageResId = null,
                durationMinutes = 20,
                isCompleted = false,
                index = 2
            ),
            "coverages" to Lesson(
                id = "coverages",
                chapterId = "advanced-tactics",
                title = "Coverage Schemes",
                description = "Defending against the pass",
                contentType = ContentType.TEXT,
                contentData = "Coverage schemes determine how defenders match up against receivers. Common coverages include man-to-man, zone coverage (Cover 2, Cover 3, etc.), and hybrid schemes that combine both approaches.",
                imageResId = null,
                durationMinutes = 22,
                isCompleted = false,
                index = 3
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
                courseId = "football-basics",
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
                quizCompleted = true,
                index = 0
            ),
            Chapter(
                id = "player-positions",
                courseId = "football-basics",
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
                quizCompleted = false,
                index = 1
            ),
            Chapter(
                id = "game-flow",
                courseId = "football-basics",
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
                quizCompleted = false,
                index = 2
            ),
            Chapter(
                id = "advanced-tactics",
                courseId = "football-basics",
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
                quizCompleted = false,
                index = 3
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

        // Track completed lessons
        private val completedLessons = mutableSetOf<String>()

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

        // New method to get lesson content
        fun getLessonContent(lessonId: String): Pair<String, Int?> {
            val lesson = lessons[lessonId] ?: return Pair("", null)
            return Pair(lesson.contentData, lesson.imageResId)
        }

        fun hasStartedLesson(lessonId: String): Boolean {
            return startedLessons.contains(lessonId)
        }

        fun isLessonCompleted(lessonId: String): Boolean {
            return completedLessons.contains(lessonId)
        }

        fun markLessonComplete(lessonId: String) {
            completedLessons.add(lessonId)
            startedLessons.add(lessonId)
            // In future, save this to SharedPreferences or database
            Log.d("DataManager", "Lesson $lessonId marked as completed")
        }

        // Get lessons for a specific chapter
        fun getLessonsForChapter(chapterId: String): List<Lesson> {
            return lessons.values.filter { it.chapterId == chapterId }
                .sortedBy { it.index }
        }

        // Get next lesson in a chapter
        fun getNextLessonInChapter(chapterId: String): Lesson? {
            val chapterLessons = getLessonsForChapter(chapterId)
            return chapterLessons.firstOrNull { !completedLessons.contains(it.id) }
        }

        // Get next incomplete lesson
        fun getNextIncompleteLesson(lessonId: String): Lesson? {
            val currentLesson = getLessonById(lessonId) ?: return null
            val chapterId = currentLesson.chapterId
            val chapterLessons = getLessonsForChapter(chapterId)

            // Find lessons with higher index that aren't completed
            return chapterLessons
                .filter { it.index > currentLesson.index && !completedLessons.contains(it.id) }
                .minByOrNull { it.index }
        }

        // Get previous lesson
        fun getPreviousLesson(lessonId: String): Lesson? {
            val currentLesson = getLessonById(lessonId) ?: return null
            val chapterId = currentLesson.chapterId
            val chapterLessons = getLessonsForChapter(chapterId)

            // Find lessons with lower index
            return chapterLessons
                .filter { it.index < currentLesson.index }
                .maxByOrNull { it.index }
        }

        fun getAllLessons(): List<Lesson> = lessons.values.toList()
        fun getAllChapters(): List<Chapter> = chapters
        fun getAllCourses(): List<Course> = courses
        fun getChapterById(id: String): Chapter? = chapters.find { it.id == id }
        fun getCourseById(id: String): Course? = courses.find { it.id == id }
        fun getCurrentCourse(): Course = getCourseById("football-basics") ?: courses.first()

        // Check if a lesson is the last one in its chapter
        fun isLastLessonInChapter(lessonId: String): Boolean {
            val lesson = getLessonById(lessonId) ?: return false
            val chapterLessons = getLessonsForChapter(lesson.chapterId)
            val lastLesson = chapterLessons.maxByOrNull { it.index }
            return lastLesson?.id == lessonId
        }

        // State methods
        fun hasStartedLearning(): Boolean = hasStartedLearning
        fun setStartedLearning(started: Boolean) {
            hasStartedLearning = started
            // In future, save this to SharedPreferences or a database
        }

        fun wasLessonCompletedToday(lessonId: String): Boolean {
            // For now, just check if the lesson is completed
            // In future, we'd also check the completion date
            return completedLessons.contains(lessonId)
        }

        // Get next lesson to complete (for Today's Goal)
        fun getNextLessonToComplete(): Lesson? {
            // First try to find an incomplete lesson in the current course
            val currentCourse = getCurrentCourse()

            for (chapter in currentCourse.chapters) {
                if (chapter.isLocked) continue

                for (lesson in chapter.lessons) {
                    if (!completedLessons.contains(lesson.id)) {
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
                        if (!completedLessons.contains(lesson.id)) {
                            return lesson
                        }
                    }
                }
            }

            return null
        }

        // Check if a chapter has been started
        fun hasStartedChapter(chapterId: String): Boolean {
            // Check if chapter ID is in tracked started chapters
            if (startedChapters.contains(chapterId)) return true

            // Or check if any lessons in the chapter have been started
            val chapterLessons = getLessonsForChapter(chapterId)
            val hasStartedAnyLesson = chapterLessons.any { startedLessons.contains(it.id) }

            // If any lesson has been started, mark the chapter as started too
            if (hasStartedAnyLesson) {
                startedChapters.add(chapterId)
            }

            return hasStartedAnyLesson
        }

        // Get the current or next chapter to study
        fun getCurrentOrNextChapter(): Pair<Course, Chapter>? {
            val currentCourse = getCurrentCourse()

            // Try to find an incomplete chapter in the current course
            val currentChapter = currentCourse.chapters.find {
                !it.isLocked && it.lessons.any { lesson -> !completedLessons.contains(lesson.id) }
            }

            if (currentChapter != null) {
                return Pair(currentCourse, currentChapter)
            }

            // If all chapters in current course are completed or locked, find the next course
            val nextCourse = getAllCourses().find {
                !it.isCompleted && it.id != currentCourse.id
            } ?: return null

            val nextChapter = nextCourse.chapters.find {
                !it.isLocked && it.lessons.any { lesson -> !completedLessons.contains(lesson.id) }
            } ?: return null

            return Pair(nextCourse, nextChapter)
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