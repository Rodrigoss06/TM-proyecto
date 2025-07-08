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
                            Log.d("FirestoreManager", "✨ Hábito nuevo detectado. Incrementando contador de hábitos.")
                            userRef.update("total_habits", FieldValue.increment(1))
                        }
                        Log.d("FirestoreManager", "✅ Hábito guardado correctamente: ${habito.id}")
                        callback(true)
                    }
                    .addOnFailureListener {
                        Log.e("FirestoreManager", "❌ Error al guardar hábito: ${it.message}", it)
                        callback(false)
                    }
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

        val updates = mapOf(
            "last_completed_date" to dateStr,
            "current_streak" to newStreak,
            "longest_streak" to updatedLongest
        )

        Log.d("FirestoreManager", "✅ Marcando hábito como completado → ID: ${habito.id}")
        Log.d("FirestoreManager", "📈 Rachas actualizadas: $updates")

        habitsRef.document(habito.id)
            .update(updates)
            .addOnSuccessListener {
                Log.d("FirestoreManager", "✅ Hábito actualizado con nueva racha")
                actualizarUsuarioStats {
                    callback(true)
                }
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al actualizar hábito completado", it)
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
        Log.d("FirestoreManager", "👑 Consultando leaderboard...")

        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val rankings = result.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val completionRate = doc.getDouble("completion_rate") ?: 0.0
                    val currentStreak = (doc.getLong("current_streak") ?: 0L).toInt()
                    UserRanking(name, completionRate, currentStreak)
                }

                Log.d("FirestoreManager", "✅ Leaderboard cargado con ${rankings.size} usuarios")
                callback(rankings.sortedByDescending { it.completionRate })
            }
            .addOnFailureListener {
                Log.e("FirestoreManager", "❌ Error al cargar leaderboard", it)
                callback(emptyList())
            }
    }

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
