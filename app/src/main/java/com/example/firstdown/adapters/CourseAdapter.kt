package com.example.firstdown.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.R
import com.example.firstdown.model.Course

class CourseAdapter(
    private val courses: List<Course>,
    private val listener: OnCourseClickListener
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    interface OnCourseClickListener {
        fun onCourseClick(course: Course)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.bind(course, listener)
    }

    override fun getItemCount(): Int = courses.size

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCourseImage: ImageView = itemView.findViewById(R.id.iv_course_image)
        private val tvCourseTitle: TextView = itemView.findViewById(R.id.tv_course_title)
        private val tvCourseDescription: TextView = itemView.findViewById(R.id.tv_course_description)
        private val tvCourseDifficulty: TextView = itemView.findViewById(R.id.tv_course_difficulty)
        private val tvCourseLength: TextView = itemView.findViewById(R.id.tv_course_length)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val btnViewCourse: Button = itemView.findViewById(R.id.btn_view_course)

        fun bind(course: Course, listener: OnCourseClickListener) {
            // Set course image
            ivCourseImage.setImageResource(course.imageResId)

            // Set course title and description
            tvCourseTitle.text = course.title
            tvCourseDescription.text = course.description

            // Set course difficulty and length
            tvCourseDifficulty.text = course.difficulty
            tvCourseLength.text = "${course.estimatedHours} hours"

            // Set progress
            progressBar.progress = course.progress
            tvProgress.text = "${course.progress}% Complete"

            // Set button text based on progress
            if (course.progress > 0) {
                btnViewCourse.text = "Continue"
            } else {
                btnViewCourse.text = "Start"
            }

            // Set click listener for the entire card
            itemView.setOnClickListener {
                listener.onCourseClick(course)
            }

            // Set click listener for the button
            btnViewCourse.setOnClickListener {
                listener.onCourseClick(course)
            }
        }
    }
}