package com.example.proyectfaseii.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.ui.adapters.LeaderboardAdapter
import com.example.proyectfaseii.data.firebase.FirestoreManager
import com.example.proyectfaseii.data.models.UserRanking
import com.google.android.material.button.MaterialButtonToggleGroup

class LeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupPeriod: MaterialButtonToggleGroup
    private val users = mutableListOf<UserRanking>()
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.rv_leaderboard)
        groupPeriod = view.findViewById(R.id.group_period)

        setupRecycler()
        setupFilterButtons()
        loadLeaderboard("today")
    }

    private fun setupRecycler() {
        adapter = LeaderboardAdapter(users)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupFilterButtons() {
        groupPeriod.addOnButtonCheckedListener { _, checkedId, _ ->
            val period = when (checkedId) {
                R.id.btn_today -> "today"
                R.id.btn_week -> "week"
                R.id.btn_month -> "month"
                R.id.btn_year -> "year"
                else -> "today"
            }
            loadLeaderboard(period)
        }
    }

    private fun loadLeaderboard(period: String) {
        FirestoreManager.getLeaderboard { usersData ->
            users.clear()
            users.addAll(usersData)
            adapter.notifyDataSetChanged()
        }
    }
}
