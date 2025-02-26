package com.example.firstdown

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSubmit: Button

    // Sample quiz data - in a real app, this would come from a database or API
    private val currentQuestionIndex = 1
    private val totalQuestions = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Get quiz details from intent
        val lessonTitle = intent.getStringExtra("LESSON_TITLE") ?: "Lesson"

        // Set up back button
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // Set up question counter
        val tvQuestionCounter: TextView = findViewById(R.id.tv_question_counter)
        tvQuestionCounter.text = "Question $currentQuestionIndex/$totalQuestions"

        // Set up progress bar
        val progressBar: androidx.core.widget.ContentLoadingProgressBar = findViewById(R.id.progress_bar)
        progressBar.progress = (currentQuestionIndex * 100) / totalQuestions

        // Set up radio group for answers
        radioGroup = findViewById(R.id.radio_group)

        // Set up submit button
        btnSubmit = findViewById(R.id.btn_submit_answer)
        btnSubmit.setOnClickListener {
            // Check answer and navigate to next question
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                // In a real app, you would check if the answer is correct and handle accordingly
                // For now, just finish the activity
                finish()
            }
        }
    }
}