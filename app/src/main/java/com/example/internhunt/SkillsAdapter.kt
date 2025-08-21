package com.example.internhunt

import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context


class SkillsAdapter(
    private val context: Context,
    private val skills: MutableList<String>,
    private val onEditClick: (position: Int, skill: String) -> Unit
) : RecyclerView.Adapter<SkillsAdapter.SkillViewHolder>() {

    inner class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val skillTextView: TextView = itemView.findViewById(R.id.skills)
        val editIcon: ImageView = itemView.findViewById(R.id.edit_skills)

        init {
            editIcon.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onEditClick(pos, skills[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.skill_list, parent, false)
        return SkillViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = skills[position]
        holder.skillTextView.text = skill

        val prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val role = prefs.getString("role", null).toString().lowercase()

        if (role == "student") {
            holder.editIcon.visibility = View.VISIBLE
        } else {
            holder.editIcon.visibility = View.GONE
        }

        holder.editIcon.setOnClickListener {
            onEditClick(position, skill)
        }
    }

    override fun getItemCount(): Int = skills.size

    fun updateSkills(newSkills: List<String>) {
        skills.clear()
        skills.addAll(newSkills)
        notifyDataSetChanged()
    }
}

