package com.example.firstdown.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstdown.adapters.CourseAdapter
import com.example.firstdown.databinding.FragmentCoursesBinding
import com.example.firstdown.model.Course
import com.example.firstdown.viewmodel.CoursesViewModel
import androidx.navigation.fragment.findNavController

class CoursesFragment : Fragment(), CourseAdapter.OnCourseClickListener {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CoursesViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        setupRecyclerView()

        binding.progressBarCourses.visibility = View.VISIBLE
        binding.rvCourses.visibility = View.GONE

        viewModel.getAllCourses { courses ->
            courses.forEach { course ->
                val resourceName = try {
                    resources.getResourceName(course.imageResId)
                } catch (e: Exception) {
                    "INVALID RESOURCE ID"
                }
                Log.d("CoursesFragment", "Course: ${course.id}, ImageResId: ${course.imageResId}, Resource name: $resourceName")
            }
            courseAdapter.updateCourses(courses)

            binding.progressBarCourses.visibility = View.GONE
            binding.rvCourses.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())

        binding.rvCourses.setHasFixedSize(true)
        binding.rvCourses.setItemViewCacheSize(10)

        courseAdapter = CourseAdapter(emptyList(), this)
        binding.rvCourses.adapter = courseAdapter
    }

    override fun onCourseClick(course: Course) {
        val action = CoursesFragmentDirections.actionNavigationCoursesToChaptersFragment(course.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}