package com.example.internhunt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import com.example.internhunt.BookmarkAdapter.InternshipViewHolder

class AppliedInternshipAdapter (private val internship: List<InternshipApplications>,
                                private val onItemClick: (InternshipApplications, String?, String?)-> Unit):
    RecyclerView.Adapter<AppliedInternshipAdapter.AppliedInternshipHolder>() {
    private val postCache = mutableMapOf<String, Pair<String?, String?>>()

    inner class AppliedInternshipHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val companyImage: ImageView = itemView.findViewById(R.id.CompanyProfileImage)
        val title = itemView.findViewById<TextView>(R.id.JobTitle)
        val companyName: TextView = itemView.findViewById(R.id.companyname)
        val location = itemView.findViewById<TextView>(R.id.Location)
        val postid = itemView.findViewById<TextView>(R.id.postId)
        val userid = itemView.findViewById<TextView>(R.id.userId)
        val companyid = itemView.findViewById<TextView>(R.id.companyId)
        val closeHide = itemView.findViewById<ImageView>(R.id.closeHide)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppliedInternshipHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.applied_internship_item, parent, false)
        return AppliedInternshipHolder(view)
    }

    override fun onBindViewHolder(holder: AppliedInternshipHolder, position: Int) {
        val item = internship[position]

        val prefs =
            holder.itemView.context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("userid", null)

        if (currentUserId == item.userId) {
            //  Click on entire item
            holder.postid.text = item.postId
            holder.userid.text = item.userId
            holder.companyid.text = item.companyId

            val db = FirebaseFirestore.getInstance()
            db.collection("Users").document(item.companyId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val companyName = doc.getString("company_name") ?: "Unknown Company"
                        val profileImageUrl = doc.getString("profile_image_url")
                        holder.companyName.text = companyName
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(profileImageUrl)
                                .into(holder.companyImage)
                        }
                        postCache[item.postId] = Pair(companyName, profileImageUrl)
                    } else {
                        holder.companyName.text = "Company not found"
                    }
                }
                .addOnFailureListener {
                    holder.companyName.text = "Error loading company"
                }

            db.collection("internshipPostsData").document(item.postId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        holder.title.text = doc.getString("title") ?: "No Title"
                        holder.location.text = doc.getString("location") ?: "No Location"
                    }
                }
                .addOnFailureListener {
                    holder.title.text = "Error loading title"
                    holder.location.text = ""
                }



            holder.itemView.setOnClickListener {
                val cachedData = postCache[item.postId]
                onItemClick(item, cachedData?.first, cachedData?.second)
            }

            //  Click on hidePost icon
            holder.closeHide.setOnClickListener {
                val prefs = holder.itemView.context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                val currentUserId = prefs.getString("userid", null)

                if (currentUserId != null) {
                    db.collection("Users")
                        .document(currentUserId)
                        .update("removed_applied_posts", com.google.firebase.firestore.FieldValue.arrayUnion(item.id))
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Post Removed", Toast.LENGTH_SHORT).show()

                            val position = holder.adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                (internship as MutableList).removeAt(position)
                                notifyItemRemoved(position)
                            }

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

        }
    }



    override fun getItemCount(): Int = internship.size
}