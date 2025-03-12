package com.example.firstdown

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.firstdown.databinding.ActivityHomeBinding
import com.example.firstdown.fragments.HomeFragment
import com.example.firstdown.model.DataManager
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Store isNewUser flag in static variable
        val isNewUser = intent.getBooleanExtra("IS_NEW_USER", false)
        HomeFragment.isNewUserStatic = isNewUser

        // Load user progress first
        DataManager.loadUserProgress {
            // Find the NavHostFragment and get its controller
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            if (navHostFragment != null) {
                navController = navHostFragment.navController

                // Set up bottom navigation with the nav controller
                val navView: BottomNavigationView = binding.bottomNavigation
                navView.setupWithNavController(navController)

                // Handle reselection for courses tab
                binding.bottomNavigation.setOnItemReselectedListener { item ->
                    if (item.itemId == R.id.navigation_courses) {
                        val currentDestinationId = navController.currentDestination?.id
                        if (currentDestinationId != null &&
                            currentDestinationId != R.id.navigation_courses) {
                            navController.popBackStack(R.id.navigation_courses, false)
                        }
                    }
                }

                // Add a destination change listener to update the selected tab
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    val menuItem = when (destination.id) {
                        R.id.navigation_home -> R.id.navigation_home
                        R.id.navigation_courses, R.id.ChaptersFragment, R.id.lessonFragment, R.id.quizFragment ->
                            R.id.navigation_courses
                        R.id.navigation_community -> R.id.navigation_community
                        R.id.navigation_profile -> R.id.navigation_profile
                        else -> null
                    }

                    if (menuItem != null) {
                        binding.bottomNavigation.menu.findItem(menuItem)?.isChecked = true
                    }
                }
            } else {
                // Handle the case where nav host fragment is not found, helps with debugging
                throw IllegalStateException("NavHostFragment not found in activity_home.xml")
            }
        }

        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                // User has been signed out, navigate back to welcome screen
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Add auth listener when activity starts
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        // Remove auth listener when activity stops
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}