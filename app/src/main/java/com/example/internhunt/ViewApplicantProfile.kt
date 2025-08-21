package com.example.internhunt

import InputFilterMinMax
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import org.checkerframework.checker.units.qual.Length
import org.w3c.dom.Text
import kotlin.concurrent.thread


class ViewApplicantProfile : AppCompatActivity() {

    private lateinit var userProfileImage2: ImageView
    private lateinit var username: TextView
    private lateinit var headline: TextView
    private lateinit var Location: TextView
    private lateinit var backButton: ImageView



    private lateinit var skillsRecyclerView: RecyclerView
    private lateinit var skillsAdapter: SkillsAdapter
    private val skillsList = mutableListOf<String>()

    private lateinit var educationContainer: LinearLayout

    private lateinit var projectLinearLayout: LinearLayout
    private var profileListener: ListenerRegistration? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var ViewCV: TextView
    private lateinit var downloadCV: ImageView
    private lateinit var btnHire: Button
    private lateinit var btnCancelHire: Button





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContentView(R.layout.activity_view_applicant_profile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        val UserId = intent.getStringExtra("userId").toString()
        val postId = intent.getStringExtra("postId").toString()
        val cvPdf = intent.getStringExtra("cvPdf")



        if (UserId != null){
            loadUserProfile(UserId)
        }
        else{
            Toast.makeText(this, "Please Login Again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        userProfileImage2 = findViewById(R.id.UserProfileImage2)
        username = findViewById(R.id.username)
        headline = findViewById(R.id.headline)
        Location = findViewById(R.id.Location)
        progressBar = findViewById(R.id.progressBar)
        ViewCV = findViewById(R.id.ViewCV)
        downloadCV = findViewById(R.id.downloadCV)
        btnHire = findViewById(R.id.btnHire)
        btnCancelHire = findViewById(R.id.btnCancelHire)

        backButton = findViewById(R.id.backButton)

        ViewCV.setOnClickListener {
            if (cvPdf != null) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(cvPdf), "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
        downloadCV.setOnClickListener {
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

        skillsRecyclerView = findViewById(R.id.SkillrecyclerView)
        skillsRecyclerView.layoutManager = LinearLayoutManager(this)
        skillsAdapter = SkillsAdapter(this, skillsList) { position, skill ->
            Toast.makeText(this, "Edit skill: $skill", Toast.LENGTH_SHORT).show()
        }
        skillsRecyclerView.adapter = skillsAdapter

        educationContainer = findViewById(R.id.edu_layout)

        projectLinearLayout = findViewById(R.id.projectLinearLayout)

        // Back Button
        backButton.setOnClickListener {
            finish()
        }

        val db = FirebaseFirestore.getInstance()
        val postRef = db.collection("internshipPostsData").document(postId)

        postRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val hiredList = doc.get("hiredApplicants") as? List<String> ?: emptyList()

                if (hiredList.contains(UserId)) {
                    btnHire.visibility = View.GONE
                    btnCancelHire.visibility = View.VISIBLE
                } else {
                    btnHire.visibility = View.VISIBLE
                    btnCancelHire.visibility = View.GONE
                }
            }
        }

        btnHire.setOnClickListener {
            sendHireEmail(UserId, postId)
        }

        btnCancelHire.setOnClickListener {
            cancelHire(UserId, postId)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun loadUserProfile(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (doc != null && doc.exists()) {

                    val imageUrl = doc.getString("profile_image_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        if (!isDestroyed && !isFinishing) {
                            Glide.with(this)
                                .load(imageUrl)
                                .into(findViewById(R.id.UserProfileImage2))
                        }
                    }

                    val email = doc.getString("email")
                    val phone = doc.getString("phone")
                    val emailView = findViewById<TextView>(R.id.email)
                    val phoneView = findViewById<TextView>(R.id.phone)
                    emailView.text = email
                    phoneView.text = phone

                    val CoverimageUrl = doc.getString("background_cover_url")
                    val coverImageView = findViewById<ImageView>(R.id.coverImageView)
                    if (!CoverimageUrl.isNullOrEmpty()) {
                        if (!isDestroyed && !isFinishing) {
                            Glide.with(this)
                                .load(CoverimageUrl)
                                .into(coverImageView)
                        }
                    }


                    val userName = doc.getString("username")
                    val companyName = doc.getString("company_name")
                    val role = doc.getString("role")

                    val username = findViewById<TextView>(R.id.username)

                    if (role == "Student") {
                        if (!userName.isNullOrEmpty()) {
                            username.text = userName
                        }

                        loadUserSkills(userId)
                        loadEducation(userId)
                        loadProject(userId)

                        val projectLinearLayout = findViewById<LinearLayout>(R.id.projectLinearLayout)

                        val skillList = doc.get("skill") as? List<String> ?: emptyList()
                        val projectList = doc.get("projects") as? List<Map<String, Any>> ?: emptyList()

                        if (projectList.isEmpty()) {
                            projectLinearLayout.visibility = View.GONE
                        }


                        val skillLists = doc.get("skill") as? List<String> ?: emptyList()
                        if (skillLists.isEmpty()) {
                            findViewById<LinearLayout>(R.id.SkillSection).visibility = View.GONE
                        }
                    } else if (role == "Company") {
                        if (!companyName.isNullOrEmpty()) {
                            username.text = companyName
                        }

                        findViewById<LinearLayout>(R.id.SectionAddSection).visibility = View.GONE
                        findViewById<LinearLayout>(R.id.SkillSection).visibility = View.GONE
                    } else {
                        username.text = "Guest"
                    }

                    val state = doc.getString("state") ?: ""
                    val city = doc.getString("city") ?: ""
                    val locationView = findViewById<TextView>(R.id.Location)
                    locationView.text = "$city, $state".trim().trimStart(',')


                    val headlineStr = doc.getString("headline") ?: ""
                    headline.text = if (headlineStr.isNotEmpty()) headlineStr else "Add Headline"

                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun loadUserSkills(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val skills = document.get("skill") as? List<String> ?: emptyList()

                    skillsList.clear()
                    skillsList.addAll(skills)

                    skillsRecyclerView.visibility = View.VISIBLE
                    skillsAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No skills found", Toast.LENGTH_SHORT).show()
                    skillsRecyclerView.visibility = View.GONE

                }
            }
            .addOnFailureListener { e ->
                Log.e("loadUserSkills", "Error loading skills: ${e.message}")
                Toast.makeText(this, "Failed to load skills: ${e.message}", Toast.LENGTH_SHORT).show()
                skillsRecyclerView.visibility = View.GONE
            }
    }
    private fun loadEducation(userId: String) {
        val db = FirebaseFirestore.getInstance()
        educationContainer = findViewById(R.id.edu_layout)

        // Clear old views before adding new ones
        educationContainer.removeAllViews()

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get the education list from the user document
                    val educationList = document.get("education") as? List<Map<String, Any>>

                    if (educationList != null) {
                        for ((index, eduMap) in educationList.withIndex()) {
                            val education = EducationEntry(
                                collage_name = eduMap["collage_name"] as? String ?: "N/A",
                                degree_name = eduMap["degree_name"] as? String ?: "N/A",
                                graduation_start_year = eduMap["graduation_start_year"] as? String ?: "N/A",
                                graduation_end_year = eduMap["graduation_end_year"] as? String ?: "N/A"
                            )

                            val educationView = layoutInflater.inflate(
                                R.layout.education_list, educationContainer, false
                            )

                            educationView.findViewById<TextView>(R.id.collageName).text = education.collage_name
                            educationView.findViewById<TextView>(R.id.degreeName).text = education.degree_name
                            educationView.findViewById<TextView>(R.id.academicYear).text =
                                "${education.graduation_start_year} - ${education.graduation_end_year}"

                            var editIcon = educationView.findViewById<ImageView>(R.id.editIcon)
                            editIcon.visibility = View.GONE

                            educationContainer.addView(educationView)
                        }
                    } else {
                        Toast.makeText(this, "No education data found", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Education", "Error loading education", e)
                Toast.makeText(this, "Failed to load education: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    fun formatMonthYear(input: String): String {
        return try {
            val parts = input.split("-")
            if (parts.size == 2) {
                val month = parts[0].toInt()
                val year = parts[1]
                val monthName = java.text.DateFormatSymbols().months[month - 1]
                "$monthName $year"
            } else {
                input // fallback if format is wrong
            }
        } catch (e: Exception) {
            input
        }
    }
    private fun loadProject(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val projectLayout = findViewById<LinearLayout>(R.id.project_layout)
        projectLayout.removeAllViews() // Clear previous projects

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                val projectList = document.get("projects") as? List<Map<String, Any>> ?: emptyList()
                if (projectList.isNotEmpty()) {
                    Log.d("loadProject", "Projects from Firestore: $projectList")

                    for ((index, pMap) in projectList.withIndex()) {
                        val project = ProjectsEntry(
                            title = pMap["title"] as? String ?: "N/A",
                            description = pMap["description"] as? String ?: "N/A",
                            end_date = pMap["end_date"] as? String ?: "N/A",
                            link = pMap["link"] as? String ?: "N/A",
                            startDate = pMap["startDate"] as? String ?: "N/A",
                            technologies = pMap["technologies"] as? List<String> ?: emptyList(),
                        )

                        // Inflate new project view
                        val projectView = layoutInflater.inflate(
                            R.layout.project_list,
                            projectLayout, // attach to the correct layout
                            false
                        )

                        // Set project data
                        projectView.findViewById<TextView>(R.id.projectTitle).text = project.title
                        projectView.findViewById<TextView>(R.id.projectDescription).text = project.description
                        projectView.findViewById<TextView>(R.id.projectTechnologiesSkill).text =
                            project.technologies.joinToString(", ")

                        val start = formatMonthYear(project.startDate)
                        val end = formatMonthYear(project.end_date)
                        projectView.findViewById<TextView>(R.id.projectDuration).text = "$start - $end"

                        // Handle project link
                        val linkContainer = projectView.findViewById<LinearLayout>(R.id.linkContainer)
                        if (project.link.isNotEmpty()) {
                            val linkTextView = projectView.findViewById<TextView>(R.id.projectLink)
                            linkTextView.text = project.link
                            linkTextView.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(project.link))
                                it.context.startActivity(intent)
                            }
                            linkContainer.visibility = View.VISIBLE
                        } else {
                            linkContainer.visibility = View.GONE
                        }

                        // Edit project
                        var editIcon = projectView.findViewById<ImageView>(R.id.editIcon)
                        editIcon.visibility = View.GONE
                        // Add view to layout
                        projectLayout.addView(projectView)
                    }
                } else {
                    Log.d("loadProject", "No projects found in document.")
                }
            }
    }



    private fun sendHireEmail(userId: String, postId: String) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        val postRef = db.collection("internshipPostsData").document(postId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val hiredList = snapshot.get("hiredApplicants") as? MutableList<String> ?: mutableListOf()
            if (!hiredList.contains(userId)) {
                hiredList.add(userId)
                transaction.update(postRef, "hiredApplicants", hiredList)
            } else {
                throw Exception("User already hired for this post")
            }
        }.addOnSuccessListener {
            sendHireEmailToUser(userId, postId)
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to hire user", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun sendHireEmailToUser(userId: String, postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId).get().addOnSuccessListener { userDoc ->
            val userEmail = userDoc.getString("email")
            val userName = userDoc.getString("username")
            if (userEmail.isNullOrBlank()) {
                progressBar.visibility = View.GONE
                return@addOnSuccessListener
            }

            db.collection("internshipPostsData").document(postId).get()
                .addOnSuccessListener { postDoc ->
                    val title = postDoc.getString("title")
                    val companyId = postDoc.getString("companyId")
                    if (companyId.isNullOrBlank()) return@addOnSuccessListener

                    db.collection("Users").document(companyId).get()
                        .addOnSuccessListener { companyDoc ->
                            val companyName = companyDoc.getString("company_name")
                            val companyLink = companyDoc.getString("company_url")

                            thread {
                                try {
                                    val sender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                                    sender.sendEmail(
                                        toEmail = userEmail,
                                        subject = "Congratulations! You’re Hired at $companyName 🎉",
                                        body = """
                                    Hello $userName,
                                    
                                    Congratulations! You have been selected for the internship "$title" 
                                    at $companyName.
                                    
                                    Company Website: $companyLink
                                    
                                    The company will contact you soon with further details.
                                    
                                    Best wishes,
                                    InternHunt Team
                                """.trimIndent()
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    progressBar.post {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(this, "Hire successful and email sent!", Toast.LENGTH_SHORT).show()
                                        btnHire.visibility = View.GONE
                                        btnCancelHire.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                }
        }
    }

    private fun cancelHire(userId: String, postId: String) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        val postRef = db.collection("internshipPostsData").document(postId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val hiredList = snapshot.get("hiredApplicants") as? MutableList<String> ?: mutableListOf()
            if (hiredList.contains(userId)) {
                hiredList.remove(userId)
                transaction.update(postRef, "hiredApplicants", hiredList)
            } else {
                throw Exception("User not found in hired list")
            }
        }.addOnSuccessListener {
            sendCancelEmailToUser(userId, postId)
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to cancel hire", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun sendCancelEmailToUser(userId: String, postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId).get().addOnSuccessListener { userDoc ->
            val userEmail = userDoc.getString("email")
            val userName = userDoc.getString("username")
            if (userEmail.isNullOrBlank()) {
                progressBar.visibility = View.GONE
                return@addOnSuccessListener
            }

            db.collection("internshipPostsData").document(postId).get()
                .addOnSuccessListener { postDoc ->
                    val title = postDoc.getString("title")
                    val companyId = postDoc.getString("companyId") ?: return@addOnSuccessListener

                    db.collection("Users").document(companyId).get()
                        .addOnSuccessListener { companyDoc ->
                            val companyName = companyDoc.getString("company_name")
                            val companyLink = companyDoc.getString("company_url")

                            thread {
                                try {
                                    val sender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                                    sender.sendEmail(
                                        toEmail = userEmail,
                                        subject = "Update: Your Internship at $companyName ❌",
                                        body = """
                                    Hello $userName,
                                    
                                    We regret to inform you that your selection for the internship "$title" 
                                    at $companyName has been cancelled.
                                    
                                    Company Website: $companyLink
                                    
                                    We encourage you to apply for other opportunities.
                                    
                                    Best regards,  
                                    InternHunt Team
                                """.trimIndent()
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    progressBar.post {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(this, "Hire cancelled successfully!", Toast.LENGTH_SHORT).show()
                                        btnCancelHire.visibility = View.GONE
                                        btnHire.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        profileListener?.remove() // stop listening when activity is gone
    }



}