package com.example.internhunt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SearchResult : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InternshipAdapter
    private val internshipList = mutableListOf<InternshipPostData>()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvNoResults: TextView
    private lateinit var backBtn: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_result)

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        tvNoResults = findViewById(R.id.tvNoResults)
        recyclerView = findViewById(R.id.recyclerViewSearchResults)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = InternshipAdapter(internshipList) { post, companyName, imageUrl ->
            val intent = Intent(this, InternshipDetails::class.java)
            intent.putExtra("id", post.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val query = intent.getStringExtra("searchQuery") ?: ""
        if (query.isNotEmpty()) {
            searchFirestore(query)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun searchFirestore(searchText: String) {
        internshipList.clear()

        db.collection("internshipPostsData")
            .get()
            .addOnSuccessListener { internshipResult ->
                val internshipsTemp = mutableListOf<InternshipPostData>()

                for (doc in internshipResult) {
                    val post = doc.toObject(InternshipPostData::class.java)
                        .copy(id = doc.id)

                    // ✅ Get degreeEligibility array
                    val degreeEligibility = doc.get("degreeEligibility") as? List<String> ?: emptyList()

                    if (
                        post.title.contains(searchText, ignoreCase = true) ||
                        post.skillsRequired.any { it.contains(searchText, ignoreCase = true) } ||
                        post.internshipType.equals(searchText, ignoreCase = true) ||
                        post.internshipTime.equals(searchText, ignoreCase = true) ||
                        degreeEligibility.any { it.contains(searchText, ignoreCase = true) } // ✅ Degree match
                    ) {
                        internshipsTemp.add(post)
                    }
                }

                // Search by company_name in Users
                db.collection("Users")
                    .get()
                    .addOnSuccessListener { usersResult ->
                        val companyIdMatches = mutableSetOf<String>()

                        for (doc in usersResult) {
                            val companyName = doc.getString("company_name") ?: ""
                            if (companyName.contains(searchText, ignoreCase = true)) {
                                companyIdMatches.add(doc.id)
                            }
                        }

                        // Add internships that match company name
                        for (doc in internshipResult) {
                            val post = doc.toObject(InternshipPostData::class.java)
                                .copy(id = doc.id)

                            if (companyIdMatches.contains(post.companyId)) {
                                internshipsTemp.add(post)
                            }
                        }

                        // Avoid duplicates
                        internshipList.clear()
                        internshipList.addAll(internshipsTemp.distinctBy { it.id })

                        // ✅ Update UI visibility
                        if (internshipList.isEmpty()) {
                            tvNoResults.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            tvNoResults.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }

                        adapter.notifyDataSetChanged()
                    }
            }
    }



}

