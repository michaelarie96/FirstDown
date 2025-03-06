package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.example.firstdown.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already signed in
        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is already signed in, go directly to HomeActivity
            navigateToHome()
        } else {
            // User is not signed in, start authentication flow
            startSignIn()
        }
    }

    private fun startSignIn() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), // Email authentication
            AuthUI.IdpConfig.PhoneBuilder().build(), // Phone authentication
            AuthUI.IdpConfig.GoogleBuilder().build() // Google Sign-In
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.football_logo)
            .setTheme(R.style.Theme_FirstDown)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            // Sign in failed
            if (response == null) {
                // User cancelled the sign-in flow
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
                finish() // Return to welcome activity
            } else {
                // Handle error
                Toast.makeText(this, "Sign in failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun navigateToHome() {
        // Set result to OK so WelcomeActivity knows authentication succeeded
        setResult(RESULT_OK)

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Close this activity
    }
}