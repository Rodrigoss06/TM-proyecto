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

        val habitDoc = habitsRef.document(habito.id)

        habitDoc.get().addOnSuccessListener { doc ->
            val isNew = !doc.exists()

            habitDoc.set(habito)
                .addOnSuccessListener {
                    if (isNew) {
                        Log.d("FirestoreManager", "✨ Nuevo hábito detectado: ${habito.name}")
                        userRef.update("total_habits", FieldValue.increment(1))
                            .addOnSuccessListener {
                                Log.d("FirestoreManager", "📊 total_habits incrementado en Firestore")
                            }
                            .addOnFailureListener {
                                Log.e("FirestoreManager", "❌ Error al actualizar total_habits", it)
                            }
                    } else {
                        Log.d("FirestoreManager", "📝 Hábito existente actualizado")
                    }

                    callback(true)
                }
                .addOnFailureListener {
                    Log.e("FirestoreManager", "❌ Error al guardar hábito: ${it.message}", it)
                    callback(false)
                }

        }.addOnFailureListener {
            Log.e("FirestoreManager", "❌ Error al verificar si hábito existe", it)
            callback(false)
        }
    }

    fun deleteHabit(habitId: String, callback: () -> Unit) {
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede eliminar hábito.")
            return
        }

        Log.d("FirestoreManager", "🗑 Eliminando hábito con ID: $habitId")

        habitsRef.document(habitId)
            .delete()
            .addOnSuccessListener {
                Log.d("FirestoreManager", "✅ Hábito eliminado correctamente")
                callback()
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al eliminar hábito: ${it.message}", it)
            }
    }

    fun archiveHabit(habitId: String, callback: () -> Unit) {
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede archivar hábito.")
            return
        }

        Log.d("FirestoreManager", "📦 Archivando hábito: $habitId")

        habitsRef.document(habitId)
            .update("_archived", true)
            .addOnSuccessListener {
                Log.d("FirestoreManager", "✅ Hábito archivado correctamente")
                callback()
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al archivar hábito: ${it.message}", it)
            }
    }

    fun getHabitById(habitId: String, callback: (Habito?) -> Unit) {
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede obtener hábito.")
            callback(null)
            return
        }

        Log.d("FirestoreManager", "🔍 Obteniendo hábito por ID: $habitId")

        habitsRef.document(habitId)
            .get()
            .addOnSuccessListener {
                val habito = it.toObject(Habito::class.java)
                Log.d("FirestoreManager", "✅ Hábito encontrado: $habito")
                callback(habito)
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al obtener hábito: ${it.message}", it)
                callback(null)
            }
    }

    fun getHabitsForDay(date: LocalDate, callback: (List<Habito>) -> Unit) {
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede consultar hábitos para el día.")
            callback(emptyList())
            return
        }

        Log.d("FirestoreManager", "📆 Consultando hábitos para el día: $date")

        habitsRef
            .whereEqualTo("_archived", false)
            .get()
            .addOnSuccessListener { docs ->
                val dayKey = date.dayOfWeek.name.lowercase().take(3) // ej: "mon", "tue", ...
                val filtered = docs.mapNotNull { it.toObject(Habito::class.java) }
                    .filter { habito ->
                        habito.recurrence == "Daily" || habito.time_of_day.contains(dayKey)
                    }


                Log.d("FirestoreManager", "✅ Hábitos filtrados para el día: ${filtered.size}")
                callback(filtered)
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al consultar hábitos para el día", it)
                callback(emptyList())
            }
    }

    fun markHabitAsCompleted(habito: Habito, date: LocalDate, callback: (Boolean) -> Unit) {
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede marcar hábito como completado.")
            callback(false)
            return
        }

        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        val newStreak = if (Utils.isYesterday(habito.last_completed_date, dateStr)) {
            habito.current_streak + 1
        } else 1
        val updatedLongest = maxOf(habito.longest_streak, newStreak)

        val habitUpdates = mapOf(
            "last_completed_date" to dateStr,
            "current_streak" to newStreak,
            "longest_streak" to updatedLongest
        )

        Log.d("FirestoreManager", "✅ Completando hábito ${habito.name} → $habitUpdates")

        habitsRef.document(habito.id)
            .update(habitUpdates)
            .addOnSuccessListener {
                Log.d("FirestoreManager", "📌 Hábito actualizado")

                userRef.get()
                    .addOnSuccessListener { doc ->
                        val userCurrentStreak = (doc.getLong("current_streak") ?: 0).toInt()
                        val userLongestStreak = (doc.getLong("longest_streak") ?: 0).toInt()
                        val userHabitsCompleted = (doc.getLong("habits_completed") ?: 0).toInt()

                        val updates = mutableMapOf<String, Any>(
                            "habits_completed" to userHabitsCompleted + 1
                        )

                        if (newStreak > userCurrentStreak) {
                            updates["current_streak"] = newStreak
                        }

                        if (updatedLongest > userLongestStreak) {
                            updates["longest_streak"] = updatedLongest
                        }

                        userRef.update(updates)
                            .addOnSuccessListener {
                                Log.d("FirestoreManager", "🎯 Usuario actualizado con: $updates")
                                callback(true)
                            }
                            .addOnFailureListener {
                                Log.e("FirestoreManager", "❌ Error al actualizar usuario", it)
                                callback(false)
                            }
                    }
                    .addOnFailureListener {
                        Log.e("FirestoreManager", "❌ Error al obtener documento de usuario", it)
                        callback(false)
                    }
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al actualizar hábito", it)
                callback(false)
            }
    }

    private fun actualizarUsuarioStats(callback: () -> Unit) {
        Log.d("FirestoreManager", "📊 Actualizando estadísticas del usuario...")

        userRef.get().addOnSuccessListener { doc ->
            val total = (doc.getLong("total_habits") ?: 0).toInt()
            val completados = (doc.getLong("habits_completed") ?: 0).toInt() + 1
            val completionRate = if (total > 0) completados.toDouble() / total else 0.0

            val updates = mapOf(
                "habits_completed" to completados,
                "completion_rate" to completionRate
            )

            Log.d("FirestoreManager", "📌 Nuevos valores: $updates")

            userRef.update(updates)
                .addOnSuccessListener {
                    Log.d("FirestoreManager", "✅ Estadísticas del usuario actualizadas")
                    callback()
                }
                .addOnFailureListener {
                    Log.e("FirestoreManager", "❌ Error al actualizar estadísticas", it)
                    callback()
                }
        }.addOnFailureListener {
            Log.e("FirestoreManager", "❌ Error al leer datos del usuario", it)
            callback()
        }
    }

    fun getHabitsByFrequency(frequency: String, callback: (List<Habito>) -> Unit) {
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede obtener hábitos por frecuencia.")
            callback(emptyList())
            return
        }

        Log.d("FirestoreManager", "🔄 Buscando hábitos con frecuencia: $frequency")

        habitsRef
            .whereEqualTo("recurrence", frequency)
            .whereEqualTo("_archived", false)
            .get()
            .addOnSuccessListener { result ->
                val habits = result.mapNotNull { it.toObject(Habito::class.java) }
                Log.d("FirestoreManager", "✅ Resultados encontrados: ${habits.size}")
                callback(habits)
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al consultar hábitos por frecuencia", it)
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
                    val longestStreak = (doc.getLong("longest_streak") ?: 0L).toInt()
                    val totalHabits = (doc.getLong("total_habits") ?: 0L).toInt()
                    val habitsCompleted = (doc.getLong("habits_completed") ?: 0L).toInt()

                    UserRanking(name, completionRate, currentStreak, longestStreak, totalHabits, habitsCompleted)
                }

                val finalList = if (rankings.isEmpty()) {
                    Log.w("FirestoreManager", "📉 No hay usuarios en leaderboard, usando datos de prueba")
                    getMockRankings()
                } else {
                    rankings.sortedByDescending { it.completionRate }
                }

                callback(finalList)
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al cargar leaderboard", it)
                callback(getMockRankings()) // fallback también en error
            }
    }

    private fun getMockRankings(): List<UserRanking> = listOf(
        UserRanking("Alice 💪", 0.92, 12, 20, 15, 14),
        UserRanking("Carlos 🚀", 0.85, 7, 15, 10, 8),
        UserRanking("María 🔥", 0.76, 5, 10, 12, 9),
        UserRanking("Tú 🤖", 0.60, 3, 5, 7, 4)
    )


    fun getCurrentUserProfile(callback: (UserRanking) -> Unit) {
        if (uid.isBlank()) {
            Log.e("FirestoreManager", "❌ UID vacío. No se puede obtener perfil.")
            return
        }

        Log.d("FirestoreManager", "👤 Cargando perfil de usuario...")

        userRef.get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Usuario"
                val completionRate = doc.getDouble("completion_rate") ?: 0.0
                val currentStreak = (doc.getLong("current_streak") ?: 0).toInt()
                val longestStreak = (doc.getLong("longest_streak") ?: 0).toInt()
                val total = (doc.getLong("total_habits") ?: 0).toInt()
                val completed = (doc.getLong("habits_completed") ?: 0).toInt()

                Log.d("FirestoreManager", "✅ Perfil cargado correctamente")

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
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al obtener perfil", it)
            }
    }
}
