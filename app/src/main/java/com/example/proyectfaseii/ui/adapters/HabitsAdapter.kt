package com.example.proyectfaseii.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.Habito
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        val habito = habits[position]
        holder.bind(habito)
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val tvRecurrence: TextView = itemView.findViewById(R.id.tv_recurrence_info)
        private val tvWeekdays: TextView = itemView.findViewById(R.id.tv_weekdays)
        private val tvWeekRange: TextView = itemView.findViewById(R.id.tv_week_range)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)

        private val btnReadMore: ImageButton = itemView.findViewById(R.id.btn_read_more)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        private val btnArchive: ImageButton = itemView.findViewById(R.id.btn_archive)

        fun bind(habito: Habito) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(habito.start_date, formatter)
            val end = habito.end_date?.let { LocalDate.parse(it, formatter) }
            val today = LocalDate.now()

            // Título
            tvHabitName.text = habito.name

            // Recurrencia + fecha
            tvRecurrence.text = "${habito.recurrence} • ${start.format(formatter)}" +
                    (end?.let { " → ${it.format(formatter)}" } ?: "")

            // Días seleccionados
            if (habito.recurrence == "Weekly" || habito.recurrence == "Daily") {
                val dias = habito.time_of_day.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } }
                tvWeekdays.text = dias
                tvWeekdays.visibility = View.VISIBLE

                // Semana actual
                val startOfWeek = today.with(DayOfWeek.MONDAY)
                val endOfWeek = startOfWeek.plusDays(6)
                tvWeekRange.text = "${startOfWeek.dayOfWeek.name.take(3)} ${startOfWeek.dayOfMonth} – " +
                        "${endOfWeek.dayOfWeek.name.take(3)} ${endOfWeek.dayOfMonth}"
                tvWeekRange.visibility = View.VISIBLE
            } else {
                tvWeekdays.visibility = View.GONE
                tvWeekRange.visibility = View.GONE
            }

            // Progreso con streak
            tvProgress.text = "Racha actual: ${habito.current_streak} días • Récord: ${habito.longest_streak}"

            // Acciones
            btnReadMore.setOnClickListener { onReadMore(habito) }
            btnEdit.setOnClickListener { onEdit(habito) }
            btnArchive.setOnClickListener { onArchive(habito) }
        }

    }
}
