package com.example.proyectfaseii.data.models

data class UserRanking(
    val name: String = "",
    val completionRate: Double = 0.0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHabits: Int = 0,
    val habitsCompleted: Int = 0
)