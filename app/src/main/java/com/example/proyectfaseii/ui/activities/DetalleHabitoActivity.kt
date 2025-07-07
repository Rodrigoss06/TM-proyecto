package com.example.proyectfaseii.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.example.proyectfaseii.data.models.Habito
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetalleHabitoActivity : AppCompatActivity() {

    private lateinit var tvHabitName: TextView
    private lateinit var tvHabitArea: TextView
    private lateinit var tvHabitPriority: TextView
    private lateinit var tvStreaks: TextView
    private lateinit var lineChart: LineChart
    private lateinit var btnDelete: Button
    private lateinit var btnArchive: Button
    private lateinit var btnEdit: Button

    private var habitoId: String? = null
    private lateinit var habito: Habito

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_detalle_habito) // ✅ Reutiliza el layout

        // Obtener ID del hábito
        habitoId = intent.getStringExtra("habitoId")
        if (habitoId == null) {
            Toast.makeText(this, "ID de hábito no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Bind views
        tvHabitName = findViewById(R.id.tv_habit_name)
        tvHabitArea = findViewById(R.id.tv_habit_area)
        tvHabitPriority = findViewById(R.id.tv_habit_priority)
        tvStreaks = findViewById(R.id.tv_streaks)
        lineChart = findViewById(R.id.line_chart)
        btnDelete = findViewById(R.id.btn_delete)
        btnArchive = findViewById(R.id.btn_archive)
        btnEdit = findViewById(R.id.btn_edit)

        // Cargar datos
        FirestoreManager.getHabitById(habitoId!!) { habit ->
            if (habit != null) {
                habito = habit
                populateUI()
            } else {
                Toast.makeText(this, "Hábito no encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Botones
        btnDelete.setOnClickListener {
            FirestoreManager.deleteHabit(habitoId!!) {
                Toast.makeText(this, "Hábito eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnArchive.setOnClickListener {
            FirestoreManager.archiveHabit(habitoId!!) {
                Toast.makeText(this, "Hábito archivado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnEdit.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de edición pendiente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateUI() {
        tvHabitName.text = habito.name
        tvHabitArea.text = "Área: ${habito.area?.name ?: "N/A"}"
        tvHabitPriority.text = "Prioridad: ${mapPriority(habito.priority)}"
        tvStreaks.text = "🔥 Racha actual: ${habito.current_streak} / Récord: ${habito.longest_streak}"

        drawChart(habito)
    }

    private fun drawChart(habit: Habito) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        val formatter = DateTimeFormatter.ISO_DATE

        val sortedHistory = habit.goal_history_items.sortedBy { it.date }

        sortedHistory.forEachIndexed { index, goal ->
            val dateLabel = LocalDate.parse(goal.date, formatter).dayOfMonth.toString()
            entries.add(Entry(index.toFloat(), goal.completed.toFloat()))
            labels.add(dateLabel)
        }

        val dataSet = LineDataSet(entries, "Progreso").apply {
            color = getColor(R.color.purple_700)
            valueTextSize = 10f
            lineWidth = 2f
            setDrawFilled(true)
        }

        lineChart.apply {
            data = LineData(dataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
            }
            invalidate()
        }
    }

    private fun mapPriority(value: Double): String {
        return when {
            value >= 0.75 -> "Alta 🔥"
            value >= 0.5 -> "Media 💧"
            else -> "Baja 🧊"
        }
    }
}
