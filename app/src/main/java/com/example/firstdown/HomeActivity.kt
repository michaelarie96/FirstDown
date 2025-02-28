package com.example.firstdown

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.firstdown.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isNewUser = intent.getBooleanExtra("IS_NEW_USER", false)

        val navController = findNavController(R.id.nav_host_fragment)

        val bundle = Bundle()
        bundle.putBoolean("IS_NEW_USER", isNewUser)
        navController.setGraph(R.navigation.mobile_navigation, bundle)

        val navView: BottomNavigationView = binding.bottomNavigation

        navView.setupWithNavController(navController)
    }
}