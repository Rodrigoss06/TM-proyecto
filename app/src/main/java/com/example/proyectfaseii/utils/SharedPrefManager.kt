package com.example.proyectfaseii.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private var instance: SharedPrefManager? = null

        fun getInstance(context: Context): SharedPrefManager {
            if (instance == null) {
                instance = SharedPrefManager(context.applicationContext)
            }
            return instance!!
        }
    }

    fun saveUser(id: String, nombre: String, email: String, descripcion: String = "") {
        sharedPreferences.edit()
            .putString("user_id", id)
            .putString("username", nombre)
            .putString("email", email)
            .putString("descripcion", descripcion)
            .apply()
    }

    fun getUserId(): String? = sharedPreferences.getString("user_id", null)
    fun getUserName(): String? = sharedPreferences.getString("username", null)
    fun getUserEmail(): String? = sharedPreferences.getString("email", null)
    fun getUserDescription(): String? = sharedPreferences.getString("descripcion", null)

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("username", name).apply()
    }

    fun isDarkModeEnabled(): Boolean = sharedPreferences.getBoolean("dark_mode", false)
    fun getSuggestionFrequency(): String = sharedPreferences.getString("suggestion_frequency", "daily") ?: "daily"
    fun areNotificationsEnabled(): Boolean = sharedPreferences.getBoolean("habit_notifications", true)


    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("habit_notifications", enabled).apply()
    }
}
