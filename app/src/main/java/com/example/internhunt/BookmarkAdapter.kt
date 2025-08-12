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


class BookmarkAdapter(private val internship: List<InternshipPostData>,
                        private val onItemClick: (InternshipPostData, String?, String?) -> Unit ):
    RecyclerView.Adapter<BookmarkAdapter.InternshipViewHolder>() {

    private val companyCache = mutableMapOf<String, Pair<String?, String?>>() // companyId -> (name, imageUrl)

    inner class InternshipViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val companyImage: ImageView = itemView.findViewById(R.id.CompanyProfileImage)
        val title = itemView.findViewById<TextView>(R.id.JobTitle)
        val companyName: TextView = itemView.findViewById(R.id.companyname)
        val companyID: TextView = itemView.findViewById(R.id.companyId)
        val location = itemView.findViewById<TextView>(R.id.Location)
        val internshipType = itemView.findViewById<TextView>(R.id.internshipType)
        val internshipTime = itemView.findViewById<TextView>(R.id.internshipTime)
        val bookmark = itemView.findViewById<ImageView>(R.id.bookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternshipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bookmark_item, parent, false)
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

        //  Click on entire item
        holder.itemView.setOnClickListener {
            val cachedData = companyCache[item.id]
            onItemClick(item, cachedData?.first, cachedData?.second)
        }


        // Inside onBindViewHolder, after setting other fields
        val prefs = holder.itemView.context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("userid", null)

        if (currentUserId != null) {
            val userRef = db.collection("Users").document(currentUserId)

            //  Check bookmark status for this internship and update icon
            userRef.get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val bookmarks = doc.get("bookmark") as? List<String> ?: emptyList()
                        if (bookmarks.contains(item.id)) {
                            holder.bookmark.setImageResource(R.drawable.bookmark_fill)
                        } else {
                            holder.bookmark.setImageResource(R.drawable.bookmark)
                        }
                    }
                }

            //  Toggle on click
            holder.bookmark.setOnClickListener {
                userRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val bookmarks = doc.get("bookmark") as? List<String> ?: emptyList()

                        if (bookmarks.contains(item.id)) {
                            // Remove
                            userRef.update("bookmark", com.google.firebase.firestore.FieldValue.arrayRemove(item.id))
                                .addOnSuccessListener {
                                    holder.bookmark.setImageResource(R.drawable.bookmark)
                                    Toast.makeText(holder.itemView.context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Add
                            userRef.update("bookmark", com.google.firebase.firestore.FieldValue.arrayUnion(item.id))
                                .addOnSuccessListener {
                                    holder.bookmark.setImageResource(R.drawable.bookmark_fill)
                                    Toast.makeText(holder.itemView.context, "Bookmark added", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
            }
        }

    }



    override fun getItemCount(): Int = internship.size
}