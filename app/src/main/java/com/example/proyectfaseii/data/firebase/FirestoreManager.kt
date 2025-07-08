package com.example.proyectfaseii.data.firebase

import android.util.Log
import com.example.proyectfaseii.data.models.*
import com.example.proyectfaseii.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede guardar hábito.")
            callback(false)
            return
        }

        Log.d("FirestoreManager", "📤 Guardando hábito: $habito")

        habitsRef.document(habito.id)
            .get()
            .addOnSuccessListener { doc ->
                val isNew = !doc.exists()
                habitsRef.document(habito.id)
                    .set(habito)
                    .addOnSuccessListener {
                        if (isNew) {
                            userRef.update("total_habits", FieldValue.increment(1))
                        }
                        Log.d("FirestoreManager", "✅ Hábito guardado correctamente")
                        callback(true)
                    }
                    .addOnFailureListener {
                        Log.e("FirestoreManager", "❌ Error al guardar hábito: ${it.message}", it)
                        callback(false)
                    }
            }
    }

    fun deleteHabit(habitId: String, callback: () -> Unit) {
        if (uid.isBlank()) return

        habitsRef.document(habitId)
            .delete()
            .addOnSuccessListener {
                callback()
            }
    }

    fun archiveHabit(habitId: String, callback: () -> Unit) {
        if (uid.isBlank()) return
        habitsRef.document(habitId)
            .update("_archived", true)
            .addOnSuccessListener { callback() }
    }

    fun getHabitById(habitId: String, callback: (Habito?) -> Unit) {
        if (uid.isBlank()) {
            callback(null)
            return
        }

        habitsRef.document(habitId)
            .get()
            .addOnSuccessListener {
                callback(it.toObject(Habito::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getHabitsForDay(date: LocalDate, callback: (List<Habito>) -> Unit) {
        if (uid.isBlank()) {
            callback(emptyList())
            return
        }

        habitsRef
            .whereEqualTo("_archived", false)
            .get()
            .addOnSuccessListener { docs ->
                val filtered = docs.mapNotNull { it.toObject(Habito::class.java) }
                    .filter { it.time_of_day.isNotEmpty() } // puedes refinar lógica según `date.dayOfWeek`

                callback(filtered)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun markHabitAsCompleted(habito: Habito, date: LocalDate, callback: (Boolean) -> Unit) {
        if (uid.isBlank()) {
            callback(false)
            return
        }

        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        val newStreak = if (Utils.isYesterday(habito.last_completed_date, dateStr)) {
            habito.current_streak + 1
        } else 1

        val updatedLongest = maxOf(habito.longest_streak, newStreak)

        val updates = mapOf(
            "last_completed_date" to dateStr,
            "current_streak" to newStreak,
            "longest_streak" to updatedLongest
        )

        habitsRef.document(habito.id)
            .update(updates)
            .addOnSuccessListener {
                Log.d("FirestoreManager", "✅ Hábito actualizado con rachas")
                actualizarUsuarioStats {
                    callback(true)
                }
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al marcar como completado", it)
                callback(false)
            }
    }

    private fun actualizarUsuarioStats(callback: () -> Unit) {
        userRef.get().addOnSuccessListener { doc ->
            val total = (doc.getLong("total_habits") ?: 0).toInt()
            val completados = (doc.getLong("habits_completed") ?: 0).toInt() + 1
            val completionRate = if (total > 0) completados.toDouble() / total else 0.0

            val updated = mapOf(
                "habits_completed" to completados,
                "completion_rate" to completionRate
            )

            userRef.update(updated)
                .addOnSuccessListener {
                    Log.d("FirestoreManager", "📈 Stats de usuario actualizadas: $updated")
                    callback()
                }
                .addOnFailureListener {
                    Log.e("FirestoreManager", "❌ Error al actualizar usuario", it)
                    callback()
                }
        }.addOnFailureListener {
            Log.e("FirestoreManager", "❌ Error al leer datos de usuario", it)
            callback()
        }
    }

    fun getHabitsByFrequency(frequency: String, callback: (List<Habito>) -> Unit) {
        if (uid.isBlank()) {
            callback(emptyList())
            return
        }

        habitsRef
            .whereEqualTo("recurrence", frequency)
            .whereEqualTo("_archived", false)
            .get()
            .addOnSuccessListener { result ->
                val habits = result.mapNotNull { it.toObject(Habito::class.java) }
                callback(habits)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
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
            .addOnFailureListener {
                callback(emptyList())
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
