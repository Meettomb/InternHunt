package com.example.internhunt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EducationAdapter(
    private val education: MutableList<EducationEntry>,
    private val onEditClick: (position: Int, education: EducationEntry) -> Unit
) : RecyclerView.Adapter<EducationAdapter.EducationViewHolder>() {


    inner class EducationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collageName: TextView = itemView.findViewById(R.id.collageName)
        val degreeName: TextView = itemView.findViewById(R.id.degreeName)
        val academicYear: TextView = itemView.findViewById(R.id.academicYear)
        val editIcon: ImageView = itemView.findViewById(R.id.editIcon)

        init {
            editIcon.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onEditClick(pos, education[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EducationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.education_list, parent, false)
        return EducationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EducationViewHolder, position: Int) {
        val edu = education[position]
        holder.collageName.text = edu.collage_name
        holder.degreeName.text = edu.degree_name
        holder.academicYear.text = "${edu.graduation_start_year} - ${edu.graduation_end_year}"

        holder.editIcon.setOnClickListener {
            onEditClick(position, education[position])
        }
    }

    override fun getItemCount(): Int = education.size

    fun updateEducation(newEducation: List<EducationEntry>) {
        education.clear()
        education.addAll(newEducation)
        notifyDataSetChanged()
    }

}