package com.example.firstdown

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.firstdown.databinding.ActivityHomeBinding
import com.example.firstdown.fragments.HomeFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Store isNewUser flag in static variable
        val isNewUser = intent.getBooleanExtra("IS_NEW_USER", false)
        HomeFragment.isNewUserStatic = isNewUser

        // Find the NavHostFragment and get its controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        if (navHostFragment != null) {
            navController = navHostFragment.navController

            // Set up bottom navigation with the nav controller
            val navView: BottomNavigationView = binding.bottomNavigation
            navView.setupWithNavController(navController)
        } else {
            // Handle the case where nav host fragment is not found, helps with debugging
            throw IllegalStateException("NavHostFragment not found in activity_home.xml")
        }
    }
}