package com.example.proyectfaseii.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.Habito
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.example.proyectfaseii.ui.adapters.DaySelectorAdapter
import com.example.proyectfaseii.ui.adapters.HabitoTodayAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayFragment : Fragment() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var rvDaySelector: RecyclerView
    private lateinit var rvHabitsToday: RecyclerView
    private lateinit var progressLoading: ProgressBar
    private lateinit var tvEmptyState: TextView

    private lateinit var adapter: HabitoTodayAdapter
    private var selectedDate: LocalDate = LocalDate.now()
    private val habitsToday = mutableListOf<Habito>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Vistas
        tvSelectedDate = view.findViewById(R.id.tv_selected_date)
        rvDaySelector = view.findViewById(R.id.rv_day_selector)
        rvHabitsToday = view.findViewById(R.id.rv_habits_today)
        progressLoading = view.findViewById(R.id.progress_loading)
        tvEmptyState = view.findViewById(R.id.tv_empty_state)

        setupDaySelector()
        setupRecycler()
        loadHabitsForToday()
    }

    private fun setupDaySelector() {
        val days = (0..6).map { LocalDate.now().minusDays(3).plusDays(it.toLong()) }
        val dayAdapter = DaySelectorAdapter(days) { date ->
            selectedDate = date
            tvSelectedDate.text = date.format(DateTimeFormatter.ofPattern("EEEE d MMMM"))
            loadHabitsForToday()
        }

        rvDaySelector.adapter = dayAdapter
        rvDaySelector.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        tvSelectedDate.text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE d MMMM"))
    }

    private fun setupRecycler() {
        adapter = HabitoTodayAdapter(habitsToday) { habito ->
            marcarComoCompletado(habito)
        }
        rvHabitsToday.layoutManager = LinearLayoutManager(requireContext())
        rvHabitsToday.adapter = adapter
    }

    private fun loadHabitsForToday() {
        progressLoading.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        FirestoreManager.getHabitsForDay(selectedDate) { fetchedHabits ->
            habitsToday.clear()
            habitsToday.addAll(fetchedHabits)
            adapter.notifyDataSetChanged()

            progressLoading.visibility = View.GONE
            tvEmptyState.visibility = if (habitsToday.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun marcarComoCompletado(habito: Habito) {
        FirestoreManager.markHabitAsCompleted(habito, selectedDate) { success ->
            if (success) {
                Toast.makeText(requireContext(), "¡Hábito marcado como completado!", Toast.LENGTH_SHORT).show()
                loadHabitsForToday()
            } else {
                Toast.makeText(requireContext(), "Error al completar hábito", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
