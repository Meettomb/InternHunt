package com.example.internhunt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.collections.set


class JobPostAdapter(
    private val jobList: List<InternshipPostData>,
    private val onItemClick: (InternshipPostData) -> Unit
) : RecyclerView.Adapter<JobPostAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.JobTitle)
        val internshipTypeText: TextView = itemView.findViewById(R.id.internshipType)
        val internshipTimeText: TextView = itemView.findViewById(R.id.internshipTime)
        val stipendText: TextView = itemView.findViewById(R.id.Stipend)
        val deadlineText: TextView = itemView.findViewById(R.id.Deadline)
        val DeadlineLayout: LinearLayout = itemView.findViewById(R.id.DeadlineLayout)
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

        val deadlineStr = job.applicationDeadline
        if (deadlineStr != "N/A") {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val deadlineDate = sdf.parse(deadlineStr)
                val today = Calendar.getInstance().time
                if (today.after(deadlineDate)) {
                    holder.DeadlineLayout.visibility = View.GONE
                } else {
                    holder.DeadlineLayout.visibility = View.VISIBLE
                    holder.deadlineText.text = deadlineStr
                }
            } catch (e: Exception) {
                holder.DeadlineLayout.visibility = View.VISIBLE
                holder.deadlineText.text = deadlineStr
            }
        } else {
            holder.deadlineText.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(job)
        }
    }

    override fun getItemCount(): Int = jobList.size
}
