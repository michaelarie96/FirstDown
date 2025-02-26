package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var btnGetStarted: Button
    private lateinit var btnSignIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        btnGetStarted = findViewById(R.id.btn_get_started)
        btnSignIn = findViewById(R.id.btn_sign_in)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnGetStarted.setOnClickListener {
            // Navigate to MainActivity (Home screen)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("IS_NEW_USER", true)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            // For now, just navigate to MainActivity without the new user flag
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("IS_NEW_USER", false)
            startActivity(intent)
        }
    }
}