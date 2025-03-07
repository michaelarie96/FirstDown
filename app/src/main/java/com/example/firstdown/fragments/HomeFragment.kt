package com.example.firstdown.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.firstdown.R
import com.example.firstdown.databinding.FragmentHomeBinding
import com.example.firstdown.viewmodel.MainViewModel
import androidx.navigation.fragment.findNavController
import com.example.firstdown.model.User

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private var isFirstTimeUser = false

    companion object {
        var isNewUserStatic: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isFirstTimeUser = isNewUserStatic

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Get data from ViewModel
        val currentUser = viewModel.getCurrentUser()
        setupUserInfo(currentUser)
        setupTodayGoal()
        setupContinueLearning()
        setupLatestAchievement()
        setupQuickTip()
    }

    private fun setupUserInfo(currentUser: User) {
        binding.tvGreeting.text = getString(R.string.greeting, currentUser.name)
        binding.tvStreak.text = getString(R.string.day_streak, currentUser.streakDays)
    }

    private fun setupTodayGoal() {
        // Get the next lesson to complete
        val nextLesson = viewModel.getNextLessonToComplete()

        if (nextLesson != null) {
            binding.tvGoalDescription.text = getString(R.string.complete_lesson_goal, nextLesson.title)

            // Check if lesson was completed today
            val isCompleted = viewModel.wasLessonCompletedToday(nextLesson.id)
            binding.cbGoalCompleted.isChecked = isCompleted
            binding.cbGoalCompleted.isEnabled = false // Disable user interaction

        } else {
            // No more lessons to complete
            binding.tvGoalDescription.text = getString(R.string.all_lessons_completed)
            binding.cbGoalCompleted.isChecked = true
            binding.cbGoalCompleted.isEnabled = false
        }
    }

    private fun setupContinueLearning() {
        val courseChapterPair = viewModel.getCurrentOrNextChapter()

        if (courseChapterPair != null) {
            val (course, chapter) = courseChapterPair

            // Set the title based on user's learning status
            if (isFirstTimeUser || !viewModel.hasStartedAnyLearning()) {
                binding.tvLearningStatus.text = getString(R.string.start_learning)
            } else {
                binding.tvLearningStatus.text = getString(R.string.continue_learning)
            }

            // Set course and chapter info
            binding.tvLessonTitle.text = course.title
            binding.tvLessonDescription.text = chapter.title

            // Set the button text based on chapter status
            if (viewModel.hasStartedChapter(chapter.id)) {
                binding.btnStart.text = getString(R.string.continue_learning_button)
            } else {
                binding.btnStart.text = getString(R.string.start_learning)
            }
        } else {
            // Handle the case where no courses/chapters are available
            binding.cardNextLesson.visibility = View.GONE
        }
    }

    private fun setupLatestAchievement() {
        val latestAchievement = viewModel.getLatestAchievement()

        if (latestAchievement != null) {
            // Find the achievement card in the layout
            val achievementCard = binding.layoutAchievements.getChildAt(0)

            // Update achievement details
            val tvAchievementTitle = achievementCard.findViewById<TextView>(R.id.tv_achievement_title)
            val tvAchievementDesc = achievementCard.findViewById<TextView>(R.id.tv_achievement_description)
            val tvAchievementDate = achievementCard.findViewById<TextView>(R.id.tv_achievement_date)

            tvAchievementTitle.text = latestAchievement.title
            tvAchievementDesc.text = latestAchievement.description

            // Format the date (for simplicity showing "X days ago")
            val daysAgo = (System.currentTimeMillis() - latestAchievement.earnedDate) / (1000 * 60 * 60 * 24)
            tvAchievementDate.text = getString(R.string.earned_days_ago, daysAgo.toInt())
        }
    }

    private fun setupQuickTip() {
        // Get a random tip
        val quickTip = viewModel.getRandomQuickTip()

        // Find the quick tip card in the layout
        val tipCard = binding.layoutAchievements.getChildAt(1)
        val tvTipContent = tipCard.findViewById<TextView>(R.id.tv_tip_content)

        tvTipContent.text = quickTip
    }

    private fun setupListeners() {
        // Navigation listeners
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

        // Button listener for continuing/starting learning
        binding.btnStart.setOnClickListener {
            val courseChapterPair = viewModel.getCurrentOrNextChapter()

            if (courseChapterPair != null) {
                val (_, chapter) = courseChapterPair

                // Find the first unfinished lesson in this chapter
                val lesson = chapter.lessons.find { !it.isCompleted }

                if (lesson != null) {
                    navigateToLesson(lesson.id, lesson.title)
                }
            }
        }
    }

    private fun navigateToLesson(lessonId: String, lessonTitle: String) {
        // Get the correct page based on progress
        val currentPage = if (viewModel.hasStartedLesson(lessonId)) {
            viewModel.getLessonProgress(lessonId)
        } else {
            1 // Start from the first page
        }

        // Use Navigation Component to navigate to the LessonContentFragment
        val totalPages = viewModel.getLessonContentSize(lessonId)
        val action = HomeFragmentDirections.actionNavigationHomeToLessonContentFragment(
            lessonId = lessonId,
            lessonTitle = lessonTitle,
            currentPage = currentPage,
            totalPages = totalPages
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}