package com.example.firstdown.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.R
import com.example.firstdown.model.Course
import com.example.firstdown.model.DataManager

class CourseAdapter(
    private var courses: List<Course>,
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

    fun updateCourses(newCourses: List<Course>) {
        val oldList = ArrayList(courses)
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = newCourses.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newCourses[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newCourses[newItemPosition]
            }
        })

        courses = newCourses
        diffResult.dispatchUpdatesTo(this)
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCourseImage: ImageView = itemView.findViewById(R.id.iv_course_image)
        private val tvCourseTitle: TextView = itemView.findViewById(R.id.tv_course_title)
        private val tvCourseDescription: TextView = itemView.findViewById(R.id.tv_course_description)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val btnViewCourse: Button = itemView.findViewById(R.id.btn_view_course)

        fun bind(course: Course, listener: OnCourseClickListener) {
            // Set course image
            ivCourseImage.setImageResource(course.imageResId)

            // Set course title and description
            tvCourseTitle.text = course.title
            tvCourseDescription.text = course.description

            DataManager.shouldCourseBeLocked(course.id) { isLocked ->
                if (isLocked) {
                    tvProgress.text = "Locked"
                    btnViewCourse.text = "Locked"
                    // Add lock icon to button
                    btnViewCourse.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0)
                    btnViewCourse.isEnabled = false
                    // Make progress bar gray
                    progressBar.progress = 0
                } else {
                    // Course is unlocked
                    progressBar.progress = course.progress
                    tvProgress.text = "${course.progress}% Complete"

                    // Set button text based on progress
                    if (course.progress > 0) {
                        btnViewCourse.text = "Continue"
                    } else {
                        btnViewCourse.text = "Start"
                    }

                    btnViewCourse.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    btnViewCourse.isEnabled = true
                }

                // Set click listeners
                itemView.setOnClickListener {
                    if (!isLocked) {
                        listener.onCourseClick(course)
                    }
                }

                btnViewCourse.setOnClickListener {
                    if (!isLocked) {
                        listener.onCourseClick(course)
                    }
                }
            }
        }
    }
}
