package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdown.databinding.ActivityWelcomeBinding
import com.example.firstdown.model.DataManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // User successfully logged in, we can finish WelcomeActivity
            finish()
        }
        // If result is not OK, keep WelcomeActivity alive so user can try again
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        Log.d("WelcomeActivity", "onCreate: currentUser = ${FirebaseAuth.getInstance().currentUser}")

        // Check if user is already signed in
        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is already signed in, load progress and go to HomeActivity
            DataManager.loadUserProgress {
                navigateToHome()
            }
            return  // Skip the rest of onCreate
        }

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        loginLauncher.launch(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()  // Close this activity
    }
}