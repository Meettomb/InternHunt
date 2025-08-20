package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.w3c.dom.Text
import java.io.LineNumberReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.Query


class CompanyHomePage : AppCompatActivity() {
    private var popupWindow: PopupWindow? = null
    private lateinit var UserProfileImage: ImageView
    private lateinit var company_name: TextView
    private lateinit var post_new_internship: LinearLayout
    private lateinit var topFivePost_container: LinearLayout
    private lateinit var activeJobPostViewAllBtn: TextView
    private lateinit var TopFiveActiveInternshipPost: LinearLayout
    private val loadedInternshipIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContentView(R.layout.activity_company_home_page)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.card_background)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        val headerProfile: View = findViewById(R.id.header_profile)
        UserProfileImage = findViewById(R.id.UserProfileImage)
        company_name = findViewById(R.id.company_name)
        post_new_internship = findViewById(R.id.post_new_internship)
        topFivePost_container = findViewById(R.id.topFivePost_container)
        activeJobPostViewAllBtn = findViewById(R.id.activeJobPostViewAllBtn)
        TopFiveActiveInternshipPost = findViewById(R.id.TopFiveActiveInternshipPost)

        // Get session
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        val role = prefs.getString("role", null)?.trim()?.lowercase()

        // Check if session exists
        if (userId == null || role == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        when (role) {
            "company" -> {
                if (this !is CompanyHomePage) { // Prevent reopening the same page
                    startActivity(Intent(this, CompanyHomePage::class.java))
                    finish()
                }
            }

            "student" -> {
                if (this !is Home) { // Prevent reopening same page
                    startActivity(Intent(this, Home::class.java))
                    finish()
                }
            }

            else -> {
                Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }

        if (userId != null) {
            lodeCompanyDetail(userId)
            getTotalInternshipCount(userId)
            getActiveInternshipCount(userId)
            getTopFiveActiveInternshipPost(userId)
        }
        saveFcmToken(userId)
        headerProfile.setOnClickListener {
            if (popupWindow != null && popupWindow!!.isShowing) {
                popupWindow!!.dismiss()
            } else {
                showProfileMenu(it)
            }
        }

        post_new_internship.setOnClickListener {
            startActivity(Intent(this, JobPost::class.java))
        }

        activeJobPostViewAllBtn.setOnClickListener {
            val intent = Intent(this, AllActivePostedInternship::class.java)
            startActivity(intent)
            finish()
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun lodeCompanyDetail(userId: String) {
        var db = FirebaseFirestore.getInstance()
        var userRef = db.collection("Users").document(userId)
        userRef.addSnapshotListener { doc, error ->
            if (error != null) {
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                var companyName = doc.getString("company_name")
                var profileImage = doc.getString("profile_image_url")

                if (companyName != null) {
                    company_name.text = "$companyName!"
                }
                if (profileImage != null) {
                    Glide.with(this)
                        .load(profileImage)
                        .into(UserProfileImage)
                }
            }
        }
    }

    private fun showProfileMenu(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.profile_dropdown, null)
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        val role = prefs.getString("role", null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true // Allow outside touch dismiss
        ).apply {
            isOutsideTouchable = true
            showAsDropDown(anchor, -150, 10) // Position adjust
        }

        // Menu item clicks
        popupView.findViewById<View>(R.id.manageProfile).setOnClickListener {
            // Handle Manage Profile
            popupWindow?.dismiss()
        }
        popupView.findViewById<View>(R.id.home).setOnClickListener {
            var intent = Intent(this, CompanyHomePage::class.java)
            startActivity(intent)
            finish()
            popupWindow?.dismiss()
        }

        popupView.findViewById<View>(R.id.settings).setOnClickListener {
            // Handle Settings
            popupWindow?.dismiss()
        }

        popupView.findViewById<View>(R.id.logout).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun getTotalInternshipCount(userId: String) {
        var db = FirebaseFirestore.getInstance()
        db.collection("internshipPostsData").whereEqualTo("companyId", userId)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Fail to get Total Internship", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    var totalInternship = querySnapshot.size()
                    var TotalInternship = findViewById<TextView>(R.id.TotalInternship)
                    TotalInternship.text = totalInternship.toString()
                }
            }

    }

    private fun getActiveInternshipCount(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val formatter = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
        val now = Date()

        db.collection("internshipPostsData")
            .whereEqualTo("companyId", userId)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Fail to get Active Internship", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val activeInternships = querySnapshot.documents.filter { doc ->
                        val deadlineStr = doc.getString("applicationDeadline")
                        val deadlineDate = deadlineStr?.let { formatter.parse(it) }
                        deadlineDate != null && deadlineDate.after(now)
                    }

                    val totalActive = activeInternships.size
                    findViewById<TextView>(R.id.ActiveInternship).text = totalActive.toString()
                }
            }
    }

    private fun getTopFiveActiveInternshipPost(userId: String) {
        val db = FirebaseFirestore.getInstance()


        db.collection("internshipPostsData")
            .whereEqualTo("companyId", userId)
            .orderBy("postedDate", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("InternshipDetail", "Error fetching internships", error)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    // Sort by deadline if needed, then take top 5
                    val formatter = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
                    val now = Date()

                    val activePosts = querySnapshot.documents
                        .filter { doc ->
                            val deadlineStr = doc.getString("applicationDeadline")
                            val deadlineDate = deadlineStr?.let { formatter.parse(it) }
                            deadlineDate != null && deadlineDate.after(now)
                        }
                        .take(5)

                    TopFiveActiveInternshipPost.removeAllViews()
                    loadedInternshipIds.clear()

                    for (doc in activePosts) {
                        val title = doc.getString("title")
                        val internshipType = doc.getString("internshipType")
                        val internshipTime = doc.getString("internshipTime")
                        val stipend = doc.getString("stipend")
                        val deadline = doc.getString("applicationDeadline")

                        Log.d(
                            "InternshipDetail",
                            "Title: $title, Type: $internshipType, Time: $internshipTime, Stipend: $stipend, Deadline: $deadline"
                        )

                       val activeInternshipView = layoutInflater.inflate(
                           R.layout.job_post_item,
                           TopFiveActiveInternshipPost,
                           false)


                        activeInternshipView.findViewById<TextView>(R.id.JobTitle).text = title
                        activeInternshipView.findViewById<TextView>(R.id.internshipType).text = internshipType
                        activeInternshipView.findViewById<TextView>(R.id.internshipTime).text = internshipTime
                        activeInternshipView.findViewById<TextView>(R.id.Stipend).text = stipend
                        activeInternshipView.findViewById<TextView>(R.id.Deadline).text = deadline



                        activeInternshipView.setOnClickListener {
                            var intent = Intent(this, InternshipDetails::class.java)
                            intent.putExtra("id", doc.id)
                            startActivity(intent)
                        }

                        var edit_delete_buttons = activeInternshipView.findViewById<LinearLayout>(R.id.edit_delete_buttons)
                        edit_delete_buttons.visibility = View.VISIBLE

                        activeInternshipView.findViewById<TextView>(R.id.btnViewApplicants).setOnClickListener {
                            Toast.makeText(this, "View Applicants (${doc.id})", Toast.LENGTH_SHORT).show()
                        }

                        activeInternshipView.findViewById<TextView>(R.id.btnEdit).setOnClickListener {
                            var intent = Intent(this, EditInternship::class.java)
                            intent.putExtra("id", doc.id)
                            startActivity(intent)
                        }

                        activeInternshipView.findViewById<TextView>(R.id.btnDelete).setOnClickListener {
                            val db = FirebaseFirestore.getInstance()
                            val todayStr = SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).format(Date())

                            // Update the document
                            db.collection("internshipPostsData").document(doc.id)
                                .update(
                                    mapOf(
                                        "status" to false,
                                        "applicationDeadline" to todayStr
                                    )
                                )
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Internship marked as inactive", Toast.LENGTH_SHORT).show()
                                    // Optionally remove the view from UI
                                    TopFiveActiveInternshipPost.removeView(activeInternshipView)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update internship: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }


                        TopFiveActiveInternshipPost.addView(activeInternshipView)


                    }
                } else {
                    Log.d("InternshipDetail", "No active internships found")
                    TopFiveActiveInternshipPost.removeAllViews()
                    loadedInternshipIds.clear()
                }
            }

    }
    private fun saveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                val token = task.result
                val db = FirebaseFirestore.getInstance()

                val data = hashMapOf("fcmToken" to token)

                db.collection("Users").document(userId)
                    .set(data, SetOptions.merge())  // 👈 create if not exist, update if exist
                    .addOnSuccessListener {
                        Log.d("FCM", "Token saved: $token")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Failed to save token", e)
                    }
            }
    }
}