package com.example.proyectfaseii.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.Habito

class HabitoTodayAdapter(
    private val habits: List<Habito>,
    private val onCheckClick: (Habito) -> Unit
) : RecyclerView.Adapter<HabitoTodayAdapter.HabitoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_today, parent, false)
        return HabitoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitoViewHolder, position: Int) {
        Log.d("AdapterToday", "🟢 Pintando hábito: ${habits[position].name}")
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val tvTimeOfDay: TextView = itemView.findViewById(R.id.tv_time_of_day)
        private val btnComplete: ImageView = itemView.findViewById(R.id.btn_complete)

        fun bind(habito: Habito) {

            tvHabitName.text = habito.name
            tvTimeOfDay.text = habito.time_of_day.joinToString(" / ")

            if (habito.current_streak > 0) {
                tvHabitName.append(" 🔥 ${habito.current_streak}")
            }

            btnComplete.setOnClickListener {
                btnComplete.setImageResource(R.drawable.ic_celebration)
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.purple_500)
                )
                onCheckClick(habito)
            }

        }
    }
}
