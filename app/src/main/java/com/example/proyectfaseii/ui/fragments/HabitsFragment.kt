package com.example.proyectfaseii.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.Habito
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.example.proyectfaseii.ui.activities.DetalleHabitoActivity
import com.example.proyectfaseii.ui.adapters.HabitsAdapter
import com.example.proyectfaseii.ui.modals.CrearHabitoModal
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment() {

    private lateinit var rvHabitsList: RecyclerView
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var groupFilter: MaterialButtonToggleGroup
    private lateinit var btnDaily: MaterialButton
    private lateinit var btnWeekly: MaterialButton
    private lateinit var btnMonthly: MaterialButton
    private lateinit var btnYearly: MaterialButton

    private lateinit var adapter: HabitsAdapter
    private val habitsList = mutableListOf<Habito>()
    private var currentFilter = "Daily"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvHabitsList = view.findViewById(R.id.rv_habits_list)
        fabAddHabit = view.findViewById(R.id.fab_add_habit)
        groupFilter = view.findViewById(R.id.group_filter)
        btnDaily = view.findViewById(R.id.btn_daily)
        btnWeekly = view.findViewById(R.id.btn_weekly)
        btnMonthly = view.findViewById(R.id.btn_monthly)
        btnYearly = view.findViewById(R.id.btn_yearly)

        setupRecycler()
        setupFilterButtons()
        loadHabitsByFrequency(currentFilter)

        fabAddHabit.setOnClickListener {
            val modal = CrearHabitoModal()
            modal.show(parentFragmentManager, "CrearHabitoModal")
        }


    }

    private fun setupRecycler() {
        adapter = HabitsAdapter(
            habitsList,
            onReadMore = { habito ->
                val intent = Intent(requireContext(), DetalleHabitoActivity::class.java)
                intent.putExtra("habitoId", habito.id)
                startActivity(intent)
            },
            onEdit = { habito ->
                val modal = CrearHabitoModal.newInstance(habito)
                modal.show(parentFragmentManager, "EditarHabitoModal")
            },
            onArchive = { habito -> archiveHabit(habito) }
        )

        rvHabitsList.layoutManager = LinearLayoutManager(requireContext())
        rvHabitsList.adapter = adapter
    }

    private fun setupFilterButtons() {
        groupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentFilter = when (checkedId) {
                    btnDaily.id -> "Daily"
                    btnWeekly.id -> "Weekly"
                    btnMonthly.id -> "Monthly"
                    btnYearly.id -> "Yearly"
                    else -> "Daily"
                }
                loadHabitsByFrequency(currentFilter)
            }
        }
    }

    private fun loadHabitsByFrequency(freq: String) {
        FirestoreManager.getHabitsByFrequency(freq) { fetched ->
            habitsList.clear()
            habitsList.addAll(fetched)
            adapter.notifyDataSetChanged()
        }
    }

    private fun archiveHabit(habito: Habito) {
        FirestoreManager.archiveHabit(habito.id) {
            loadHabitsByFrequency(currentFilter)
        }
    }
}
