package com.example.firstdown.fragments

import android.os.Bundle
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

        viewModel.getAllCourses { courses ->
            courseAdapter.updateCourses(courses)
        }
    }

    private fun setupRecyclerView() {
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())

        // Create and set the adapter
        courseAdapter = CourseAdapter(emptyList(), this)
        binding.rvCourses.adapter = courseAdapter
    }

    // Handle course click events
    override fun onCourseClick(course: Course) {
        // Navigate to ChaptersFragment using SafeArgs
        val action = CoursesFragmentDirections.actionNavigationCoursesToChaptersFragment(course.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}