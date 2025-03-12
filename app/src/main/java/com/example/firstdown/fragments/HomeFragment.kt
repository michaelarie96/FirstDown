package com.example.firstdown.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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

        binding.loadingSpinner.visibility = View.VISIBLE

        isFirstTimeUser = isNewUserStatic

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        viewModel.getCurrentUser { currentUser ->
            setupUserInfo(currentUser)
            setupTodayGoal()
            setupContinueLearning()
            setupQuickTip()

            binding.loadingSpinner.visibility = View.GONE
        }
    }

    private fun setupUserInfo(currentUser: User) {
        binding.tvGreeting.text = getString(R.string.greeting, currentUser.name)
        binding.tvStreak.text = getString(R.string.day_streak, currentUser.streakDays)
    }

    private fun setupTodayGoal() {
        viewModel.getNextLessonToComplete { nextLesson ->
            if (nextLesson != null) {
                binding.tvGoalDescription.text = getString(R.string.complete_lesson_goal, nextLesson.title)

                viewModel.wasLessonCompletedToday(nextLesson.id) { isCompleted ->
                    binding.cbGoalCompleted.isChecked = isCompleted
                    binding.cbGoalCompleted.isEnabled = false
                }
            } else {
                // No more lessons to complete
                binding.tvGoalDescription.text = getString(R.string.all_lessons_completed)
                binding.cbGoalCompleted.isChecked = true
                binding.cbGoalCompleted.isEnabled = false
            }
        }
    }

    private fun setupContinueLearning() {
        viewModel.getCurrentOrNextChapter { courseChapterPair ->
            if (courseChapterPair != null) {
                val (course, chapter) = courseChapterPair

                viewModel.hasStartedAnyLearning { hasStarted ->
                    if (isFirstTimeUser || !hasStarted) {
                        binding.tvLearningStatus.text = getString(R.string.start_learning)
                    } else {
                        binding.tvLearningStatus.text = getString(R.string.continue_learning)
                    }
                }

                binding.tvLessonTitle.text = course.title
                binding.tvLessonDescription.text = chapter.title

                viewModel.hasStartedChapter(chapter.id) { hasStarted ->
                    if (hasStarted) {
                        binding.btnStart.text = getString(R.string.continue_learning_button)
                    } else {
                        binding.btnStart.text = getString(R.string.start_learning)
                    }
                }
            } else {
                // Handle the case where no courses/chapters are available (Shouldn't happen)
                binding.cardNextLesson.visibility = View.GONE
            }
        }
    }

    private fun setupQuickTip() {
        viewModel.getRandomQuickTip { quickTip ->
            val tipCard = binding.layoutTips.getChildAt(0)
            val tvTipContent = tipCard.findViewById<TextView>(R.id.tv_tip_content)

            tvTipContent.text = quickTip
        }
    }

    private fun setupListeners() {
        // Navigation listeners
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

        // Button listener for continuing/starting learning
        binding.btnStart.setOnClickListener {
            Log.d("HomeFragment", "Start button clicked")
            viewModel.getCurrentOrNextChapter { courseChapterPair ->
                if (courseChapterPair != null) {
                    val (_, chapter) = courseChapterPair

                    Log.d("HomeFragment", "Chapter: ${chapter.id}, Lessons count: ${chapter.lessons.size}")

                    // Find the first unfinished lesson in this chapter
                    val lesson = chapter.lessons.find { !it.isCompleted }

                    if (lesson != null) {
                        Log.d("HomeFragment", "Found lesson: ${lesson.id}, navigating...")
                        navigateToLesson(lesson.id, lesson.title)
                    } else {
                        Log.d("HomeFragment", "No unfinished lesson found!")
                        // If no unfinished lesson, just navigate to the first lesson
                        if (chapter.lessons.isNotEmpty()) {
                            val firstLesson = chapter.lessons.first()
                            Log.d("HomeFragment", "Using first lesson instead: ${firstLesson.id}")
                            navigateToLesson(firstLesson.id, firstLesson.title)
                        }
                    }
                } else {
                    Log.d("HomeFragment", "No chapter found!")
                }
            }
        }
    }

    private fun navigateToLesson(lessonId: String, lessonTitle: String) {
        try {
            Log.d("HomeFragment", "Navigating to lesson: $lessonId, title: $lessonTitle")
            // Use Navigation Component to navigate to the LessonFragment
            val action = HomeFragmentDirections.actionNavigationHomeToLessonFragment(
                lessonId = lessonId
            )
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Navigation error: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}