package com.example.firstdown.model

import android.util.Log
import com.example.firstdown.R
import com.example.firstdown.utilities.FirestoreManager
import com.example.firstdown.utilities.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    }

    // Local cache
    private val completedLessons = mutableSetOf<String>()
    private val completedQuizzes = mutableSetOf<String>()
    private val completedChapters = mutableSetOf<String>()
    private val completedCourses = mutableSetOf<String>()
    private val startedLessons = mutableSetOf<String>()
    private val startedChapters = mutableSetOf<String>()
    private val coursesCache = mutableMapOf<String, Course>()
    private val chaptersCache = mutableMapOf<String, Chapter>()
    private val lessonsCache = mutableMapOf<String, Lesson>()
    private val allCoursesCache = mutableListOf<Course>()
    private var allCoursesCacheTimestamp = 0L

    private var hasStartedLearning = false
    private var isProgressLoaded = false

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

    private val posts = listOf(
        Post(
            id = "post1",
            userProfileImage = "https://i.pravatar.cc/150?img=1",
            userName = "John Cooper",
            timeAgo = "2 hours ago",
            content = "What's the best way to improve ball control during high-pressure situations? Any drills recommendations?",
            imageUrl = null,
            likes = 24,
            comments = 8
        ),
        Post(
            id = "post2",
            userProfileImage = "https://i.pravatar.cc/150?img=2",
            userName = "Sarah Wilson",
            timeAgo = "5 hours ago",
            content = "Pro tip: Here's a quick drill for improving your first touch. Practice this for 15 minutes daily and you'll see improvement within a week.",
            imageUrl = "https://images.unsplash.com/photo-1566577739112-5180d4bf9390?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8Zm9vdGJhbGwlMjB0cmFpbmluZ3xlbnwwfHwwfHx8MA%3D%3D&w=1000&q=80",
            likes = 56,
            comments = 12
        ),
        Post(
            id = "post3",
            userProfileImage = "https://i.pravatar.cc/150?img=3",
            userName = "Mike Johnson",
            timeAgo = "Yesterday",
            content = "Can someone explain the difference between Cover 2 and Cover 3? I'm trying to understand defensive schemes better.",
            imageUrl = null,
            likes = 18,
            comments = 7
        ),
        Post(
            id = "post4",
            userProfileImage = "https://i.pravatar.cc/150?img=4",
            userName = "Emma Rodriguez",
            timeAgo = "2 days ago",
            content = "Just completed the Quarterback Fundamentals course. The mechanics section really helped me improve my throwing accuracy!",
            imageUrl = "https://images.unsplash.com/photo-1566577739112-5180d4bf9390?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8Zm9vdGJhbGwlMjB0cmFpbmluZ3xlbnwwfHwwfHx8MA%3D%3D&w=1000&q=80",
            likes = 42,
            comments = 5
        )
    )

    private val lessons = mapOf(
        //// Football Basics Course

        // Basic Rules Chapter
        "downs" to Lesson(
            id = "downs",
            chapterId = "basic-rules",
            title = "Downs and Distances",
            description = "Understanding the four downs system",
            contentType = ContentType.TEXT,
            contentData = "In American football, a team has four attempts (downs) to advance the ball 10 yards. If they succeed, they get a new set of downs. If they fail, possession is turned over to the opposing team.",
            imageResId = R.drawable.football_field_bg,
            durationMinutes = 15,
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
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
            isCompleted = false,
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

        // Player Positions Chapter
        "offense" to Lesson(
            id = "offense",
            chapterId = "player-positions",
            title = "Offensive Positions",
            description = "Key offensive player roles",
            contentType = ContentType.TEXT,
            contentData = "The offense consists of various positions including quarterback, running back, wide receiver, tight end, and offensive linemen. Each position has specific responsibilities in moving the ball down the field.",
            imageResId = null,
            durationMinutes = 18,
            isCompleted = false,
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
            isCompleted = false,
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

        // Game Flow Chapter
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

        // Advanced Tactics Chapter
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
        ),

        //// Offensive Playbook Course

        // West Coast Offense Chapter
        "wco-philosophy" to Lesson(
            id = "wco-philosophy",
            chapterId = "west-coast-offense",
            title = "West Coast Philosophy",
            description = "Understanding timing-based passing",
            contentType = ContentType.TEXT,
            contentData = "The West Coast Offense, pioneered by Bill Walsh, uses a short, horizontal passing game as an extension of the running game. The system emphasizes timing, precision, and ball control. Quarterbacks make quick reads and releases, often taking three and five-step drops. Receivers run precise routes with exact timing, allowing the quarterback to throw to spots rather than waiting to see receivers open. The system uses receiver splits, pre-snap motion, and formation variations to create favorable matchups. A successful West Coast Offense controls the clock, moves the chains consistently, and sets up occasional deep shots when defenses compress to stop short passes.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 0
        ),
        "wco-route-concepts" to Lesson(
            id = "wco-route-concepts",
            chapterId = "west-coast-offense",
            title = "West Coast Route Concepts",
            description = "Key passing combinations",
            contentType = ContentType.IMAGE,
            contentData = "West Coast Offense route concepts create horizontal stretches and high-percentage completions. The 'Slant-Flat' combination pairs a quick slant with a flat route to stretch zone defenders horizontally. 'Stick' features a stick route (quick out-then-stop) paired with a flat route. 'Levels' places receivers at different depths on the same side of the field. 'Mesh' creates traffic with crossing routes that hinder man coverage. The 'Drive' concept uses a shallow cross with an in route behind it. Each concept attacks specific coverages by placing multiple receivers in the same zone or creating pick situations against man coverage. Precision timing is essential – quarterbacks often throw before the receiver makes his break.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 28,
            isCompleted = false,
            index = 1
        ),
        "wco-terminology" to Lesson(
            id = "wco-terminology",
            chapterId = "west-coast-offense",
            title = "West Coast Terminology",
            description = "Understanding the complex play calling system",
            contentType = ContentType.TEXT,
            contentData = "The West Coast Offense uses a distinct terminology system for play calling. A typical play call includes formation, motion, protection, and route concept. For example, 'Trips Right, Z Motion, 639 F Swing' tells each player their responsibility. Numbers often denote routes for specific receivers. 'Even' numbers typically indicate routes to the right, while 'odd' numbers go left. Protection calls are integrated into the play name. Learning this detailed terminology requires significant study but allows for precise communication of complex concepts. The verbose play calls initially seem cumbersome but actually provide clear instructions to all eleven offensive players, reducing confusion and mental errors.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 2
        ),
        "wco-progression" to Lesson(
            id = "wco-progression",
            chapterId = "west-coast-offense",
            title = "Quarterback Progressions",
            description = "Reading the field in the West Coast system",
            contentType = ContentType.TEXT,
            contentData = "West Coast quarterbacks use structured progressions to move through reads quickly. Most concepts have a pre-snap read that may alter the progression. The quarterback then works through defined reads – primary, secondary, and check-down options. Reads are often patterned (left to right or high to low) rather than based on specific defenders. Progressions are tied to footwork – first read on the last step of the drop, second read on the first hitch, etc. This disciplined approach ensures the quarterback's eyes and feet work together. Against zone, quarterbacks read zones and throw to openings. Against man, they look for winning one-on-one matchups or pick situations. If all reads are covered, the check-down or scramble becomes the final option.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 3
        ),

        // Spread Offense Chapter
        "spread-philosophy" to Lesson(
            id = "spread-philosophy",
            chapterId = "spread-offense",
            title = "Spread Offense Fundamentals",
            description = "Creating space and exploiting matchups",
            contentType = ContentType.TEXT,
            contentData = "The Spread Offense uses wide receiver alignments to stretch the defense horizontally, creating running lanes and passing windows. By employing three to five receivers, the offense forces the defense to defend the entire field width. This creates favorable numbers in the box for running and exploits one-on-one matchups in the passing game. While many associate the spread with passing, modern spread attacks are often run-heavy, using concepts like the read option. The shotgun formation gives quarterbacks better vision of the field. Tempo is frequently employed to prevent defensive substitutions and adjustments. The system's flexibility allows it to be adapted to various personnel types and skill sets.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 0
        ),
        "rpo-concepts" to Lesson(
            id = "rpo-concepts",
            chapterId = "spread-offense",
            title = "Run-Pass Options",
            description = "Modern packaged plays",
            contentType = ContentType.IMAGE,
            contentData = "Run-Pass Options (RPOs) combine running and passing elements into a single play, allowing the quarterback to decide post-snap. The offensive line blocks for a run play while receivers run pass routes. The quarterback reads a specific defender (often a linebacker or safety) and either hands off or throws based on that defender's action. If the read defender plays the run, the quarterback throws; if he drops for pass coverage, the quarterback hands off. Common RPO concepts include bubble screens, slants, and stick routes paired with inside zone or power runs. RPOs put defenders in no-win situations and prevent defenses from being right regardless of their call. Timing is crucial – passes must be thrown quickly to avoid ineligible receiver downfield penalties.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 25,
            isCompleted = false,
            index = 1
        ),
        "option-running" to Lesson(
            id = "option-running",
            chapterId = "spread-offense",
            title = "Option Running Game",
            description = "Reading defenders in the run game",
            contentType = ContentType.TEXT,
            contentData = "Option running plays give the quarterback decision-making responsibility based on defensive reactions. The zone read involves the quarterback reading an unblocked edge defender and either keeping or handing off. The triple option adds a pitch phase, creating three possible ball carriers. The midline read focuses on an interior defender. In all option plays, the quarterback must make quick decisions based on the movement of unblocked defenders. The mesh point (where the quarterback and running back meet) requires precise timing and ball handling. Option plays create numerical advantages by essentially removing the read defender from the play. They're particularly effective against aggressive, penetrating defenses because they punish defenders for committing too quickly.",
            imageResId = null,
            durationMinutes = 28,
            isCompleted = false,
            index = 2
        ),
        "tempo" to Lesson(
            id = "tempo",
            chapterId = "spread-offense",
            title = "Tempo and No-Huddle",
            description = "Controlling game pace for advantage",
            contentType = ContentType.TEXT,
            contentData = "No-huddle tempo creates advantages by limiting defensive substitutions, preventing adjustment time, and creating fatigue. Teams use visual sideline signals, wristbands, or simple verbal codes to communicate plays quickly. Tempo can vary – ultra-fast (snapping immediately), controlled (waiting for defensive alignment before snapping), or sugar huddle (brief gathering before alignment). Effective tempo requires simplified communication systems, conditioned players, and quarterbacks who can process information quickly. Linemen must be able to recognize fronts without coach input. Teams practice specific tempos for different situations – accelerating when the defense appears confused or slowing when their own defense needs rest. Strategic tempo changes throughout the game often create more advantage than constant speed.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 3
        ),

        // Red Zone Offense Chapter
        "red-zone-challenges" to Lesson(
            id = "red-zone-challenges",
            chapterId = "red-zone-offense",
            title = "Red Zone Challenges",
            description = "Adapting to compressed space",
            contentType = ContentType.TEXT,
            contentData = "The red zone (inside the opponent's 20-yard line) presents unique challenges for offenses. The compressed vertical space eliminates deep threats and allows defenders to be more aggressive. Passing windows become smaller as defenders have less ground to cover. Timing routes must be adjusted due to shorter depths. Defenses often employ different coverages and more complex blitzes in the red zone. The back of the end zone serves as an extra defender. Field goals become a viable alternative, affecting risk-reward calculations for play calling. Success in this area often determines game outcomes, as teams must convert opportunities into touchdowns rather than field goals. Effective red zone offenses plan specifically for this area rather than simply running their standard offense.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 0
        ),
        "red-zone-run" to Lesson(
            id = "red-zone-run",
            chapterId = "red-zone-offense",
            title = "Red Zone Running Game",
            description = "Power football near the goal line",
            contentType = ContentType.TEXT,
            contentData = "The red zone running game emphasizes power and deception rather than speed. Inside runs become more effective as defenses must respect the entire width of the field. Gap scheme runs (Power, Counter) are particularly effective due to their downhill nature. The quarterback run becomes a valuable tool near the goal line, using the extra blocker advantage. Short-yardage packages often feature extra tight ends or fullbacks for additional blockers. Play-action is highly effective in the red zone as defenders must respect the run. Motion and shifts help identify defensive coverages before the snap. When inside the 5-yard line, leverage and pad level become even more crucial. Goal line packages often use defensive linemen as blockers for maximum push.",
            imageResId = null,
            durationMinutes = 18,
            isCompleted = false,
            index = 1
        ),
        "red-zone-pass" to Lesson(
            id = "red-zone-pass",
            chapterId = "red-zone-offense",
            title = "Red Zone Passing Concepts",
            description = "High-percentage throws for scoring",
            contentType = ContentType.IMAGE,
            contentData = "Red zone passing attacks use specific concepts to counter compressed space. 'Fade' routes to tall receivers exploit height mismatches. 'Rub' or 'pick' concepts create natural separation against man coverage. 'Flood' concepts put three receivers at different depths to one side. 'Mesh' crossers create traffic for defensive backs. 'Levels' concepts horizontally stretch zone defenses. Back-shoulder throws become valuable when defenders have good position. Quarterbacks must make faster decisions and accurate throws with less margin for error. Route depth is compressed but timing remains critical. Effective red zone offenses use formations and motion to identify coverage, then attack specific weaknesses. The throwing lanes are smaller, requiring precise ball placement, especially on inside throws.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 25,
            isCompleted = false,
            index = 2
        ),
        "situational-strategy" to Lesson(
            id = "situational-strategy",
            chapterId = "red-zone-offense",
            title = "Situational Strategy",
            description = "Decision-making based on game context",
            contentType = ContentType.TEXT,
            contentData = "Red zone strategy must adapt to game situation. Early in games, conservative approaches may be appropriate to ensure points. In close games, the risk-reward calculation changes based on score differential and time remaining. Two-point conversion planning affects play selection inside the 5-yard line. Teams often have 'must have' calls for critical situations (third down, fourth down, two-point conversions). Clock management becomes crucial in end-of-half or end-of-game situations. The opponent's defensive tendencies inform strategy – teams attack what that specific defense struggles to defend in the red zone. Field conditions and weather also influence red zone strategy. The best offensive coordinators prepare specific packages for different red zone scenarios and clearly communicate situational priorities to players before critical plays.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 3
        ),

        //// Quarterback Fundamentals Course

        // Mechanics Chapter
        "stance-grip" to Lesson(
            id = "stance-grip",
            chapterId = "mechanics",
            title = "Stance and Grip",
            description = "Foundational elements of quarterback play",
            contentType = ContentType.IMAGE,
            contentData = "The quarterback's stance begins with feet shoulder-width apart, with the throwing-side foot slightly back. Weight should be balanced on the balls of the feet with knees slightly bent. In the shotgun, the QB faces the center with hands extended to receive the snap. Under center, the hands should be directly under the center with the dominant hand receiving the ball. The proper grip positions fingers across the laces with the thumb and index finger creating a 'V' shape. The ball should be held with the fingertips rather than palmed. Pressure comes primarily from the thumb, index, and middle fingers. The pinky provides stability. The wrist should remain loose for proper motion during the throw. Hand size affects optimal grip – smaller hands may require adjustments to the standard grip.",
            imageResId = R.drawable.qb_stance,
            durationMinutes = 18,
            isCompleted = false,
            index = 0
        ),
        "footwork" to Lesson(
            id = "footwork",
            chapterId = "mechanics",
            title = "Dropback Footwork",
            description = "Proper movement from center",
            contentType = ContentType.TEXT,
            contentData = "Quarterbacks use various dropback depths based on the play design: 3-step (quick game), 5-step (intermediate), and 7-step (deep). From under center, the first step is always with the throwing-side foot, then crossover with the non-throwing foot. Steps should be quick and controlled, gaining depth while maintaining balance. From shotgun, the quarterback may use a catch step (step back upon receiving snap) or immediate read step. Footwork must be synchronized with the timing of route concepts. The drop should finish with feet at shoulder width and weight balanced, ready to throw. After the drop, the QB may need to shuffle or reset feet to adjust to pressure or receiver timing. In bootlegs or rollouts, the initial steps establish depth before turning to run parallel to the line of scrimmage. The plant foot (non-throwing side) must be pointed toward the target for accurate throws.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 1
        ),
        "throwing-motion" to Lesson(
            id = "throwing-motion",
            chapterId = "mechanics",
            title = "Throwing Mechanics",
            description = "Proper throwing technique for accuracy and power",
            contentType = ContentType.TEXT,
            contentData = "Efficient throwing mechanics generate power from the ground up, transferring energy through the body. The sequence begins with the lower body – stride toward the target with the non-throwing foot. Hip rotation generates rotational power. The upper body follows with shoulder rotation, keeping the elbow at or above shoulder height. The throwing motion involves the elbow leading forward, followed by arm extension and wrist snap. Proper follow-through has the thumb ending toward the opposite hip. Common errors include 'arm throwing' without lower body involvement, dropping the elbow (causing inaccurate passes), and over-striding. Release point should be consistent for accuracy. Quarterbacks should practice throwing from various platforms as game situations often prevent perfect mechanics. Maintaining balance throughout the throw is crucial, even when adjusting to pressure or moving in the pocket.",
            imageResId = null,
            durationMinutes = 30,
            isCompleted = false,
            index = 2
        ),
        "ball-handling" to Lesson(
            id = "ball-handling",
            chapterId = "mechanics",
            title = "Ball Handling Skills",
            description = "Clean exchanges and play fakes",
            contentType = ContentType.TEXT,
            contentData = "Ball handling is crucial for quarterbacks, particularly in under-center exchanges and handoffs. For the snap, hands should form a pocket with thumbs and index fingers making a 'V'. The quarterback should apply pressure primarily with the thumbs and fingers, not the palms. For handoffs, the quarterback creates a pocket with the arm opposite the running back's path. Inside handoffs require the QB to open the torso toward the back while extending the ball. Outside handoffs involve extending the ball while keeping shoulders square downfield. Play-action fakes require selling the handoff with full extension and hip/shoulder movement as if completing a real handoff. Ball security during movement in the pocket requires keeping two hands on the ball until the throwing motion begins. The quarterback should protect the ball with both hands when moving in traffic or when pressure is imminent.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 3
        ),

        // Mental Game Chapter
        "pre-snap-reads" to Lesson(
            id = "pre-snap-reads",
            chapterId = "mental-game",
            title = "Pre-Snap Reads",
            description = "Analyzing defenses before the ball is snapped",
            contentType = ContentType.IMAGE,
            contentData = "Pre-snap reads help quarterbacks anticipate defensive coverage and pressure. Begin by counting the defensive box – typically six for two-high safety looks and seven or eight for single-high. Identify the safeties – two deep likely indicates Cover 2, Cover 4, or Cover 6, while one deep suggests Cover 1 or Cover 3. Cornerback alignment offers clues – press coverage often indicates man or Cover 3, while off coverage suggests zone. Look for blitz indicators like walked-up linebackers, defensive back alignment close to the line, or defensive line shifts. Motion can help confirm coverage – defenders following a motioned player suggests man coverage. Leverage (inside or outside positioning) of defensive backs indicates their coverage responsibility. The quarterback should communicate protection adjustments based on these reads and may change the play if the defense presents an unfavorable look for the called play.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 28,
            isCompleted = false,
            index = 0
        ),
        "progression-reading" to Lesson(
            id = "progression-reading",
            chapterId = "mental-game",
            title = "Progression Reading",
            description = "Moving through receiver options efficiently",
            contentType = ContentType.TEXT,
            contentData = "Quarterbacks must work through receiver progressions systematically based on the play design. Most plays establish a pre-determined sequence – primary, secondary, and check-down options. 'Full-field' reads work from one side to the other, while 'half-field' reads focus on two or three receivers to one side. Some progressions are based on 'high-to-low' or 'low-to-high' rather than specific receivers. Disciplined eye control prevents defenders from anticipating the throw destination. Eye manipulation can move defenders away from the intended target area. For zone coverage, quarterbacks read the reaction of specific defenders rather than focusing solely on receivers. Against man coverage, they look for receivers winning their individual matchups. If the progression completes without an open receiver, the QB must check down to the safety valve or scramble. Progressions must synchronize with protection – deeper reads require more time, which may not be available against certain pressures.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 1
        ),
        "protection-recognition" to Lesson(
            id = "protection-recognition",
            chapterId = "mental-game",
            title = "Protection Recognition",
            description = "Identifying and adjusting to pressure",
            contentType = ContentType.TEXT,
            contentData = "Quarterbacks must understand protection schemes to identify potential free rushers. The first step is identifying the 'Mike' linebacker, which establishes blocking assignments for the offensive line and backs. In most protections, five or six blockers account for the defensive linemen and designated linebackers. By counting rushers and blockers, the QB can identify potential unblocked defenders. When the defense shows more rushers than blockers, the quarterback must recognize 'hot' routes – short, quick-breaking patterns by receivers who adjust their routes against blitzes. Pass protection adjustments ('checks') can be made by shifting protection direction, adding blockers, or changing the play entirely. Quarterbacks must understand which receivers will adjust their routes against blitzes and where the quick throw should go. Recognizing the difference between simulated pressure (rushers dropping out) and actual blitzes comes with experience and film study. Communicating adjustments clearly to linemen, backs, and receivers ensures everyone understands their responsibilities.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 2
        ),
        "game-management" to Lesson(
            id = "game-management",
            chapterId = "mental-game",
            title = "Game Management",
            description = "Controlling tempo, clock, and situations",
            contentType = ContentType.TEXT,
            contentData = "Quarterbacks are responsible for various game management aspects beyond throwing. Clock management involves understanding when to hurry (two-minute drill) or slow down (protecting a lead). Situational awareness means recognizing down, distance, score, and time remaining to inform decision-making. Managing the play clock requires efficient communication and awareness to avoid delay penalties. Tempo control involves speeding up or slowing down based on the game plan and situation. Pre-snap adjustments ('audibles') change plays when the defense presents unfavorable looks. Calling protections identifies blocking assignments for linemen and backs. Managing timeouts preserves them for critical moments. Red zone and third-down situational awareness requires understanding higher-percentage options. Two-minute drill execution demands quick thinking and precise communication. Understanding game situations affects risk assessment – when to take chances versus when to be conservative based on score, time, and field position.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 3
        ),

        // Leadership Chapter
        "huddle-command" to Lesson(
            id = "huddle-command",
            chapterId = "leadership",
            title = "Huddle Command",
            description = "Effective communication and presence",
            contentType = ContentType.TEXT,
            contentData = "The quarterback's huddle presence sets the tone for the entire offense. Effective huddle command begins with clear, confident voice projection so all players can hear the play call. The quarterback should stand tall, make eye contact, and project confidence regardless of the game situation. Play calls should be delivered with a consistent cadence and rhythm to aid memorization. Complex terminology requires precise articulation. The QB should break the huddle with authority and positive reinforcement. In no-huddle situations, communication shifts to hand signals, one-word calls, or wristband numbers, but the need for clarity and confidence remains. The best quarterbacks maintain composed body language even after mistakes or in challenging situations. When making checks or audibles at the line, the quarterback must ensure all players understand the adjustment. Establishing command early in games and practice builds team confidence in the quarterback's leadership.",
            imageResId = null,
            durationMinutes = 15,
            isCompleted = false,
            index = 0
        ),
        "teammate-relationships" to Lesson(
            id = "teammate-relationships",
            chapterId = "leadership",
            title = "Building Teammate Relationships",
            description = "Developing trust and communication",
            contentType = ContentType.TEXT,
            contentData = "Successful quarterbacks develop strong relationships with teammates both on and off the field. With receivers, extra practice sessions build timing and trust while establishing non-verbal communication for route adjustments. With offensive linemen, showing appreciation and understanding their challenges builds a protective mindset. Regular communication with the center ensures clean snaps and shared protection responsibilities. Relationships with running backs involve understanding their running styles and preferences for handoff placement. The quarterback should learn each teammate's communication style – some respond to encouragement, others to direct feedback. Taking responsibility for mistakes rather than blaming others builds respect. Organizing off-field activities and study sessions strengthens bonds. During games, the quarterback should maintain consistent communication with each position group – providing information about what they're seeing and adjustments needed. The quarterback's treatment of all teammates, regardless of status, sets the tone for team culture.",
            imageResId = null,
            durationMinutes = 18,
            isCompleted = false,
            index = 1
        ),
        "adversity-management" to Lesson(
            id = "adversity-management",
            chapterId = "leadership",
            title = "Managing Adversity",
            description = "Leading through challenges",
            contentType = ContentType.TEXT,
            contentData = "Quarterbacks must lead effectively through difficult situations. After turnovers, the quarterback should demonstrate resilience and refocus the team on the next opportunity. During losing streaks, maintaining consistent work habits and positive messaging prevents spiraling negativity. When offensive struggles occur, the quarterback must project confidence while making constructive adjustments. Handling media criticism requires balancing accountability with forward focus. When teammates make mistakes, the quarterback should provide immediate supportive communication while reinforcing correct execution. Physical adversity (playing through minor injuries) demonstrates toughness but requires honest communication with coaches about limitations. The quarterback sets the example for resilience – team resilience rarely exceeds quarterback resilience. During in-game adversity, maintaining composed body language and communication prevents emotional contagion that can derail performance. The best quarterbacks frame adversity as an opportunity to demonstrate character and mental toughness rather than a threat.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 2
        ),
        "coach-relationship" to Lesson(
            id = "coach-relationship",
            chapterId = "leadership",
            title = "Coach-Quarterback Relationship",
            description = "Effective partnership with coaches",
            contentType = ContentType.TEXT,
            contentData = "The relationship between quarterback and coaches is crucial for offensive success. Effective communication with the coordinator involves understanding the why behind play calls and game plans. Regular meetings with the quarterback coach should address mechanical adjustments and mental approach. The quarterback must balance respectfully providing input with accepting coaching. Quarterbacks should communicate what they see on the field while being open to the broader perspective coaches provide. Gametime communication should be efficient and solution-focused. Disagreements should be handled privately rather than visibly on the sideline. The quarterback often serves as the bridge between coaches and other players, translating coaching points to teammates. Building trust happens through consistent preparation, honest communication about preferences and concerns, and demonstrating improvement in targeted areas. The best quarterback-coach relationships involve mutual respect and a continuous feedback loop, with both parties focused on team success rather than individual credit.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 3
        ),

        // Advanced Concepts Chapter
        "creating-leverage" to Lesson(
            id = "creating-leverage",
            chapterId = "advanced-concepts",
            title = "Creating Throwing Leverage",
            description = "Advanced ball placement and timing",
            contentType = ContentType.TEXT,
            contentData = "Elite quarterbacks create leverage advantages through precise ball placement and timing. Throwing 'receivers open' means placing the ball away from defenders even when the receiver appears covered. Back-shoulder throws counter tight downfield coverage by placing the ball behind the receiver, away from the defender. High-point throws take advantage of taller receivers by placing the ball where only they can reach it. Anticipation throws involve releasing the ball before the receiver's break, arriving as they create separation. Against zone coverage, throwing to spatial windows between defenders maximizes yards-after-catch opportunity. Leading receivers properly requires understanding their speed and acceleration. When throwing to the sideline, proper leverage places the ball toward the sideline, using it as an extra defender. In contested catch situations, the quarterback must give the receiver a chance while minimizing interception risk. These advanced placement concepts require extensive practice and trust between quarterback and receivers.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 0
        ),
        "pocket-movement" to Lesson(
            id = "pocket-movement",
            chapterId = "advanced-concepts",
            title = "Pocket Movement Skills",
            description = "Navigating pressure while maintaining throwing posture",
            contentType = ContentType.IMAGE,
            contentData = "Effective pocket movement extends plays while maintaining the ability to throw accurately. Subtle pocket movement involves small adjustments to avoid rushers while staying in the pocket – 'climbing' steps up to avoid edge pressure or side steps to create passing lanes. When pressure forces larger movements, the quarterback must keep eyes downfield rather than focusing on rushers. Scrambling outside the pocket requires maintaining proper throwing mechanics despite movement. The 'escape hatch' concept identifies pre-snap exit routes based on protection schemes. Ball security is paramount during movement – two hands on the ball until ready to throw. Rushing opportunities should be taken when significant yards are available, but quarterbacks must recognize when to slide or get out of bounds to avoid unnecessary hits. Pocket presence – the awareness of pressure without seeing it – develops through experience and peripheral vision training. Elite pocket movement buys time for receivers to get open while maintaining balance and ready-to-throw positioning.",
            imageResId = R.drawable.quarterback_bg,
            durationMinutes = 28,
            isCompleted = false,
            index = 1
        ),
        "field-manipulation" to Lesson(
            id = "field-manipulation",
            chapterId = "advanced-concepts",
            title = "Manipulating Defenders",
            description = "Using eyes and body to move defenders",
            contentType = ContentType.TEXT,
            contentData = "Advanced quarterbacks manipulate defenders to create throwing opportunities. Eye manipulation involves looking off safeties or linebackers to create openings elsewhere – holding defenders in place or moving them away from the intended target. Pump fakes cause defenders to commit, creating separation for receivers. Body positioning, including shoulder orientation, influences defender reads of the quarterback's intentions. Against zone defenses, looking at one area causes zone defenders to cheat that direction, opening windows elsewhere. Against man coverage, eye discipline prevents help defenders from breaking on the throw. In the red zone, manipulation is especially effective due to compressed space. No-look passes – throwing while looking elsewhere – represent the highest level of manipulation but require absolute certainty about receiver location. These techniques require practice in game-like conditions and should be used selectively rather than on every throw. Understanding defender responsibilities in various coverages allows targeted manipulation of specific players within the defensive scheme.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 2
        ),
        "film-study" to Lesson(
            id = "film-study",
            chapterId = "advanced-concepts",
            title = "Effective Film Study",
            description = "Analyzing opponents and self-scouting",
            contentType = ContentType.TEXT,
            contentData = "Film study gives quarterbacks a competitive edge through preparation. Opponent study should identify coverage tendencies based on down, distance, and formation. Blitz tendencies reveal when and from where pressure is likely to come. Coverage tells (pre-snap indicators) help anticipate defensive responses. Red zone and third-down tendencies deserve special attention as high-leverage situations. Self-scouting involves reviewing one's own film to identify mechanical issues and decision-making patterns. The quarterback should track both successful and unsuccessful plays to understand why each worked or failed. Film study should include detailed note-taking and simulation – mentally rehearsing reads and decisions. Advanced quarterbacks create a tendency database for opponents and key defensive players. Study should combine full-game review for context and cut-ups organized by situation or concept. The best quarterbacks connect film study to practice, working on specific aspects identified in review. When studying with receivers, quarterbacks should focus on coverage recognition and timing details to build shared understanding.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 3
        ),

        //// Offensive Strategies Course

        // Run Game Chapter
        "run-philosophy" to Lesson(
            id = "run-philosophy",
            chapterId = "run-game",
            title = "Running Game Philosophy",
            description = "Understanding the importance of the run game",
            contentType = ContentType.TEXT,
            contentData = "The running game is fundamental to football success. A strong running attack controls the clock, wears down the defense, and sets up play-action passes. Teams must establish an identity - power running or zone schemes. Power running focuses on straight-ahead blocking and physicality, while zone running relies on runner vision and lateral movement. The running game also impacts defensive alignment - successful runs often lead to defenses bringing safeties closer to the line, opening opportunities for deep passes.",
            imageResId = null,
            durationMinutes = 15,
            isCompleted = false,
            index = 0
        ),
        "inside-zone" to Lesson(
            id = "inside-zone",
            chapterId = "run-game",
            title = "Inside Zone Runs",
            description = "Core zone running play concepts",
            contentType = ContentType.IMAGE,
            contentData = "The Inside Zone is a foundational run play in modern football. Offensive linemen block zones rather than specific defenders, stepping playside and working together with combination blocks. The running back aims for the outside hip of the guard but reads the defensive flow to find cutback lanes. Keys to success include double-team blocks at the point of attack and linemen reaching the second level to block linebackers. The running back must be decisive with one cut and get upfield quickly once they identify the running lane.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 22,
            isCompleted = false,
            index = 1
        ),
        "outside-zone" to Lesson(
            id = "outside-zone",
            chapterId = "run-game",
            title = "Outside Zone Runs",
            description = "Stretching the defense horizontally",
            contentType = ContentType.TEXT,
            contentData = "Outside Zone (or Stretch) plays attack the perimeter of the defense. Offensive linemen take lateral steps toward the sideline, trying to reach the outside shoulder of defenders. The running back aims for the outside hip of the tackle but looks for cutback lanes as they move laterally. The defense is stretched horizontally, often creating vertical seams. This play requires athletic linemen who can move laterally and seal defenders. Running backs need excellent vision and the ability to make one decisive cut upfield when they find a lane.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 2
        ),
        "gap-schemes" to Lesson(
            id = "gap-schemes",
            chapterId = "run-game",
            title = "Gap Scheme Runs",
            description = "Power and counter running plays",
            contentType = ContentType.TEXT,
            contentData = "Gap scheme runs, like Power and Counter, create new running lanes by pulling offensive linemen. In Power, a backside guard pulls to lead through the hole while playside linemen down-block. Counter adds deception by having the running back take initial steps away from the play direction before following pulling linemen. These plays create a numbers advantage at the point of attack. They require physical linemen who can pull effectively and running backs who can follow blockers patiently. These plays are especially effective in short-yardage situations.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 3
        ),

        // Pass Game Chapter
        "pass-concepts" to Lesson(
            id = "pass-concepts",
            chapterId = "pass-game",
            title = "Passing Concepts",
            description = "Understanding route combinations",
            contentType = ContentType.IMAGE,
            contentData = "Modern passing attacks use route combinations to stress defenses. Common concepts include Smash (corner route with underneath hitch), Flood (three receivers at different depths to one side), Mesh (crossing routes at shallow depth), and Four Verticals (multiple deep threats). These concepts are designed to beat either man or zone coverage by creating natural picks, stretching zones vertically/horizontally, or isolating defenders in mismatches. Quarterbacks read these concepts progressively, looking from primary to secondary receivers based on the defensive coverage.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 30,
            isCompleted = false,
            index = 0
        ),
        "progression-reads" to Lesson(
            id = "progression-reads",
            chapterId = "pass-game",
            title = "Quarterback Progressions",
            description = "How to read the field systematically",
            contentType = ContentType.TEXT,
            contentData = "Quarterbacks must work through progressions to find open receivers. Most plays have a pre-snap read that may cause the QB to alter the progression. Standard progressions move from primary to secondary to check-down receivers. Full-field reads scan from one side to the other, while half-field reads focus on two or three receivers to one side. High-low reads involve watching a single defender and throwing to whichever receiver he doesn't cover. The progression changes based on defensive coverage - man and zone coverages create different open windows.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 1
        ),
        "play-action" to Lesson(
            id = "play-action",
            chapterId = "pass-game",
            title = "Play-Action Passing",
            description = "Using run fakes to create passing opportunities",
            contentType = ContentType.TEXT,
            contentData = "Play-action passes use run fakes to draw defenders toward the line of scrimmage, creating opportunities downfield. These plays begin with run-blocking by the offensive line and a convincing fake handoff by the quarterback. The deception creates time for deeper routes to develop. Linebackers and safeties who step toward the line of scrimmage create voids in coverage behind them. Play-action is most effective when a team has established a credible running threat. The quarterback must sell the fake handoff with proper footwork and ball handling.",
            imageResId = null,
            durationMinutes = 18,
            isCompleted = false,
            index = 2
        ),
        "protection-schemes" to Lesson(
            id = "protection-schemes",
            chapterId = "pass-game",
            title = "Protection Schemes",
            description = "Keeping the quarterback safe",
            contentType = ContentType.TEXT,
            contentData = "Pass protection is vital for successful passing attacks. Basic schemes include 5-man protection (all linemen), 6-man protection (adding a tight end or back), and 7-man max protection (keeping two additional blockers in). Slide protection shifts the line to one side with the back responsible for the backside. Man protection assigns blockers to specific defenders. Identifying the 'middle' linebacker (Mike) helps set protection by defining blocking assignments. Proper protection requires communication between the quarterback, center, and the entire offensive line.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 3
        ),

        // Game Planning Chapter
        "opponent-analysis" to Lesson(
            id = "opponent-analysis",
            chapterId = "game-planning",
            title = "Opponent Analysis",
            description = "Breaking down defensive tendencies",
            contentType = ContentType.TEXT,
            contentData = "Effective game planning begins with thorough opponent analysis. This involves studying game film to identify defensive tendencies based on down and distance, formation, and game situation. Analysts track defensive fronts, coverage schemes, blitz patterns, and personnel groupings. Tendencies are categorized into situations: first down, second-and-long, third-and-short, red zone, etc. The goal is to predict what defense will be played in each situation and design plays that exploit weaknesses. Software tools help compile and sort data to identify statistical patterns.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 0
        ),
        "game-script" to Lesson(
            id = "game-script",
            chapterId = "game-planning",
            title = "Game Script Development",
            description = "Planning your offensive approach",
            contentType = ContentType.TEXT,
            contentData = "A game script is a pre-planned sequence of plays for different situations. Most teams script the first 15-20 offensive plays to evaluate defensive responses. Scripts are also developed for specific situations like two-minute drills, third downs, and red zone. The script considers factors like field position, defensive tendencies, and desired tempo. While coaches deviate from scripts based on game flow, having a pre-planned approach prevents decision paralysis and ensures that core concepts are used. The best scripts include built-in adjustments for various defensive responses.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 1
        ),
        "in-game-adjustments" to Lesson(
            id = "in-game-adjustments",
            chapterId = "game-planning",
            title = "In-Game Adjustments",
            description = "Adapting to defensive changes",
            contentType = ContentType.TEXT,
            contentData = "Games are often won or lost based on in-game adjustments. Coaches must recognize when defensive strategies change and adapt accordingly. Adjustments can involve play selection, formation changes, protection modifications, or even tempo adjustments. Communication is critical - information from players on the field and coaches in the booth helps identify issues. Technology helps with this process, as coaches review tablets showing play images. Halftime provides a key opportunity for major adjustments when initial game plans aren't working. The best coaches prepare contingency plans for various defensive approaches.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 2
        ),

        //// Defensive Strategies Course

        // Defensive Line Techniques Chapter
        "dl-stances" to Lesson(
            id = "dl-stances",
            chapterId = "defensive-line",
            title = "Defensive Line Stances",
            description = "Proper starting positions",
            contentType = ContentType.TEXT,
            contentData = "A proper defensive lineman stance provides explosiveness and balance. The three-point stance (two feet, one hand) is most common, with weight forward on the balls of the feet. Hand placement should be directly below the shoulder with the arm straight. The back should be flat and the head up, with eyes on the offensive line keys. The four-point stance (two hands down) provides more power but less mobility and vision. Stagger between feet varies by technique - wider stagger for edge rushers, narrower for interior linemen. Pre-snap movement must be avoided to prevent offside penalties.",
            imageResId = null,
            durationMinutes = 15,
            isCompleted = false,
            index = 0
        ),
        "gap-techniques" to Lesson(
            id = "gap-techniques",
            chapterId = "defensive-line",
            title = "Gap and Technique Assignments",
            description = "Understanding line responsibilities",
            contentType = ContentType.IMAGE,
            contentData = "Defensive line alignments are described using 'techniques' that indicate position relative to offensive linemen. Even numbers (0, 2, 4, 6, 8) indicate alignment directly over an offensive lineman, while odd numbers (1, 3, 5, 7, 9) indicate alignment in gaps between linemen. For example, 0-technique aligns directly over the center, while 3-technique aligns outside shoulder of the guard. Gaps are labeled alphabetically from the center outward (A-gap between center and guard, B-gap between guard and tackle, etc.). Assignments combine technique and responsibility - a 3-technique might be responsible for the B-gap.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 25,
            isCompleted = false,
            index = 1
        ),
        "pass-rush-moves" to Lesson(
            id = "pass-rush-moves",
            chapterId = "defensive-line",
            title = "Pass Rush Techniques",
            description = "Moves to defeat blockers",
            contentType = ContentType.TEXT,
            contentData = "Effective pass rushers use various moves to defeat blockers. The bull rush uses straight-ahead power to push the blocker backward. The swim move involves swinging one arm over the blocker's shoulder while using the other to push their shoulder down. The rip move drives the inside arm up through the blocker's armpit while turning the hips toward the quarterback. The spin move is a quick rotation around the blocker. Counters are secondary moves used when the initial move is stopped. Elite pass rushers set up blockers by using patterns of moves throughout the game.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 2
        ),
        "run-defense" to Lesson(
            id = "run-defense",
            chapterId = "defensive-line",
            title = "Defending the Run",
            description = "Techniques for stopping run plays",
            contentType = ContentType.TEXT,
            contentData = "Defending the run requires specific techniques from defensive linemen. Two-gap responsibility means controlling the blocker and handling gaps on either side. One-gap responsibility means penetrating a specific gap to disrupt the backfield. Against zone runs, linemen must maintain leverage and not get reached (blocked on the outside shoulder). Against gap scheme runs, recognizing pulling linemen is crucial to defeating blocks. Defensive linemen must maintain proper pad level (staying lower than offensive linemen) to maintain leverage. Proper hand placement inside the blocker's chest provides control and the ability to shed blocks when the runner approaches.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 3
        ),

        // Linebacker Play Chapter
        "lb-reads" to Lesson(
            id = "lb-reads",
            chapterId = "linebacker-play",
            title = "Linebacker Keys and Reads",
            description = "What to watch before and after the snap",
            contentType = ContentType.TEXT,
            contentData = "Linebackers must read keys to diagnose plays quickly. Pre-snap, they study backfield alignment, line splits, and tight end position. Post-snap, they read the offensive line's first movement - high hat (standing up) indicates pass, low hat (drive blocking) indicates run. For inside linebackers, the guard is often the primary key. For outside linebackers, the tackle or tight end serves as the key. On run plays, linebackers must identify the blocking scheme and fill the proper gap. On pass plays, they must recognize route concepts and understand their coverage responsibilities.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 0
        ),
        "gap-control" to Lesson(
            id = "gap-control",
            chapterId = "linebacker-play",
            title = "Gap Control and Run Fits",
            description = "Maintaining defensive integrity against the run",
            contentType = ContentType.IMAGE,
            contentData = "Gap control defense assigns each defender a specific gap responsibility. Linebackers must understand the entire defensive front to know their assignments in each call. Run fits describe how defenders align to account for all gaps. Inside linebackers often have A or B gap responsibilities, while outside linebackers typically handle C gaps or contain responsibilities. When defensive linemen have two-gap responsibilities, linebackers can flow more freely to the ball. Against zone runs, linebackers must flow laterally while maintaining gap integrity. Against gap scheme runs, they must recognize pulling linemen and adjust their fits accordingly.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 25,
            isCompleted = false,
            index = 1
        ),
        "coverage-responsibilities" to Lesson(
            id = "coverage-responsibilities",
            chapterId = "linebacker-play",
            title = "Coverage Responsibilities",
            description = "Defending the pass from the second level",
            contentType = ContentType.TEXT,
            contentData = "Linebackers have varied coverage responsibilities depending on the defensive call. In man coverage, they typically cover tight ends, running backs, or occasionally slot receivers. In zone coverage, they may have hook/curl, flat, or middle responsibilities. Pattern-matching coverage requires them to pick up receivers entering their zone based on route combinations. Linebackers must recognize route concepts that target the middle of the field (like mesh, dig, and seam routes). They must also understand how to use proper leverage based on their help – inside leverage with outside help, outside leverage with inside help.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 2
        ),
        "blitzing" to Lesson(
            id = "blitzing",
            chapterId = "linebacker-play",
            title = "Blitzing Techniques",
            description = "Pressuring the quarterback",
            contentType = ContentType.TEXT,
            contentData = "Linebacker blitzes can disrupt offensive timing and pressure the quarterback. A-gap blitzes attack between center and guard, while B-gap blitzes come between guard and tackle. Edge blitzes from outside linebackers attack around the offensive tackle. Timing is crucial – blitzing too early reveals the pressure, while blitzing too late reduces effectiveness. Linebackers must understand how to disguise their blitzes and use proper angles of attack. When encountering running backs in protection, linebackers should use power moves for larger backs and speed for smaller backs. Stunts and twists coordinate multiple blitzers to confuse blocking schemes.",
            imageResId = null,
            durationMinutes = 18,
            isCompleted = false,
            index = 3
        ),

        // Secondary Play Chapter
        "cornerback-techniques" to Lesson(
            id = "cornerback-techniques",
            chapterId = "secondary-play",
            title = "Cornerback Techniques",
            description = "Coverage fundamentals for corners",
            contentType = ContentType.TEXT,
            contentData = "Cornerbacks use various techniques depending on the coverage call. Press coverage involves physical contact at the line of scrimmage to disrupt timing. Off coverage starts with a cushion to prevent deep passes. In man coverage, corners must maintain proper leverage and use the sideline as an extra defender. In zone coverage, corners must understand their area responsibility while reading the quarterback. Proper backpedal technique allows corners to maintain position while moving backward. The 'shuffle' technique helps maintain position against vertical routes. When transitioning from backpedal to running with a receiver, the 'flip' technique maintains speed and vision.",
            imageResId = null,
            durationMinutes = 25,
            isCompleted = false,
            index = 0
        ),
        "safety-play" to Lesson(
            id = "safety-play",
            chapterId = "secondary-play",
            title = "Safety Responsibilities",
            description = "The last line of defense",
            contentType = ContentType.TEXT,
            contentData = "Safeties have diverse responsibilities in modern defenses. Free safeties typically have more coverage responsibility, often playing deep middle or half-field zones. Strong safeties often play closer to the line of scrimmage with run support duties. In Cover 1, a single high safety must cover the deep middle while other defenders play man coverage. In Cover 2, two deep safeties each cover half the field. In Cover 3, safeties join a cornerback in dividing the deep field into thirds. Safeties must master proper angles both in coverage and tackling. Communication is crucial as safeties often make coverage adjustments for the entire secondary.",
            imageResId = null,
            durationMinutes = 22,
            isCompleted = false,
            index = 1
        ),
        "pattern-matching" to Lesson(
            id = "pattern-matching",
            chapterId = "secondary-play",
            title = "Pattern-Matching Coverage",
            description = "Modern hybrid coverages",
            contentType = ContentType.IMAGE,
            contentData = "Pattern-matching combines elements of man and zone coverage. Defenders are responsible for zones but pick up specific receivers based on route combinations. This allows the defense to maintain zone integrity while getting the tight coverage of man-to-man. Common pattern-match concepts include 'Palms' (a Cover 2 variant), 'Quarters' (a 4-deep adaptation), and 'Skate' (a Cover 3 adaptation). These coverages require defensive backs to recognize route concepts and understand how to pass off receivers between zones. Communication is critical as defenders must make quick decisions about coverage responsibilities based on receivers' routes after the snap.",
            imageResId = R.drawable.offense_playbook_bg,
            durationMinutes = 25,
            isCompleted = false,
            index = 2
        ),
        "tackling" to Lesson(
            id = "tackling",
            chapterId = "secondary-play",
            title = "Tackling in Space",
            description = "Techniques for open-field tackling",
            contentType = ContentType.TEXT,
            contentData = "Secondary defenders often must make tackles in open space. Proper tackling form involves breaking down (gathering feet under shoulders), bending knees, keeping head up, and driving through the ball carrier's midsection. The 'hawk' tackling technique (wrapping arms around the runner while keeping head to the side) increases safety and effectiveness. Angle tackles require understanding leverage and pursuit paths to intercept ball carriers. When tackling larger runners, aiming low is crucial. Against elusive runners, containing and breaking down rather than lunging for big hits improves consistency. Secondary players should always tackle through the ball to prevent fumbles from being recovered by the offense.",
            imageResId = null,
            durationMinutes = 20,
            isCompleted = false,
            index = 3
        )
    )

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
        ),
        "west-coast-offense-quiz" to Quiz(
            id = "west-coast-offense-quiz",
            question = "What is a key characteristic of the West Coast Offense?",
            options = listOf("Primarily deep vertical passing", "Power running with minimal passing", "Short, timing-based horizontal passing", "Option-based running attack"),
            correctOptionIndex = 2,
            explanation = "The West Coast Offense uses a short, horizontal passing game with precise timing as an extension of the running game. Quarterbacks make quick reads and releases based on timing rather than waiting to see receivers get open."
        ),
        "spread-offense-quiz" to Quiz(
            id = "spread-offense-quiz",
            question = "What is the purpose of Run-Pass Option (RPO) plays?",
            options = listOf("To confuse the offensive line", "To give the quarterback two different plays to choose from after the snap based on defensive reaction", "To always run the ball", "To maximize deep passing opportunities"),
            correctOptionIndex = 1,
            explanation = "RPOs give the quarterback the option to either hand off or throw based on the reaction of a specific defender after the snap, putting that defender in a no-win situation."
        ),
        "red-zone-offense-quiz" to Quiz(
            id = "red-zone-offense-quiz",
            question = "Why do passing concepts need to be adjusted in the red zone?",
            options = listOf("Because receivers are faster in the red zone", "Because the compressed vertical space eliminates deep routes and creates tighter windows", "Because quarterbacks throw harder in the red zone", "Because offensive linemen block differently in the red zone"),
            correctOptionIndex = 1,
            explanation = "The red zone's compressed vertical space (due to the back of the end zone) eliminates deep threats, creates tighter throwing windows, and allows defenders to be more aggressive since they have less ground to cover."
        ),
        "mechanics-quiz" to Quiz(
            id = "mechanics-quiz",
            question = "What is the proper sequence for generating power in the throwing motion?",
            options = listOf("Arm, shoulder, hips, legs", "Legs, hips, shoulder, arm", "Shoulder, arm, hips, legs", "Arms and shoulders simultaneously"),
            correctOptionIndex = 1,
            explanation = "Proper throwing mechanics generate power from the ground up - starting with the lower body (legs), transferring through hip rotation, then shoulder rotation, and finally the arm. This sequential transfer of energy creates maximum power and accuracy."
        ),
        "mental-game-quiz" to Quiz(
            id = "mental-game-quiz",
            question = "What might indicate man coverage pre-snap?",
            options = listOf("Two high safeties", "Defensive backs following a motioned receiver", "Cornerbacks with a large cushion", "Defensive linemen with their hands in the dirt"),
            correctOptionIndex = 1,
            explanation = "When defensive backs follow a receiver who motions across the formation, it strongly indicates man coverage, as zone defenders would typically stay in their assigned areas and pass the receiver to another defender."
        ),
        "leadership-quiz" to Quiz(
            id = "leadership-quiz",
            question = "What is an effective approach for a quarterback after a teammate makes a mistake?",
            options = listOf("Publicly criticize them to prevent future errors", "Ignore the mistake to avoid confrontation", "Provide immediate supportive communication while reinforcing correct execution", "Notify the coaches to handle the situation"),
            correctOptionIndex = 2,
            explanation = "Effective quarterback leadership involves providing immediate supportive communication after mistakes while also reinforcing the correct execution. This approach maintains teammate confidence while still addressing the error constructively."
        ),
        "advanced-concepts-quiz" to Quiz(
            id = "advanced-concepts-quiz",
            question = "What is 'eye manipulation' in quarterback play?",
            options = listOf("A technique to improve visual acuity", "Using eye movement to mislead defenders about the intended target", "A medical procedure for quarterbacks with vision problems", "A method for signaling receivers"),
            correctOptionIndex = 1,
            explanation = "Eye manipulation involves a quarterback using their eyes to influence defenders - looking off safeties or linebackers to hold them in place or move them away from the intended target, creating openings elsewhere on the field."
        ),
        "run-game-quiz" to Quiz(
            id = "run-game-quiz",
            question = "In the Inside Zone running play, what is the running back's primary read?",
            options = listOf("The defensive end", "The middle linebacker", "The flow of the defensive line", "The cornerback"),
            correctOptionIndex = 2,
            explanation = "In Inside Zone, the running back reads the flow of the defensive line to find cutback lanes, aiming initially for the outside hip of the guard."
        ),
        "pass-game-quiz" to Quiz(
            id = "pass-game-quiz",
            question = "What is the primary benefit of play-action passing?",
            options = listOf("It simplifies the quarterback's read", "It draws defenders toward the line of scrimmage, creating openings downfield", "It reduces the risk of interception", "It always guarantees a completed pass"),
            correctOptionIndex = 1,
            explanation = "Play-action passes use run fakes to draw defenders toward the line of scrimmage, creating openings for receivers downfield."
        ),
        "game-planning-quiz" to Quiz(
            id = "game-planning-quiz",
            question = "What is the purpose of scripting plays at the beginning of a game?",
            options = listOf("To confuse the defense", "To evaluate defensive responses to various offensive looks", "To give the quarterback easy completions", "To use all players equally"),
            correctOptionIndex = 1,
            explanation = "Scripting the first 15-20 plays allows coaches to evaluate how the defense responds to different formations, concepts, and plays, informing adjustments for later in the game."
        ),
        "defensive-line-quiz" to Quiz(
            id = "defensive-line-quiz",
            question = "What does a '3-technique' defensive lineman alignment mean?",
            options = listOf("The player is lined up directly over the center", "The player is lined up on the inside shoulder of the guard", "The player is lined up on the outside shoulder of the guard", "The player is lined up directly over the tackle"),
            correctOptionIndex = 2,
            explanation = "A 3-technique alignment positions the defensive lineman on the outside shoulder of the guard, typically responsible for the B-gap between guard and tackle."
        ),
        "linebacker-play-quiz" to Quiz(
            id = "linebacker-play-quiz",
            question = "What is typically the primary 'key' that inside linebackers read to diagnose plays?",
            options = listOf("The quarterback", "The guard", "The running back", "The wide receivers"),
            correctOptionIndex = 1,
            explanation = "Inside linebackers typically key the guard's initial movement - high hat (standing up) indicates pass, while low hat (drive blocking) indicates run."
        ),
        "secondary-play-quiz" to Quiz(
            id = "secondary-play-quiz",
            question = "In Cover 2 defense, how many deep zones are there and who covers them?",
            options = listOf("Two deep zones covered by cornerbacks", "Two deep zones covered by safeties", "Three deep zones covered by two corners and a safety", "Four deep zones covered by corners and safeties"),
            correctOptionIndex = 1,
            explanation = "Cover 2 features two deep safeties, each responsible for covering half of the deep field (half the field from sideline to middle)."
        )
    )

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
            quiz = quizzes["basic-rules-quiz"],
            quizCompleted = false,
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
            isLocked = true,
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
            isLocked = true,
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
            quiz = quizzes["advanced-tactics-quiz"],
            quizCompleted = false,
            index = 3
        ),

        // Offensive Playbook Course
        Chapter(
            id = "west-coast-offense",
            courseId = "offensive-playbook",
            title = "West Coast Offense",
            description = "Master timing-based passing systems",
            lessons = listOf(
                lessons["wco-philosophy"]!!,
                lessons["wco-route-concepts"]!!,
                lessons["wco-terminology"]!!,
                lessons["wco-progression"]!!
            ),
            isLocked = false,
            quiz = quizzes["west-coast-offense-quiz"],
            quizCompleted = false,
            index = 0
        ),
        Chapter(
            id = "spread-offense",
            courseId = "offensive-playbook",
            title = "Spread Offense Concepts",
            description = "Modern spread attack fundamentals",
            lessons = listOf(
                lessons["spread-philosophy"]!!,
                lessons["rpo-concepts"]!!,
                lessons["option-running"]!!,
                lessons["tempo"]!!
            ),
            isLocked = true,
            quiz = quizzes["spread-offense-quiz"],
            quizCompleted = false,
            index = 1
        ),
        Chapter(
            id = "red-zone-offense",
            courseId = "offensive-playbook",
            title = "Red Zone Strategy",
            description = "Scoring tactics inside the 20-yard line",
            lessons = listOf(
                lessons["red-zone-challenges"]!!,
                lessons["red-zone-run"]!!,
                lessons["red-zone-pass"]!!,
                lessons["situational-strategy"]!!
            ),
            isLocked = true,
            quiz = quizzes["red-zone-offense-quiz"],
            quizCompleted = false,
            index = 2
        ),

        // Quarterback Fundamentals Course
        Chapter(
            id = "mechanics",
            courseId = "quarterback-fundamentals",
            title = "Core Mechanics",
            description = "Essential physical techniques for quarterbacks",
            lessons = listOf(
                lessons["stance-grip"]!!,
                lessons["footwork"]!!,
                lessons["throwing-motion"]!!,
                lessons["ball-handling"]!!
            ),
            isLocked = false,
            quiz = quizzes["mechanics-quiz"],
            quizCompleted = false,
            index = 0
        ),
        Chapter(
            id = "mental-game",
            courseId = "quarterback-fundamentals",
            title = "Mental Game",
            description = "Reading defenses and making decisions",
            lessons = listOf(
                lessons["pre-snap-reads"]!!,
                lessons["progression-reading"]!!,
                lessons["protection-recognition"]!!,
                lessons["game-management"]!!
            ),
            isLocked = true,
            quiz = quizzes["mental-game-quiz"],
            quizCompleted = false,
            index = 1
        ),
        Chapter(
            id = "leadership",
            courseId = "quarterback-fundamentals",
            title = "Leadership Skills",
            description = "Guiding and inspiring your team",
            lessons = listOf(
                lessons["huddle-command"]!!,
                lessons["teammate-relationships"]!!,
                lessons["adversity-management"]!!,
                lessons["coach-relationship"]!!
            ),
            isLocked = true,
            quiz = quizzes["leadership-quiz"],
            quizCompleted = false,
            index = 2
        ),
        Chapter(
            id = "advanced-concepts",
            courseId = "quarterback-fundamentals",
            title = "Advanced Concepts",
            description = "Elite-level quarterback skills and techniques",
            lessons = listOf(
                lessons["creating-leverage"]!!,
                lessons["pocket-movement"]!!,
                lessons["field-manipulation"]!!,
                lessons["film-study"]!!
            ),
            isLocked = true,
            quiz = quizzes["advanced-concepts-quiz"],
            quizCompleted = false,
            index = 3
        ),

        // Offensive Strategies Course
        Chapter(
            id = "run-game",
            courseId = "offensive-strategies",
            title = "Running Game",
            description = "Master the fundamentals of effective running attacks",
            lessons = listOf(
                lessons["run-philosophy"]!!,
                lessons["inside-zone"]!!,
                lessons["outside-zone"]!!,
                lessons["gap-schemes"]!!
            ),
            isLocked = false,
            quiz = quizzes["run-game-quiz"],
            quizCompleted = false,
            index = 0
        ),
        Chapter(
            id = "pass-game",
            courseId = "offensive-strategies",
            title = "Passing Game",
            description = "Develop a dynamic and effective aerial attack",
            lessons = listOf(
                lessons["pass-concepts"]!!,
                lessons["progression-reads"]!!,
                lessons["play-action"]!!,
                lessons["protection-schemes"]!!
            ),
            isLocked = true,
            quiz = quizzes["pass-game-quiz"],
            quizCompleted = false,
            index = 1
        ),
        Chapter(
            id = "game-planning",
            courseId = "offensive-strategies",
            title = "Game Planning",
            description = "Strategies for preparing and adjusting offensive approach",
            lessons = listOf(
                lessons["opponent-analysis"]!!,
                lessons["game-script"]!!,
                lessons["in-game-adjustments"]!!
            ),
            isLocked = true,
            quiz = quizzes["game-planning-quiz"],
            quizCompleted = false,
            index = 2
        ),

        // Defensive Strategies Course
        Chapter(
            id = "defensive-line",
            courseId = "defensive-strategies",
            title = "Defensive Line Play",
            description = "Techniques and responsibilities for the front line",
            lessons = listOf(
                lessons["dl-stances"]!!,
                lessons["gap-techniques"]!!,
                lessons["pass-rush-moves"]!!,
                lessons["run-defense"]!!
            ),
            isLocked = false,
            quiz = quizzes["defensive-line-quiz"],
            quizCompleted = false,
            index = 0
        ),
        Chapter(
            id = "linebacker-play",
            courseId = "defensive-strategies",
            title = "Linebacker Fundamentals",
            description = "Core skills for the heart of the defense",
            lessons = listOf(
                lessons["lb-reads"]!!,
                lessons["gap-control"]!!,
                lessons["coverage-responsibilities"]!!,
                lessons["blitzing"]!!
            ),
            isLocked = true,
            quiz = quizzes["linebacker-play-quiz"],
            quizCompleted = false,
            index = 1
        ),
        Chapter(
            id = "secondary-play",
            courseId = "defensive-strategies",
            title = "Secondary Techniques",
            description = "Coverage and tackling skills for defensive backs",
            lessons = listOf(
                lessons["cornerback-techniques"]!!,
                lessons["safety-play"]!!,
                lessons["pattern-matching"]!!,
                lessons["tackling"]!!
            ),
            isLocked = true,
            quiz = quizzes["secondary-play-quiz"],
            quizCompleted = false,
            index = 2
        )
    )

    private val courses = listOf(
        Course(
            id = "football-basics",
            title = "Football Basics",
            description = "Master the fundamentals of American Football with simple, interactive lessons designed for beginners.",
            imageResId = R.drawable.football_field_bg,
            chapters = chapters.filter { it.courseId == "football-basics" }
        ),
        Course(
            id = "offensive-playbook",
            title = "Offensive Playbook",
            description = "Master offense strategies with advanced play diagrams and concepts.",
            imageResId = R.drawable.offense_playbook_bg,
            chapters = chapters.filter { it.courseId == "offensive-playbook" }
        ),
        Course(
            id = "quarterback-fundamentals",
            title = "Quarterback Fundamentals",
            description = "Learn essential quarterback techniques from footwork to reading defenses.",
            imageResId = R.drawable.quarterback_bg,
            chapters = chapters.filter { it.courseId == "quarterback-fundamentals" }
        ),
        Course(
            id = "offensive-strategies",
            title = "Offensive Strategies",
            description = "Learn the principles and tactics of modern offensive football systems.",
            imageResId = R.drawable.offense_playbook_bg,
            chapters = chapters.filter { it.courseId == "offensive-strategies" }
        ),
        Course(
            id = "defensive-strategies",
            title = "Defensive Strategies",
            description = "Master defensive techniques and strategies at all three levels of the defense.",
            imageResId = R.drawable.quarterback_bg,
            chapters = chapters.filter { it.courseId == "defensive-strategies" }
        ),
    )

    // Initialize the DataManager
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
            Log.d("DataManager", "Database already initialized, loading user progress")

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

                        Log.d("DataManager", "User progress loaded successfully from Firestore")
                        onComplete()
                    }
                } else {
                    // No saved progress found, use defaults
                    withContext(Dispatchers.Main) {
                        isProgressLoaded = true
                        onComplete()
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
            onComplete(User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Football Fan",
                email = firebaseUser.email ?: "",
                profileImage = firebaseUser.photoUrl?.toString(),
                title = "Beginner",
                streakDays = 0,
                lessonsCompleted = completedLessons.size,
                chaptersCompleted = completedChapters.size,
                coursesCompleted = completedCourses.size,
                quizScores = emptyList(),
                timeSpent = 0
            ))
        } else {
            // Fall back to default user
            onComplete(User(
                id = "user123",
                name = "John Thompson",
                email = "john@example.com",
                title = "Quarterback in Training",
                streakDays = 12,
                lessonsCompleted = completedLessons.size,
                chaptersCompleted = completedChapters.size,
                coursesCompleted = completedCourses.size,
                quizScores = listOf(85, 90, 92, 88),
                timeSpent = 24 * 60 // 24 hours in minutes
            ))
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

    fun markQuizCompleted(chapterId: String, score: Int) {
        completedQuizzes.add(chapterId)

        // Check if all lessons in chapter are completed as well and mark chapter as completed
        getChapterById(chapterId) { chapter ->
            if (chapter != null && chapter.lessons.all { completedLessons.contains(it.id) }) {
                markChapterComplete(chapterId)
            }
        }

        saveProgress()

        FirestoreManager.markQuizCompleted(chapterId, score)
    }

    fun markChapterComplete(chapterId: String) {
        completedChapters.add(chapterId)

        // Check if this completes a course
        getChapterById(chapterId) { chapter ->
            if (chapter != null) {
                val courseId = chapter.courseId

                getCourseById(courseId) { course ->
                    if (course != null && course.chapters.all { completedChapters.contains(it.id) }) {
                        completedCourses.add(courseId)
                        saveProgress()
                    }
                }
            }
        }

        saveProgress()
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

    fun getNextLessonInChapter(chapterId: String, onComplete: (Lesson?) -> Unit) {
        getLessonsForChapter(chapterId) { chapterLessons ->
            val nextLesson = chapterLessons.firstOrNull { !completedLessons.contains(it.id) }
            onComplete(nextLesson)
        }
    }

    fun getLatestAchievement(onComplete: (Achievement?) -> Unit) {
        onComplete(achievements.maxByOrNull { it.earnedDate })
    }

    fun isLastLessonInChapter(lessonId: String, onComplete: (Boolean) -> Unit) {
        getLessonById(lessonId) { lesson ->
            if (lesson == null) {
                Log.d("DataManager", "isLastLessonInChapter: lesson is null")
                onComplete(false)
                return@getLessonById
            }

            getLessonsForChapter(lesson.chapterId) { chapterLessons ->
                val lastLesson = chapterLessons.maxByOrNull { it.index }
                val isLast = lastLesson?.id == lessonId
                Log.d("DataManager", "isLastLessonInChapter: $isLast, lessonId=$lessonId, lastLesson=${lastLesson?.id}, chapterLessons size=${chapterLessons.size}")
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
                onComplete(null)
                return@getAllCourses
            }

            // First, try to find an unlocked chapter with incomplete lessons
            for (course in courses) {
                val unlockedChapter = course.chapters.find {
                    !it.isLocked && it.lessons.any { lesson -> !completedLessons.contains(lesson.id) }
                }

                if (unlockedChapter != null) {
                    onComplete(Pair(course, unlockedChapter))
                    return@getAllCourses
                }
            }

            // If no unlocked incomplete chapter found, return the first course's first chapter
            val firstCourse = courses.first()
            if (firstCourse.chapters.isNotEmpty()) {
                onComplete(Pair(firstCourse, firstCourse.chapters.first()))
                return@getAllCourses
            }

            onComplete(null) // No courses or chapters found
        }
    }

    fun shouldChapterBeLocked(chapterId: String, onComplete: (Boolean) -> Unit) {
        getAllCourses { courses ->
            val allChapters = courses.flatMap { it.chapters }
            val chapter = allChapters.find { it.id == chapterId } ?: run {
                onComplete(true)
                return@getAllCourses
            }

            // Get all chapters for this course
            val courseChapters = allChapters.filter { it.courseId == chapter.courseId }
                .sortedBy { it.index }

            // Find the chapter index
            val chapterIndex = courseChapters.indexOfFirst { it.id == chapterId }
            if (chapterIndex <= 0) {
                onComplete(false) // First chapter is never locked
                return@getAllCourses
            }

            // Check if all previous chapters are completed
            for (i in 0 until chapterIndex) {
                val previousChapter = courseChapters[i]
                if (!isChapterCompleted(previousChapter.id)) {
                    onComplete(true) // Lock if any previous chapter isn't completed
                    return@getAllCourses
                }
            }

            onComplete(false) // All previous chapters completed, so this one is unlocked
        }
    }

    fun isChapterCompleted(chapterId: String): Boolean {
        return completedChapters.contains(chapterId)
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

    fun addAchievement(achievement: Achievement) {
        achievements.add(achievement)
        // In a future implementation, we'd save this to Firestore
    }

    fun getAllPosts(onComplete: (List<Post>) -> Unit) {
        val sharedPrefs = SharedPreferencesManager.getInstance()
        val isDatabaseInitialized = sharedPrefs.getBoolean(SPKeys.DATABASE_INITIALIZED, false)

        if (!isDatabaseInitialized) {
            // Database not yet initialized, use static data
            onComplete(posts)
        } else {
            // Database initialized, fetch from Firestore
            FirestoreManager.getAllPosts(onComplete)
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

    fun togglePostFavorite(postId: String, isFavorite: Boolean, onComplete: () -> Unit = {}) {
        FirestoreManager.updatePost(postId, isFavorite, onComplete)
    }

    fun addNewPost(post: Post, onComplete: (Boolean) -> Unit = {}) {
        FirestoreManager.addNewPost(post, onComplete)
    }
}