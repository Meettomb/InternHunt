package com.example.internhunt

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.Query


class CompanyDetail : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var companyLogo: ImageView
    private lateinit var companyName: TextView
    private lateinit var companyLocation: TextView
    private lateinit var companyPhone: TextView
    private lateinit var companyEmail: TextView
    private lateinit var companyWebsite: TextView
    private lateinit var companyDescription: TextView
    private lateinit var signupDate: TextView
    private lateinit var btnActiveJobs: TextView
    private lateinit var btnClosedJobs: TextView
    private lateinit var jobPostsRecyclerView: RecyclerView

    private lateinit var activeJobs: List<InternshipPostData>
    private lateinit var closedJobs: List<InternshipPostData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_company_detail)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Bind views
        backButton = findViewById(R.id.backButton)
        companyLogo = findViewById(R.id.CompanyLogo)
        companyName = findViewById(R.id.companyName)
        companyLocation = findViewById(R.id.companyLocation)
        companyPhone = findViewById(R.id.companyPhone)
        companyEmail = findViewById(R.id.companyEmail)
        companyWebsite = findViewById(R.id.companyWebsite)
        companyDescription = findViewById(R.id.companyDescription)
        signupDate = findViewById(R.id.signupDate)
        jobPostsRecyclerView = findViewById(R.id.recyclerViewJobs)

        jobPostsRecyclerView.layoutManager = LinearLayoutManager(this)
        btnActiveJobs = findViewById(R.id.btnOpenJobs)
        btnClosedJobs = findViewById(R.id.btnClosedJobs)



        // Get intent data
        val companyId = intent.getStringExtra("companyId")
        val companyNameStr = intent.getStringExtra("companyName")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        companyName.text = companyNameStr

        Glide.with(this)
            .load(profileImageUrl)
            .into(companyLogo)

        if (companyId != null) {
            loadCompanyDetail(companyId)
            loadJobPosts(companyId)
        } else {
            Toast.makeText(this, "Company ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }


        // Highlight Active, normal Closed
        btnActiveJobs.setOnClickListener {
            jobPostsRecyclerView.adapter = JobPostAdapter(activeJobs)


            btnActiveJobs.setBackgroundResource(R.drawable.rounded_button_higlight)
            btnClosedJobs.setBackgroundResource(R.drawable.rounded_button)
        }

        // Highlight Closed, normal Active
        btnClosedJobs.setOnClickListener {
            jobPostsRecyclerView.adapter = JobPostAdapter(closedJobs)

            btnActiveJobs.setBackgroundResource(R.drawable.rounded_button)
            btnClosedJobs.setBackgroundResource(R.drawable.rounded_button_higlight)
        }

        // Handle back
        backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadCompanyDetail(companyID: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(companyID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val companyNameValue = document.getString("company_name") ?: "N/A"
                    val profileImageUrl = document.getString("profile_image_url") ?: ""
                    val city = document.getString("city") ?: ""
                    val state = document.getString("state") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val email = document.getString("email") ?: ""
                    val website = document.getString("company_url") ?: ""
                    val description = document.getString("company_description") ?: ""
                    val signupDateRaw = document.getString("signup_date") ?: ""

                    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("d-MMMM-yyyy", Locale.getDefault())
                    val formattedDate = try {
                        val date = inputFormat.parse(signupDateRaw)
                        outputFormat.format(date!!)
                    } catch (e: Exception) {
                        signupDateRaw
                    }

                    companyName.text = companyNameValue
                    Glide.with(this)
                        .load(profileImageUrl)
                        .into(companyLogo)

                    companyLocation.text = "$city, $state"
                    companyPhone.text = phone
                    companyEmail.text = email
                    companyWebsite.text = website
                    companyDescription.text = description
                    signupDate.text = "Joined on $formattedDate"

                } else {
                    Toast.makeText(this, "Company not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading company", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadJobPosts(companyId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("internshipPostsData")
            .whereEqualTo("companyId", companyId)
            .orderBy("postedDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val activeList = mutableListOf<InternshipPostData>()
                val closedList = mutableListOf<InternshipPostData>()

                val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                val today = Calendar.getInstance().time

                for (doc in documents) {
                    val deadlineStr = doc.getString("applicationDeadline") ?: "N/A"

                    val job = InternshipPostData(
                        title = doc.getString("title") ?: "N/A",
                        internshipType = doc.getString("internshipType") ?: "N/A",
                        internshipTime = doc.getString("internshipTime") ?: "N/A",
                        stipend = doc.getString("stipend") ?: "N/A",
                        applicationDeadline = deadlineStr,
                        postedDate = doc.getTimestamp("postedDate")
                    )

                    // Decide active or closed
                    try {
                        if (deadlineStr != "N/A") {
                            val deadlineDate = sdf.parse(deadlineStr)
                            if (today.before(deadlineDate) || today == deadlineDate) {
                                activeList.add(job)
                            } else {
                                closedList.add(job)
                            }
                        } else {
                            closedList.add(job) // treat as closed if no deadline
                        }
                    } catch (e: Exception) {
                        closedList.add(job) // treat as closed if parsing fails
                    }
                }

                // Save lists
                activeJobs = activeList
                closedJobs = closedList

                // Default show active jobs
                jobPostsRecyclerView.adapter = JobPostAdapter(activeJobs)
            }
            .addOnFailureListener { e ->
                Log.e("JobPosts", "Error loading job posts", e)
                Toast.makeText(this, "Failed to load job posts: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}
