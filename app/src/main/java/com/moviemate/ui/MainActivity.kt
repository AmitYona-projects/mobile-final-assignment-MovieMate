package com.moviemate.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.moviemate.R
import com.moviemate.databinding.ActivityMainBinding
import com.moviemate.ui.viewmodel.AuthViewModel
import com.moviemate.ui.viewmodel.UserViewModel
import com.moviemate.utils.CircleTransform
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        setupDrawer()
        setupNavigation()
        observeUser()
    }

    private fun setupDrawer() {
        val headerView = binding.navigationView.getHeaderView(0)
        val closeButton = headerView.findViewById<ImageView>(R.id.navCloseButton)
        closeButton.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    if (navController.currentDestination?.id != R.id.homeFeedFragment) {
                        navController.navigate(R.id.action_global_homeFeed)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.action_global_profile)
                    }
                    true
                }
                R.id.nav_logout -> {
                    authViewModel.logout()
                    // Recreate the Activity to clear all ViewModels and reset state
                    recreate()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigation() {
        val authScreens = setOf(R.id.welcomeFragment, R.id.loginFragment, R.id.registerFragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in authScreens) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private fun observeUser() {
        userViewModel.currentUser.observe(this) { user ->
            val headerView = binding.navigationView.getHeaderView(0)
            val navUsername = headerView.findViewById<TextView>(R.id.navUsername)
            val navEmail = headerView.findViewById<TextView>(R.id.navEmail)
            val navProfileImage = headerView.findViewById<ImageView>(R.id.navProfileImage)

            navUsername.text = user?.username ?: ""
            navEmail.text = user?.email ?: ""

            if (!user?.profileImageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(user?.profileImageUrl)
                    .placeholder(R.drawable.circle_background)
                    .error(R.drawable.circle_background)
                    .transform(CircleTransform())
                    .into(navProfileImage)
            }
        }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
