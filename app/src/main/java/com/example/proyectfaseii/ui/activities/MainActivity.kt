package com.example.proyectfaseii.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.example.proyectfaseii.R
import com.example.proyectfaseii.ui.adapters.MainPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import com.example.proyectfaseii.utils.ReminderScheduler
import com.example.proyectfaseii.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : FragmentActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var titleTab: TextView
    private lateinit var btnMenu: ImageView
    private lateinit var btnSettings: ImageView

    private val tabTitles = listOf("Today", "Habits", "Leaderboard", "Profile")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val darkMode = SharedPrefManager.getInstance(this).isDarkModeEnabled()
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        ReminderScheduler.scheduleDailyReminder(this) // ← ⏰ esto inicia todo
        setContentView(R.layout.activity_main)
        setupUI()
    }

    private fun setupUI() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNavigation)
        titleTab = findViewById(R.id.titleTab)
        btnMenu = findViewById(R.id.btnMenu)
        btnSettings = findViewById(R.id.btnSettings)

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu[position].isChecked = true
                titleTab.text = tabTitles[position]
            }
        })

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_today -> viewPager.currentItem = 0
                R.id.nav_habits -> viewPager.currentItem = 1
                R.id.nav_leaderboard -> viewPager.currentItem = 2
                R.id.nav_profile -> viewPager.currentItem = 3
            }
            true
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnMenu.setOnClickListener {
            drawerLayout.open()
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_suggestions -> {
                    startActivity(Intent(this, SuggestionsActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_logout -> {
                    SharedPrefManager.getInstance(this@MainActivity).clear()
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}
