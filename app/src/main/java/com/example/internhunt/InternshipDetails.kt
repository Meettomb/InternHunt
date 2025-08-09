package com.example.internhunt

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
import com.google.firebase.firestore.FirebaseFirestore

class InternshipDetails : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvCompany: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvStipend: TextView
    private lateinit var tvDuration: TextView
//    private lateinit var tvSkills: TextView
//    private lateinit var tvResponsibilities: TextView
    private lateinit var tvDescription: TextView
//    private lateinit var tvPerks: TextView
    private lateinit var tvDeadline: TextView
    private lateinit var btnApply: Button
    private lateinit var tvCompanyName: TextView
    private lateinit var tvCompanyEmail: TextView
    private lateinit var tvCompanyUrl: TextView
    private lateinit var imgCompanyLogo: ImageView

    private lateinit var skillsContainer: LinearLayout
    private lateinit var responsibilitiesContainer: LinearLayout
    private lateinit var perksContainer: LinearLayout



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

        // Bind Views
        tvTitle = findViewById(R.id.tvTitle)
        tvCompany = findViewById(R.id.tvCompany)
        tvLocation = findViewById(R.id.tvLocation)
        tvStipend = findViewById(R.id.tvStipend)
        tvDuration = findViewById(R.id.tvDuration)
//        tvSkills = findViewById(R.id.skillsContainer)
//        tvResponsibilities = findViewById(R.id.responsibilitiesContainer)
        tvDescription = findViewById(R.id.tvDescription)
//        tvPerks = findViewById(R.id.perksContainer)
        tvDeadline = findViewById(R.id.tvDeadline)
        btnApply = findViewById(R.id.btnApply)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvCompanyEmail = findViewById(R.id.tvCompanyEmail)
        tvCompanyUrl = findViewById(R.id.tvCompanyUrl)
        imgCompanyLogo = findViewById(R.id.imgCompanyLogo)

        skillsContainer = findViewById(R.id.skillsContainer)
        responsibilitiesContainer = findViewById(R.id.responsibilitiesContainer)
        perksContainer = findViewById(R.id.perksContainer)

        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        val internshipId = intent.getStringExtra("id") ?: ""



        if (internshipId.isNotEmpty()) {
            loadInternshipDetails(internshipId)
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


    private fun populateBulletList(container: LinearLayout, items: List<String>) {
        container.removeAllViews()
        items.forEach { item ->
            val tv = TextView(this)
            tv.text = "• $item"
            tv.setTextColor(ContextCompat.getColor(this, R.color.light_color))
            tv.textSize = 14f
            container.addView(tv)
        }
    }


}