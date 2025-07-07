package com.example.proyectfaseii.data.models

data class GoalHistoryItem(
    val date: String = "",         // formato yyyy-MM-dd
    val completed: Double = 0.0    // 1.0 si se completó, o cantidad
)
