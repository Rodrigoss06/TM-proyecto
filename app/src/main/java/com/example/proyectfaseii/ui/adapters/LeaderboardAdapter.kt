package com.example.proyectfaseii.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectfaseii.R
import com.example.proyectfaseii.data.models.UserRanking

class LeaderboardAdapter(
    private val users: List<UserRanking>
) : RecyclerView.Adapter<LeaderboardAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard_row, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    override fun getItemCount(): Int = users.size

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val tvRate: TextView = itemView.findViewById(R.id.tv_rate)

        fun bind(user: UserRanking, position: Int) {
            tvName.text = user.name
            tvRank.text = when (position) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> "#${position + 1}"
            }
            tvRate.text = String.format("%.1f%%", user.completionRate)
        }
    }
}
