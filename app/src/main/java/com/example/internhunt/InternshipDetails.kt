package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class InternshipDetails : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvCompany: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvTiming: TextView
    private lateinit var tvInternshipType: TextView
    private lateinit var tvOpening: TextView
    private lateinit var tvStipend: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDeadline: TextView
    private lateinit var btnApply: Button
    private lateinit var tvCompanyName: TextView
    private lateinit var tvCompanyEmail: TextView
    private lateinit var tvCompanyUrl: TextView
    private lateinit var imgCompanyLogo: ImageView

    private lateinit var skillsContainer: LinearLayout
    private lateinit var responsibilitiesContainer: LinearLayout
    private lateinit var perksContainer: LinearLayout
    private lateinit var degreeEligibilityContainer: LinearLayout

    private lateinit var bookmark: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_internship_details)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)

        // Check if session exists
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        // Bind Views
        tvTitle = findViewById(R.id.tvTitle)
        tvCompany = findViewById(R.id.tvCompany)
        tvLocation = findViewById(R.id.tvLocation)
        tvStipend = findViewById(R.id.tvStipend)
        tvDuration = findViewById(R.id.tvDuration)
        tvDescription = findViewById(R.id.tvDescription)
        tvTiming = findViewById(R.id.tvTiming)
        tvOpening = findViewById(R.id.tvOpening)
        tvInternshipType = findViewById(R.id.tvInternshipType)
        tvDeadline = findViewById(R.id.tvDeadline)
        btnApply = findViewById(R.id.btnApply)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvCompanyEmail = findViewById(R.id.tvCompanyEmail)
        tvCompanyUrl = findViewById(R.id.tvCompanyUrl)
        imgCompanyLogo = findViewById(R.id.imgCompanyLogo)

        skillsContainer = findViewById(R.id.skillsContainer)
        responsibilitiesContainer = findViewById(R.id.responsibilitiesContainer)
        perksContainer = findViewById(R.id.perksContainer)
        degreeEligibilityContainer = findViewById(R.id.degreeEligibility)
        bookmark = findViewById(R.id.bookmark)

        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        val internshipId = intent.getStringExtra("id") ?: ""

        if (internshipId.isNotEmpty()) {
            loadInternshipDetails(internshipId)
        }


        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("Users").document(userId)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val bookmarks = document.get("bookmark") as? List<String> ?: emptyList()
                        if (bookmarks.contains(internshipId)) {
                            bookmark.setImageResource(R.drawable.bookmark_fill)
                        } else {
                            bookmark.setImageResource(R.drawable.bookmark)
                        }
                    }
                }
        }


        bookmark.setOnClickListener {
            val postId = internshipId
            val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val userId = prefs.getString("userid", null)

            if (userId != null) {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("Users").document(userId)

                userRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val bookmarks = document.get("bookmark") as? List<String> ?: emptyList()

                            if (bookmarks.contains(postId)) {
                                // Remove bookmark
                                userRef.update("bookmark", FieldValue.arrayRemove(postId))
                                    .addOnSuccessListener {
                                        bookmark.setImageResource(R.drawable.bookmark) // change to empty icon
                                        Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Add bookmark
                                userRef.update("bookmark", FieldValue.arrayUnion(postId))
                                    .addOnSuccessListener {
                                        bookmark.setImageResource(R.drawable.bookmark_fill) // change to filled icon
                                        Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadInternshipDetails(id: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("internshipPostsData")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val internship = doc.toObject(InternshipPostData::class.java)
                    internship?.let {
                        tvTitle.text = it.title
                        tvCompany.text = "Company ID: ${it.companyId}"
                        tvLocation.text = "${it.location}"
                        tvStipend.text = "${it.stipend}"
                        tvDuration.text = "${it.duration}"
                        tvInternshipType.text = "${it.internshipType}"
                        tvTiming.text = "${it.internshipTime}"
                        tvOpening.text = "${it.openings}"
                        tvDescription.text = it.description
                        tvDeadline.text = "${it.applicationDeadline}"

                        // Skills
                        populateBulletList(skillsContainer, it.skillsRequired)

                        // Responsibilities — filter out empty/blank items before showing
                        val responsibilitiesFiltered = it.responsibilities.filter { item -> item.isNotBlank() }
                        if (responsibilitiesFiltered.isEmpty()) {
                            findViewById<View>(R.id.responsibilitiesContainer).visibility = View.GONE
                            findViewById<View>(R.id.ResponsibilitiesTextView).visibility = View.GONE
                        } else {
                            findViewById<View>(R.id.responsibilitiesContainer).visibility = View.VISIBLE
                            findViewById<View>(R.id.ResponsibilitiesTextView).visibility = View.VISIBLE
                            populateBulletList(findViewById(R.id.responsibilitiesContainer), responsibilitiesFiltered)
                        }

                        // Perks
                        populateBulletList(perksContainer, it.perks)
                        populateBulletList(degreeEligibilityContainer, it.degreeEligibility)

                        // Company details
                        loadCompanyDetails(it.companyId)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading details", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadCompanyDetails(companyId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users")
            .document(companyId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val company = doc.toObject(Users::class.java)
                    company?.let {
                        tvCompanyName.text = it.company_name
                        tvCompanyEmail.text = it.email
                        tvCompanyUrl.text = it.company_url

                        // Open website on click
                        val websiteUrl = it.company_url?.trim() ?: ""
                        if (websiteUrl.isEmpty()) {
                            tvCompanyUrl.visibility = View.GONE // Hide if no URL
                        } else {
                            tvCompanyUrl.setOnClickListener {
                                val finalUrl = if (websiteUrl.startsWith("http://") || websiteUrl.startsWith("https://")) {
                                    websiteUrl
                                } else {
                                    "http://$websiteUrl"
                                }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                                startActivity(intent)
                            }
                        }

                        Glide.with(this)
                            .load(it.profile_image_url)
                            .placeholder(android.R.drawable.ic_menu_report_image) // fallback
                            .transform(CircleCrop()) // makes it circular
                            .into(imgCompanyLogo)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading company details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addBookMark(userId: String, postId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)

        // Add the postId to the 'bookmark' array field
        userRef.update("bookmark", FieldValue.arrayUnion(postId))
            .addOnSuccessListener {
                Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun populateBulletList(container: LinearLayout, items: List<String>) {
        container.removeAllViews()
        items.forEach { item ->
            val tv = TextView(this)
            tv.text = "• $item"
            tv.setTextColor(ContextCompat.getColor(this, R.color.secondary_text))
            tv.textSize = 14f
            container.addView(tv)
        }
    }


}
