package com.example.internhunt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class InternshipAdapter(private val internship: List<InternshipPostData>):
    RecyclerView.Adapter<InternshipAdapter.InternshipViewHolder>() {
    inner class InternshipViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val companyImage: ImageView = itemView.findViewById(R.id.CompanyProfileImage)
        val title = itemView.findViewById<TextView>(R.id.JobTitle)
        val companyName: TextView = itemView.findViewById(R.id.companyname)
        val companyID: TextView = itemView.findViewById(R.id.companyId)
        val location = itemView.findViewById<TextView>(R.id.Location)
        val internshipType = itemView.findViewById<TextView>(R.id.internshipType)
        val internshipTime = itemView.findViewById<TextView>(R.id.internshipTime)
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

        // Fetch company details based on companyId and populate companyName
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

                    // 💡 Now setup click listener to open detail
                    holder.itemView.setOnClickListener {
                        val context = holder.itemView.context
                        val intent = Intent(context, CompanyDetail::class.java)
                        intent.putExtra("title", item.title)
                        intent.putExtra("location", item.location)
                        intent.putExtra("companyId", item.companyId)
                        intent.putExtra("companyName", companyName)
                        intent.putExtra("internshipType", item.internshipType)
                        intent.putExtra("internshipTime", item.internshipTime)
                        intent.putExtra("profileImageUrl", profileImageUrl ?: "")
                        context.startActivity(intent)
                    }
                } else {
                    holder.companyName.text = "Company not found"
                }
            }
            .addOnFailureListener {
                holder.companyName.text = "Error loading company"
            }



    }

    override fun getItemCount(): Int = internship.size
}