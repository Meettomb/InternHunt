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


class InternshipAdapter(private val internship: List<InternshipPostData>,
                        private val onItemClick: (InternshipPostData, String?, String?) -> Unit ):
    RecyclerView.Adapter<InternshipAdapter.InternshipViewHolder>() {

    private val companyCache = mutableMapOf<String, Pair<String?, String?>>() // companyId -> (name, imageUrl)

    inner class InternshipViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val companyImage: ImageView = itemView.findViewById(R.id.CompanyProfileImage)
        val title = itemView.findViewById<TextView>(R.id.JobTitle)
        val companyName: TextView = itemView.findViewById(R.id.companyname)
        val companyID: TextView = itemView.findViewById(R.id.companyId)
        val location = itemView.findViewById<TextView>(R.id.Location)
        val internshipType = itemView.findViewById<TextView>(R.id.internshipType)
        val internshipTime = itemView.findViewById<TextView>(R.id.internshipTime)
        val hidePost = itemView.findViewById<ImageView>(R.id.hidePost)
        val bookmark = itemView.findViewById<ImageView>(R.id.bookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternshipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.internship_item, parent, false)
        return InternshipViewHolder(view)
    }


    override fun onBindViewHolder(holder: InternshipViewHolder, position: Int) {
        val item = internship[position]
        holder.title.text = item.title
        holder.location.text = item.location
        holder.companyID.text = item.companyId
        holder.internshipType.text = item.internshipType
        holder.internshipTime.text = item.internshipTime

        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(item.companyId)
            .get()
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

                    companyCache[item.id] = Pair(companyName, profileImageUrl)
                } else {
                    holder.companyName.text = "Company not found"
                }
            }
            .addOnFailureListener {
                holder.companyName.text = "Error loading company"
            }

        // ✅ Click on entire item
        holder.itemView.setOnClickListener {
            val cachedData = companyCache[item.id]
            onItemClick(item, cachedData?.first, cachedData?.second)
        }

        // ✅ Click on hidePost icon
        holder.hidePost.setOnClickListener {
            val prefs = holder.itemView.context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val currentUserId = prefs.getString("userid", null)

            if (currentUserId != null) {
                db.collection("Users")
                    .document(currentUserId)
                    .update("hide_post", com.google.firebase.firestore.FieldValue.arrayUnion(item.id))
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Post hidden", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        holder.bookmark.setOnClickListener {
            val prefs = holder.itemView.context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val currentUserId = prefs.getString("userid", null)
            if (currentUserId != null) {
                db.collection("Users")
                    .document(currentUserId)
                    .update("bookmark", com.google.firebase.firestore.FieldValue.arrayUnion(item.id))
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Post bookmarked", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }



    override fun getItemCount(): Int = internship.size
}