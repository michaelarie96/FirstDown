package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.example.firstdown.databinding.ActivityLoginBinding
import com.example.firstdown.model.DataManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity" // for easier debugging

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for existing user *and* if the intent is for sign-in
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            navigateToHome()
        } else {
            //Only start the sign-in flow if the user is not currently signed in.
            startSignIn()
        }
    }

    private fun startSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.football_logo)
            .setTheme(R.style.Theme_FirstDown)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()

            // Load user progress before navigating
            DataManager.loadUserProgress {
                navigateToHome()
            }
        } else {
            val response = result.idpResponse
            if (response == null) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.w(TAG, "Sign in error", response.error) // Log the error for debugging
                Toast.makeText(this, "Sign in failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToHome() {
        setResult(RESULT_OK)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
