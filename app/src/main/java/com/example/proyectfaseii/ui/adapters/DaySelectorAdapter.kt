package com.example.proyectfaseii.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DaySelectorAdapter(
    private val days: List<LocalDate>,
    private val onDayClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<DaySelectorAdapter.DayViewHolder>() {

    private var selectedPosition = 0

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayName: TextView = itemView.findViewById(R.id.tv_day_name)
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tv_day_number)
        private val cardDay: MaterialCardView = itemView.findViewById(R.id.card_day)

        fun bind(date: LocalDate, isSelected: Boolean) {
            val dayFormatter = DateTimeFormatter.ofPattern("EEE")
            val numFormatter = DateTimeFormatter.ofPattern("dd")

            tvDayName.text = date.format(dayFormatter)
            tvDayNumber.text = date.format(numFormatter)
            cardDay.isChecked = isSelected

            itemView.setOnClickListener {
                val oldPos = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                onDayClick(date)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_selector, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = days.size
}
