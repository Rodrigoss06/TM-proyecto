package com.example.proyectfaseii.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.Habito
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetalleHabitoFragment : Fragment() {

    private lateinit var tvHabitName: TextView
    private lateinit var tvHabitArea: TextView
    private lateinit var tvHabitPriority: TextView
    private lateinit var tvStreaks: TextView
    private lateinit var lineChart: LineChart
    private lateinit var btnDelete: Button
    private lateinit var btnArchive: Button
    private lateinit var btnEdit: Button

    private lateinit var habitoId: String
    private lateinit var habito: Habito

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detalle_habito, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Bind views
        tvHabitName = view.findViewById(R.id.tv_habit_name)
        tvHabitArea = view.findViewById(R.id.tv_habit_area)
        tvHabitPriority = view.findViewById(R.id.tv_habit_priority)
        tvStreaks = view.findViewById(R.id.tv_streaks)
        lineChart = view.findViewById(R.id.line_chart)
        btnDelete = view.findViewById(R.id.btn_delete)
        btnArchive = view.findViewById(R.id.btn_archive)
        btnEdit = view.findViewById(R.id.btn_edit)

        habitoId = arguments?.getString("habitoId") ?: return

        FirestoreManager.getHabitById(habitoId) { habit ->
            if (habit != null) {
                habito = habit
                populateUI()
            } else {
                Toast.makeText(requireContext(), "Hábito no encontrado", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            FirestoreManager.deleteHabit(habitoId) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        btnArchive.setOnClickListener {
            FirestoreManager.archiveHabit(habitoId) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        btnEdit.setOnClickListener {
            // TODO: Navegar a EditarHabitoFragment
            Toast.makeText(requireContext(), "Funcionalidad de edición pendiente", Toast.LENGTH_SHORT).show()
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
            color = requireContext().getColor(R.color.purple_700)
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
