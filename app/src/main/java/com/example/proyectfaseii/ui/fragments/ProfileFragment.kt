package com.example.proyectfaseii.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.example.proyectfaseii.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvTotalHabits: TextView
    private lateinit var tvHabitsCompleted: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvLongestStreak: TextView
    private lateinit var switchTheme: Switch
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Enlazar vistas
        tvUsername = view.findViewById(R.id.tv_username)
        tvTotalHabits = view.findViewById(R.id.tv_total_habits)
        tvHabitsCompleted = view.findViewById(R.id.tv_habits_completed)
        tvCurrentStreak = view.findViewById(R.id.tv_current_streak)
        tvLongestStreak = view.findViewById(R.id.tv_longest_streak)
        switchTheme = view.findViewById(R.id.switch_theme)
        btnLogout = view.findViewById(R.id.btn_logout)

        // Inicializar lógica
        loadUserData()
        setupThemeSwitch()
        setupLogout()
    }

    private fun loadUserData() {
        FirestoreManager.getCurrentUserProfile { user ->
            tvUsername.text = "Hola, ${user.name}"
            tvTotalHabits.text = "Total hábitos: ${user.totalHabits}"
            tvHabitsCompleted.text = "Completados: ${user.habitsCompleted}"
            tvCurrentStreak.text = "Racha actual: ${user.currentStreak} 🔥"
            tvLongestStreak.text = "Mayor racha: ${user.longestStreak} 🏆"
        }
    }

    private fun setupThemeSwitch() {
        val shared = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isDark = shared.getBoolean("dark_mode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        switchTheme.isChecked = isDark

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            shared.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupLogout() {
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
