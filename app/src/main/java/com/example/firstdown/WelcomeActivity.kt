package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdown.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("IS_NEW_USER", true)
            startActivity(intent)
            finish()
        }

        binding.btnSignIn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("IS_NEW_USER", false)
            startActivity(intent)
            finish()
        }
    }
}