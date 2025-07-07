package com.example.proyectfaseii.data.firebase

import android.util.Log
import com.example.proyectfaseii.data.models.*
import com.example.proyectfaseii.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val uid: String
        get() = FirebaseAuth.getInstance().uid ?: ""

    private val habitsRef get() = db.collection("usuarios").document(uid).collection("habitos")
    private val userRef get() = db.collection("usuarios").document(uid)

    fun saveOrUpdateHabit(habito: Habito, callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().uid

        if (userId.isNullOrBlank()) {
            Log.e("FirestoreManager", "❌ UID está vacío. No se puede guardar el hábito.")
            callback(false)
            return
        }

        // 🔍 Log del objeto a guardar (usa Gson si quieres JSON)
        Log.d("FirestoreManager", "✅ UID usado: $userId")
        Log.d("FirestoreManager", "📤 Hábito a guardar: $habito")

        val habitsRef = db.collection("usuarios").document(userId).collection("habitos")

        habitsRef.document(habito.id)
            .set(habito)
            .addOnSuccessListener {
                Log.d("FirestoreManager", "✅ Hábito guardado correctamente: ${habito.id}")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreManager", "❌ Error al guardar hábito: ${e.message}", e)
                callback(false)
            }
    }


    fun deleteHabit(habitId: String, callback: () -> Unit) {
        if (uid.isBlank()) return
        habitsRef.document(habitId)
            .delete()
            .addOnSuccessListener { callback() }
    }

    fun archiveHabit(habitId: String, callback: () -> Unit) {
        if (uid.isBlank()) return
        habitsRef.document(habitId)
            .update("is_archived", true)
            .addOnSuccessListener { callback() }
    }

    fun getHabitById(habitId: String, callback: (Habito?) -> Unit) {
        if (uid.isBlank()) return callback(null)
        habitsRef.document(habitId)
            .get()
            .addOnSuccessListener { doc ->
                callback(doc.toObject(Habito::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getHabitsForDay(date: LocalDate, callback: (List<Habito>) -> Unit) {
        if (uid.isBlank()) return callback(emptyList())

        habitsRef
            .whereEqualTo("is_archived", false)
            .get()
            .addOnSuccessListener { docs ->
                val filtered = docs.mapNotNull { it.toObject(Habito::class.java) }
                    .filter { it.time_of_day.isNotEmpty() }
                callback(filtered)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun markHabitAsCompleted(habito: Habito, date: LocalDate, callback: (Boolean) -> Unit) {
        if (uid.isBlank()) return callback(false)

        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        val updatedStreak = if (Utils.isYesterday(habito.last_completed_date, dateStr)) {
            habito.current_streak + 1
        } else 1

        val updates = mapOf(
            "last_completed_date" to dateStr,
            "current_streak" to updatedStreak,
            "longest_streak" to maxOf(updatedStreak, habito.longest_streak)
        )

        habitsRef.document(habito.id)
            .update(updates)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getHabitsByFrequency(frequency: String, callback: (List<Habito>) -> Unit) {
        if (uid.isBlank()) return callback(emptyList())
        habitsRef
            .whereEqualTo("recurrence", frequency)
            .whereEqualTo("is_archived", false)
            .get()
            .addOnSuccessListener { result ->
                val habits = result.mapNotNull { it.toObject(Habito::class.java) }
                callback(habits)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun getLeaderboard(callback: (List<UserRanking>) -> Unit) {
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val rankings = result.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val completionRate = doc.getDouble("completion_rate") ?: 0.0
                    val currentStreak = (doc.getLong("current_streak") ?: 0L).toInt()
                    UserRanking(name, completionRate, currentStreak)
                }
                callback(rankings.sortedByDescending { it.completionRate })
            }
    }

    fun getCurrentUserProfile(callback: (UserRanking) -> Unit) {
        if (uid.isBlank()) return
        userRef.get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Usuario"
                val completionRate = doc.getDouble("completion_rate") ?: 0.0
                val currentStreak = (doc.getLong("current_streak") ?: 0).toInt()
                val longestStreak = (doc.getLong("longest_streak") ?: 0).toInt()
                val total = (doc.getLong("total_habits") ?: 0).toInt()
                val completed = (doc.getLong("habits_completed") ?: 0).toInt()

                callback(
                    UserRanking(
                        name,
                        completionRate,
                        currentStreak,
                        longestStreak,
                        total,
                        completed
                    )
                )
            }
    }
}
