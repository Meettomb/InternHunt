package com.example.internhunt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JobPostAdapter(private val jobList: List<InternshipPostData>) :
    RecyclerView.Adapter<JobPostAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.JobTitle)
        val internshipTypeText: TextView = itemView.findViewById(R.id.internshipType)
        val internshipTimeText: TextView = itemView.findViewById(R.id.internshipTime)
        val stipendText: TextView = itemView.findViewById(R.id.Stipend)
        val deadlineText: TextView = itemView.findViewById(R.id.Deadline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.job_post_item, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]
        holder.titleText.text = job.title
        holder.internshipTypeText.text = job.internshipType
        holder.internshipTimeText.text = job.internshipTime
        holder.stipendText.text = job.stipend
        holder.deadlineText.text = job.applicationDeadline
    }

    override fun getItemCount(): Int = jobList.size
}
