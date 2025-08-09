package com.example.internhunt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import java.text.SimpleDateFormat


class JobPostAdapter(private val jobList: List<InternshipPostData>) :
    RecyclerView.Adapter<JobPostAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.JobTitle)
        val internshipTypeText: TextView = itemView.findViewById(R.id.internshipType)
        val internshipTimeText: TextView = itemView.findViewById(R.id.internshipTime)
        val stipendText: TextView = itemView.findViewById(R.id.Stipend)
        val deadlineText: TextView = itemView.findViewById(R.id.Deadline)
        val DeadlineLayout: LinearLayout = itemView.findViewById(R.id.DeadlineLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_post_item, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]
        holder.titleText.text = job.title
        holder.internshipTypeText.text = job.internshipType
        holder.internshipTimeText.text = job.internshipTime
        holder.stipendText.text = job.stipend

        val deadlineStr = job.applicationDeadline

        if (deadlineStr != "N/A") {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val deadlineDate = sdf.parse(deadlineStr)
                val today = java.util.Calendar.getInstance().time

                if (today.after(deadlineDate)) {
                    // Hide deadline if it's expired
                    holder.DeadlineLayout.visibility = View.GONE
                } else {
                    // Show deadline if it's still valid
                    holder.DeadlineLayout.visibility = View.VISIBLE
                    holder.deadlineText.text = deadlineStr
                }
            } catch (e: Exception) {
                // In case of parse error, just show the deadline as fallback
                holder.DeadlineLayout.visibility = View.VISIBLE
                holder.deadlineText.text = deadlineStr
            }
        } else {
            holder.deadlineText.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = jobList.size
}
