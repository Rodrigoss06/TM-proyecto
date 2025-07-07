package com.example.proyectfaseii.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Utils {
    fun isYesterday(lastDate: String, today: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ISO_DATE
            val last = LocalDate.parse(lastDate, formatter)
            val todayDate = LocalDate.parse(today, formatter)
            last.plusDays(1) == todayDate
        } catch (e: Exception) {
            false
        }
    }
}
