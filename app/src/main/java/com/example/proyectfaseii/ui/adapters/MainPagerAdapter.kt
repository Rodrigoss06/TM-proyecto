package com.example.proyectfaseii.ui.adapters

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.proyectfaseii.ui.fragments.HabitsFragment
import com.example.proyectfaseii.ui.fragments.LeaderboardFragment
import com.example.proyectfaseii.ui.fragments.ProfileFragment
import com.example.proyectfaseii.ui.fragments.TodayFragment

class MainPagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TodayFragment()
            1 -> HabitsFragment()
            2 -> LeaderboardFragment()
            3 -> ProfileFragment()
            else -> {
                Toast.makeText(fragmentActivity, "⚠️ Posición inválida: $position", Toast.LENGTH_LONG).show()
                Fragment()
            }
        }
    }
}
