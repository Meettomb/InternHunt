package com.example.internhunt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UsersAdapter(private val users: MutableList<Users>) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val companyImage: ImageView = itemView.findViewById(R.id.CompanyLogo)
        val companyName: TextView = itemView.findViewById(R.id.CompanyName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.company_list_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = users[position]
        holder.companyName.text = item.company_name
        Glide.with(holder.itemView.context)
            .load(item.profile_image_url)
            .into(holder.companyImage)

    }

    override fun getItemCount(): Int = users.size
}