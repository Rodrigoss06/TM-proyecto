package com.example.proyectfaseii.data.models

data class UserProfile(
    val name: String = "",
    val totalHabits: Int = 0,
    val habitsCompleted: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)
