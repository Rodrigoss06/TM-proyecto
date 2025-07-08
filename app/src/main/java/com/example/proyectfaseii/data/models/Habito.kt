package com.example.proyectfaseii.data.models

data class Habito(
    val id: String = "",
    val name: String = "",
    val is_archived: Boolean = false,
    val start_date: String = "", // yyyy-MM-dd
    val end_date: String? = null, // 🔄 nuevo
    val time_of_day: List<String> = emptyList(), // "mon", "tue", etc.
    val goal: Goal = Goal(),
    val goal_history_items: List<GoalHistoryItem> = emptyList(),
    val log_method: String = "", // "manual" o automático
    val recurrence: String = "", // Daily, Weekly, etc.
    val remind: List<String> = emptyList(), // horas como "08:00"
    val area: Area? = null,
    val created_date: String = "",
    val priority: Double = 0.0,

    val current_streak: Int = 0,
    val longest_streak: Int = 0,
    val last_completed_date: String = ""
)
