package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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
    private var companyId: String? = null
    private lateinit var applyLayout: ConstraintLayout
    private lateinit var innerLayout: LinearLayout
    private lateinit var innerLayoutGradLine: TextView
    private lateinit var btnUploadPdf : Button
    private lateinit var tvApply : TextView
    private lateinit var btnUploadPdfError2 : TextView

    private val PICK_PDF_REQUEST = 1
    private var selectedPdfUri: Uri? = null
    private lateinit var progressBar: ProgressBar



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

        applyLayout = findViewById(R.id.applyConstraintLayout)
        innerLayout = applyLayout.findViewById(R.id.innerLayout)
        innerLayoutGradLine = applyLayout.findViewById(R.id.innerLayoutGradLine)
        btnUploadPdf = applyLayout.findViewById(R.id.btnUploadPdf)
        tvApply = applyLayout.findViewById(R.id.tvApply)
        btnUploadPdfError2 = applyLayout.findViewById(R.id.btnUploadPdfError)

        progressBar = findViewById(R.id.progressBar)

        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val internshipId = intent.getStringExtra("id") ?: ""
        val db = FirebaseFirestore.getInstance()
        db.collection("internshipPostsData")
            .document(internshipId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    companyId = document.getString("companyId")
                }
            }


        if (internshipId.isNotEmpty()) {
            loadInternshipDetails(internshipId)

            btnApply.setOnClickListener {
                applyLayout.visibility = View.VISIBLE
            }


            btnUploadPdf.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST)
            }
            // Apply button click validation
            tvApply.setOnClickListener {
                var isFormValid = true
                if (selectedPdfUri == null) {
                    btnUploadPdfError2.text = "Please select a PDF file"
                    btnUploadPdfError2.visibility = View.VISIBLE
                    isFormValid = false
                } else {
                    btnUploadPdfError2.visibility = View.GONE
                }

                if (isFormValid) {
                    uploadPdfAndApply(userId, internshipId, companyId ?: "")
                }
            }
        }
        applyLayout.setOnClickListener {
            applyLayout.visibility = View.GONE
        }
        innerLayout.setOnClickListener(null)
        innerLayoutGradLine.setOnClickListener{
            applyLayout.visibility = View.GONE
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


    private fun uploadPdfAndApply(userId: String, intershipId: String, companyId: String) {
        if (selectedPdfUri == null) {
            Toast.makeText(this, "Please select a PDF first", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("InternshipApplications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("postId", intershipId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "You have already applied for this internship", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val storageRef = FirebaseStorage.getInstance().reference
                val pdfRef = storageRef.child("applications/${System.currentTimeMillis()}.pdf")

                pdfRef.putFile(selectedPdfUri!!)
                    .addOnSuccessListener {
                        pdfRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            applyForInternship(userId, intershipId, companyId, downloadUri.toString())
                        }
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to upload PDF", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error checking application: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyForInternship(userId: String, intershipId: String, companyId: String, pdfUrl: String) {
        Log.d("TAG", "UserId: $userId, PostId: $intershipId, CompanyId: $companyId")
        val db = FirebaseFirestore.getInstance()
        val newCollectionRef = db.collection("InternshipApplications").document()

        val data = hashMapOf(
            "id" to newCollectionRef.id,
            "userId" to userId,
            "postId" to intershipId,
            "companyId" to companyId,
            "pdfUrl" to pdfUrl, // ✅ Now storing the cloud link
            "applyDate" to Timestamp.now()
        )

        newCollectionRef.set(data)
            .addOnSuccessListener {
                applyLayout.visibility = View.GONE
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Application submitted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                applyLayout.visibility = View.GONE
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error submitting application", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedPdfUri = data.data

            // Get file name
            val cursor = contentResolver.query(selectedPdfUri!!, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val fileName = nameIndex?.let { cursor.getString(it) } ?: "Selected PDF"
            cursor?.close()

            // Show file name
            val tvFileName = findViewById<TextView>(R.id.pdfFileName)
            tvFileName.text = fileName
            tvFileName.visibility = View.VISIBLE

            // Hide error message
            val tvError = findViewById<TextView>(R.id.btnUploadPdfError)
            tvError.visibility = View.GONE
        }
    }
}

