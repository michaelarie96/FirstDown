package com.example.firstdown.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstdown.R
import com.example.firstdown.model.Chapter

class ChapterAdapter(
    private val chapters: List<Chapter>,
    private val listener: ChapterClickListener
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    interface ChapterClickListener {
        fun onChapterClicked(chapter: Chapter)
        fun onChapterQuizClicked(chapter: Chapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.bind(chapter, listener)
    }

    override fun getItemCount(): Int = chapters.size

    class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvChapterTitle: TextView = itemView.findViewById(R.id.tv_chapter_title)
        private val tvChapterProgress: TextView = itemView.findViewById(R.id.tv_chapter_progress)
        private val llProgressIndicators: LinearLayout = itemView.findViewById(R.id.ll_progress_indicators)
        private val btnAction: Button = itemView.findViewById(R.id.btn_chapter_action)

        fun bind(chapter: Chapter, listener: ChapterClickListener) {
            tvChapterTitle.text = chapter.title

            if (chapter.isLocked) {
                tvChapterProgress.text = "Locked"
                btnAction.text = "Complete Previous Chapters"
                btnAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0)
                btnAction.isEnabled = false
            } else {
                tvChapterProgress.text = "${chapter.progress}% Complete"

                // Set appropriate button text based on progress and quiz status
                when {
                    chapter.progress == 0 -> {
                        // No lessons completed yet
                        btnAction.text = "Start Chapter"
                    }
                    chapter.progress == 100 -> {
                        // All lessons completed
                        if (chapter.quizCompleted) {
                            btnAction.text = "Review Chapter"
                        } else {
                            btnAction.text = "Start Quiz"
                        }
                    }
                    else -> {
                        // Some lessons completed, but not all
                        btnAction.text = "Continue Chapter"
                    }
                }

                btnAction.isEnabled = true
            }

            // Set click listener
            btnAction.setOnClickListener {
                if (!chapter.isLocked) {
                    listener.onChapterClicked(chapter)
                }
            }

            // Set click listener for the entire card
            itemView.setOnClickListener {
                if (!chapter.isLocked) {
                    listener.onChapterClicked(chapter)
                }
            }
        }
    }}