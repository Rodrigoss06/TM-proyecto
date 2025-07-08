package com.example.proyectfaseii.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import com.example.proyectfaseii.R
import com.example.proyectfaseii.utils.ReminderScheduler
import com.example.proyectfaseii.utils.SharedPrefManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val context = requireContext()
        val sharedPrefs = preferenceManager.sharedPreferences
        val manager = SharedPrefManager.getInstance(context)

        // 🔁 Observadores de preferencias
        sharedPrefs?.registerOnSharedPreferenceChangeListener { prefs, key ->
            when (key) {

                "dark_mode" -> {
                    val isDark = prefs.getBoolean(key, false)
                    manager.setDarkModeEnabled(isDark)
                    AppCompatDelegate.setDefaultNightMode(
                        if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                }

                "notificaciones" -> {
                    val enabled = prefs.getBoolean(key, true)
                    manager.setNotificationsEnabled(enabled)
                    if (enabled) ReminderScheduler.scheduleDailyReminder(context)
                    else ReminderScheduler.cancelReminder(context)
                }

                "username" -> {
                    val newName = prefs.getString(key, "Usuario") ?: "Usuario"
                    manager.saveUserName(newName)
                    Toast.makeText(context, "Nombre actualizado: $newName", Toast.LENGTH_SHORT).show()
                }

                "habit_notifications" -> {
                    val enabled = prefs.getBoolean(key, true)
                    Toast.makeText(
                        context,
                        if (enabled) "Notificaciones de hábitos activadas" else "Notificaciones desactivadas",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                "suggestion_frequency" -> {
                    val freq = prefs.getString(key, "daily")
                    Toast.makeText(context, "Frecuencia sugerencias: $freq", Toast.LENGTH_SHORT).show()
                }

                "language" -> {
                    val lang = prefs.getString(key, "es")
                    Toast.makeText(context, "Idioma seleccionado: $lang", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
