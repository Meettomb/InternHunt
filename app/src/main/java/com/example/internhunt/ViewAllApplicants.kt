package com.example.internhunt

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ViewAllApplicants : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var applicantsContainer: LinearLayout
    private lateinit var applicantItemLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_all_applicants)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primary_color)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }


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

        val postId = intent.getStringExtra("postId").toString()
        getAllApplicants(postId)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        applicantsContainer = findViewById(R.id.applicantsContainer)
        applicantItemLayout = findViewById(R.id.applicantItemLayout)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getAllApplicants(postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("InternshipApplications")
            .whereEqualTo("postId", postId)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (doc in querySnapshot.documents) {
                        val userId = doc.getString("userId")
                        val cvPdf = doc.getString("pdfUrl")

                        if (userId != null) {
                            db.collection("Users").document(userId).get()
                                .addOnSuccessListener { userDoc ->
                                    if (userDoc != null && userDoc.exists()) {
                                        val userName = userDoc.getString("username")
                                        val userProfileImage = userDoc.getString("profile_image_url")

                                        val applicantItemView = layoutInflater.inflate(
                                            R.layout.all_applicants_list,
                                            applicantItemLayout,
                                            false
                                        )

                                        applicantItemView.findViewById<TextView>(R.id.applicantName).text = userName
                                        Glide.with(this)
                                            .load(userProfileImage)
                                            .into(applicantItemView.findViewById<ImageView>(R.id.UserProfileImage))

                                        applicantItemView.findViewById<ImageView>(R.id.downloadIcon).setOnClickListener {
                                            if (cvPdf != null) {
                                                try {
                                                    val request = DownloadManager.Request(Uri.parse(cvPdf))
                                                        .setTitle("Downloading CV")
                                                        .setDescription("Downloading applicant CV file...")
                                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                        .setAllowedOverMetered(true)
                                                        .setAllowedOverRoaming(true)
                                                        .setDestinationInExternalPublicDir(
                                                            Environment.DIRECTORY_DOWNLOADS, "Applicant_CV.pdf")

                                                    val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                                    downloadManager.enqueue(request)

                                                    Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    Toast.makeText(this, "Failed to start download", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(this, "No CV found", Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        applicantItemView.findViewById<LinearLayout>(R.id.viewApplicantProfile).setOnClickListener {
                                            val intent = Intent(this, ViewApplicantProfile::class.java)
                                            intent.putExtra("userId", userId)
                                            intent.putExtra("postId", postId)
                                            intent.putExtra("cvPdf", cvPdf)
                                            startActivity(intent)
                                        }

                                        applicantsContainer.addView(applicantItemView)
                                    } else {
                                        Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "No applicants found", Toast.LENGTH_LONG).show()
                }
            }
    }

}