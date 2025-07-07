package com.example.proyectfaseii.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.Habito

class HabitsAdapter(
    private val habits: List<Habito>,
    private val onReadMore: (Habito) -> Unit,
    private val onEdit: (Habito) -> Unit,
    private val onArchive: (Habito) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_card, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val tvHabitSchedule: TextView = itemView.findViewById(R.id.tv_habit_schedule)
        private val btnReadMore: ImageButton = itemView.findViewById(R.id.btn_read_more)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        private val btnArchive: ImageButton = itemView.findViewById(R.id.btn_archive)

        fun bind(habito: Habito) {
            tvHabitName.text = habito.name
            tvHabitSchedule.text = habito.time_of_day.joinToString(" / ")

            btnReadMore.setOnClickListener { onReadMore(habito) }
            btnEdit.setOnClickListener { onEdit(habito) }
            btnArchive.setOnClickListener { onArchive(habito) }
        }
    }
}
