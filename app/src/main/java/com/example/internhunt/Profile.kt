package com.example.internhunt

import InputFilterMinMax
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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

class Profile : AppCompatActivity() {

    private lateinit var userProfileImage2: ImageView
    private lateinit var username: TextView
    private lateinit var headline: TextView
    private lateinit var Location: TextView
    private lateinit var usernameEdit: EditText
    private lateinit var uploadOverlay: FrameLayout
    private lateinit var uploadSection: LinearLayout
    private lateinit var dragHandle: TextView
    private lateinit var uploadClick: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var profile_background_edit_icon: TextView
    private lateinit var profile_background_edit_icon2: TextView
    private lateinit var upload_cover_text: TextView
    private lateinit var update_cover_text: TextView
    private lateinit var collage_name: TextView
    private lateinit var backButton: ImageView

    private lateinit var headLineEdit: EditText
    private lateinit var birthDateEdit: EditText
    private lateinit var birthMonthEdit: EditText
    private lateinit var birthYearEdit: EditText
    private lateinit var stateEdit: EditText
    private lateinit var cityEdit: EditText
    private lateinit var genderEdit: RadioGroup

    private lateinit var updateButton: TextView
    private lateinit var UsernameError: TextView
    private lateinit var BirthDateError: TextView
    private lateinit var BirthMonthError: TextView
    private lateinit var BirthYearError: TextView
    private lateinit var StateError: TextView
    private lateinit var CityError: TextView
    private lateinit var GenderRadioError: TextView
    private lateinit var detailScrollView: ScrollView
    private lateinit var detailEditIcon: TextView
    private lateinit var closeUpdateDetail: ImageView

    private lateinit var btnActiveJobs: TextView
    private lateinit var btnClosedJobs: TextView
    private lateinit var jobPostsRecyclerView: RecyclerView

    private lateinit var activeJobs: List<InternshipPostData>
    private lateinit var closedJobs: List<InternshipPostData>
    private lateinit var internshipPostListLayout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Load Session
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val UserId = prefs.getString("userid", null)

        if (UserId != null){
            loadUserProfile(UserId)
            loadJobPosts(UserId)
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
        usernameEdit = findViewById(R.id.Username)
        uploadOverlay = findViewById(R.id.upload_back_cover_overlay)
        uploadSection = findViewById(R.id.upload_section)
        dragHandle = findViewById(R.id.back_cover_drag_handle)
        uploadClick = findViewById(R.id.upload_cover_click)
        profile_background_edit_icon = findViewById(R.id.profile_background_edit_icon)
        profile_background_edit_icon2 = findViewById(R.id.profile_background_edit_icon2)
        progressBar = findViewById(R.id.progressBar)
        upload_cover_text = findViewById(R.id.upload_cover_text)
        update_cover_text = findViewById(R.id.update_cover_text)
        collage_name = findViewById(R.id.collage_name)
        backButton = findViewById(R.id.backButton)

        headLineEdit = findViewById(R.id.headLine)
        stateEdit = findViewById(R.id.state)
        cityEdit = findViewById(R.id.City)
        genderEdit = findViewById(R.id.genderRadioGroup1)
        updateButton = findViewById(R.id.UpdateButton)
        UsernameError = findViewById(R.id.UsernameError)
        BirthDateError = findViewById(R.id.BirthDateError)
        BirthMonthError = findViewById(R.id.BirthMonthError)
        BirthYearError = findViewById(R.id.BirthYearError)
        StateError = findViewById(R.id.StateError)
        CityError = findViewById(R.id.CityError)
        GenderRadioError = findViewById(R.id.GenderRadioError)
        birthDateEdit = findViewById(R.id.BirthDate)
        birthMonthEdit = findViewById(R.id.BirthMonth)
        birthYearEdit = findViewById(R.id.BirthYear)
        detailScrollView = findViewById(R.id.detail_update_scroll_view)
        detailEditIcon = findViewById(R.id.detail_edit_icon)
        closeUpdateDetail = findViewById(R.id.closeUpdatedetail)

        jobPostsRecyclerView = findViewById(R.id.recyclerViewJobs)

        jobPostsRecyclerView.layoutManager = LinearLayoutManager(this)
        btnActiveJobs = findViewById(R.id.btnOpenJobs)
        btnClosedJobs = findViewById(R.id.btnClosedJobs)
        internshipPostListLayout = findViewById(R.id.InternshipPostListLayout)


        // Back Button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Open panel
        profile_background_edit_icon.setOnClickListener {
            openUploadSection()
        }
        profile_background_edit_icon2.setOnClickListener {
            openUploadSection()
        }

        // Close on background tap
        uploadOverlay.setOnClickListener {
            closeUploadSection()
        }

        // Prevent closing when tapping inside
        uploadSection.setOnClickListener {
            // Do nothing (prevents click-through)
        }

        // Close on drag handle tap
        dragHandle.setOnClickListener {
            closeUploadSection()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (uploadOverlay.visibility == View.VISIBLE) {
                    closeUploadSection()
                } else {
                    finish()
                }
            }
        })


        detailEditIcon.setOnClickListener {
            detailScrollView.visibility = View.VISIBLE
        }
        closeUpdateDetail.setOnClickListener {
            detailScrollView.visibility = View.GONE
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (detailScrollView.visibility == View.VISIBLE) {
                    detailScrollView.visibility = View.GONE
                } else {
                    // Default behavior (finish the activity or go back)
                    isEnabled = false  // disable this callback
                    onBackPressedDispatcher.onBackPressed() // call default back behavior
                }
            }
        })

        // Perform upload click
        uploadClick.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
            closeUploadSection()
        }

        userProfileImage2.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1002)  // use different requestCode than cover
        }

        updateButton.setOnClickListener {
            updatePersonalDetails()
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


        val sectionAddTextView = findViewById<TextView>(R.id.SectionAddId)
        val newSectionLayout = findViewById<LinearLayout>(R.id.newSectionLayout)

        sectionAddTextView.setOnClickListener {
            if (newSectionLayout.visibility == View.GONE) {
                newSectionLayout.visibility = View.VISIBLE
            } else {
                newSectionLayout.visibility = View.GONE
            }
        }

        // Example: Handle click for each option
        findViewById<TextView>(R.id.addProjects).setOnClickListener {
            Toast.makeText(this, "Add Projects Clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.addSkills).setOnClickListener {
           val intent = Intent(this, add_skills::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.addExperience).setOnClickListener {
            Toast.makeText(this, "Add Experience Clicked", Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
        }
    }

    private fun openUploadSection() {
        uploadOverlay.visibility = View.VISIBLE
        uploadSection.translationY = uploadSection.height.toFloat()
        uploadSection.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
    }

    private fun closeUploadSection() {
        uploadSection.animate()
            .translationY(uploadSection.height.toFloat())
            .setDuration(300)
            .withEndAction {
                uploadOverlay.visibility = View.GONE
            }
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            uploadImageToFirebase(imageUri)
        }

        if (requestCode == 1002 && resultCode == RESULT_OK && data?.data != null) {
            val imageUri2 = data.data
            imageUri2?.let {
                uploadProfileImageToFirebase(it) // Now it is a non-null Uri
            }
        }

    }

    private fun uploadImageToFirebase(imageUri: android.net.Uri?) {
        progressBar.visibility = View.VISIBLE
        if (imageUri == null) return

        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = prefs.getString("userid", null) ?: return

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("Users").document(userId)

        // Step 1: Get existing image URL
        userRef.get().addOnSuccessListener { doc ->
            val existingUrl = doc.getString("background_cover_url")

            // Step 2: If old image exists, delete it
            if (!existingUrl.isNullOrEmpty()) {
                val oldImageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                    .getReferenceFromUrl(existingUrl)

                oldImageRef.delete()
                    .addOnSuccessListener {
                        // Old image deleted, now upload new
                        uploadNewCoverImage(userId, imageUri)
                    }
                    .addOnFailureListener {
                        // Even if deletion fails, proceed to upload new image
                        uploadNewCoverImage(userId, imageUri)
                    }
            } else {
                // No old image, just upload new
                uploadNewCoverImage(userId, imageUri)
            }
        }
    }
    private fun uploadNewCoverImage(userId: String, imageUri: android.net.Uri) {
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
            .reference.child("user_backgrounds/$userId.jpg")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()

                val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
                userRef.update("background_cover_url", downloadUrl)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Cover image updated successfully", Toast.LENGTH_SHORT).show()

                        // Instantly load new image into ImageView
                        val coverImageView = findViewById<ImageView>(R.id.coverImageView)
                        Glide.with(this)
                            .load(downloadUrl)
                            .centerCrop() // Optional: crop nicely
                            .into(coverImageView)

                        // Hide placeholder and edit icon if needed
                        findViewById<View>(R.id.coverImage).visibility = View.GONE
                        findViewById<View>(R.id.profile_background_edit_icon).visibility = View.GONE
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Image upload failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfileImageToFirebase(imageUri: android.net.Uri) {
        progressBar.visibility = View.VISIBLE

        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = prefs.getString("userid", null) ?: return

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("Users").document(userId)

        // Step 1: Fetch existing profile image URL
        userRef.get().addOnSuccessListener { doc ->
            val existingUrl = doc.getString("profile_image_url")

            // Step 2: Delete old image if exists
            if (!existingUrl.isNullOrEmpty()) {
                val oldImageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                    .getReferenceFromUrl(existingUrl)

                oldImageRef.delete()
                    .addOnSuccessListener {
                        // Proceed to upload new image
                        uploadNewProfileImage(userId, imageUri)
                    }
                    .addOnFailureListener {
                        // Even if deletion fails, upload new image
                        uploadNewProfileImage(userId, imageUri)
                    }
            } else {
                // No old image, directly upload new one
                uploadNewProfileImage(userId, imageUri)
            }
        }
    }
    private fun uploadNewProfileImage(userId: String, imageUri: android.net.Uri) {
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
            .reference.child("profile_images/$userId.jpg")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()

                val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
                userRef.update("profile_image_url", downloadUrl)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()

                        // Instantly show new image
                        Glide.with(this)
                            .load(downloadUrl)
                            .centerCrop()
                            .into(userProfileImage2)
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Image upload failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePersonalDetails(): Boolean {
        var isValid = true

        val username = usernameEdit.text.toString().trim()
        val dobDate = birthDateEdit.text.toString().trim()
        val dobMonth = birthMonthEdit.text.toString().trim()
        val dobYear = birthYearEdit.text.toString().trim()
        val state = stateEdit.text.toString().trim()
        val city = cityEdit.text.toString().trim()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Username
        if (username.isEmpty()) {
            UsernameError.text = "Username is required"
            UsernameError.visibility = View.VISIBLE
            isValid = false
        } else {
            UsernameError.visibility = View.GONE
        }

        // Birth date
        if (dobDate.isEmpty()) {
            BirthDateError.text = "Date is required"
            BirthDateError.visibility = View.VISIBLE
            birthDateEdit.setBackgroundResource(R.drawable.border_error)
            isValid = false
        } else {
            BirthDateError.visibility = View.GONE
        }

        // Birth month
        if (dobMonth.isEmpty()) {
            BirthMonthError.text = "Month is required"
            BirthMonthError.visibility = View.VISIBLE
            birthMonthEdit.setBackgroundResource(R.drawable.border_error)
            isValid = false
        } else {
            val month = dobMonth.toIntOrNull()
            if (month == null || month !in 1..12) {
                BirthMonthError.text = "Month must be between 1 and 12"
                BirthMonthError.visibility = View.VISIBLE
                birthMonthEdit.setBackgroundResource(R.drawable.border_error)
                isValid = false
            } else {
                BirthMonthError.visibility = View.GONE
            }
        }

        // Birth year
        if (dobYear.isEmpty()) {
            BirthYearError.text = "Year is required"
            BirthYearError.visibility = View.VISIBLE
            birthYearEdit.setBackgroundResource(R.drawable.border_error)
            isValid = false
        } else {
            val year = dobYear.toIntOrNull()
            if (year == null || year !in 1900..currentYear) {
                BirthYearError.text = "Year must be between 1900 and $currentYear"
                BirthYearError.visibility = View.VISIBLE
                birthYearEdit.setBackgroundResource(R.drawable.border_error)
                isValid = false
            } else {
                BirthYearError.visibility = View.GONE
            }
        }

        // Check date validity if all 3 parts are valid
        val day = dobDate.toIntOrNull()
        val month = dobMonth.toIntOrNull()
        val year = dobYear.toIntOrNull()

        if (day != null && month != null && year != null) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1) // 0-based month
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (day !in 1..maxDay) {
                BirthDateError.text = "Date must be between 1 and $maxDay"
                BirthDateError.visibility = View.VISIBLE
                birthDateEdit.setBackgroundResource(R.drawable.border_error)
                isValid = false
            } else {
                BirthDateError.visibility = View.GONE
            }
        }

        // State
        if (state.isEmpty()) {
            StateError.text = "State is required"
            StateError.visibility = View.VISIBLE
            isValid = false
        } else {
            StateError.visibility = View.GONE
        }

        // City
        if (city.isEmpty()) {
            CityError.text = "City is required"
            CityError.visibility = View.VISIBLE
            isValid = false
        } else {
            CityError.visibility = View.GONE
        }

        // Gender
        if (genderEdit.checkedRadioButtonId == -1) {
            GenderRadioError.text = "Please select your gender"
            GenderRadioError.visibility = View.VISIBLE
            isValid = false
        } else {
            GenderRadioError.visibility = View.GONE
        }

        // Gender
        val gender = when (genderEdit.checkedRadioButtonId) {
            R.id.MaleRadioButton -> "Male"
            R.id.FemaleRadioButton -> "Female"
            else -> ""
        }

       if (isValid){
           progressBar.visibility = View.VISIBLE
           val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
           val UserId = prefs.getString("userid", null)

           if (UserId != null){
               val db = FirebaseFirestore.getInstance()
               val updateMap = hashMapOf<String, Any>(
                   "headline" to headLineEdit.text.toString(),
                   "username" to username,
                   "birth_date" to dobDate,
                   "birth_month" to dobMonth,
                   "birth_year" to dobYear,
                   "state" to state,
                   "city" to city,
                   "gender" to gender
               )
               db.collection("Users").document(UserId)
                   .update(updateMap)
                   .addOnSuccessListener {
                       progressBar.visibility = View.GONE
                       Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                       // Update views after saving data
                       var username = findViewById<TextView>(R.id.username)
                       username.text = usernameEdit.text.toString()
                       headline.text = headLineEdit.text.toString()
                       Location.text = "${cityEdit.text.toString()}, ${stateEdit.text.toString()}"

                       val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                       imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                       // Hide detail update form
                       detailScrollView.visibility = View.GONE
                   }

                   .addOnFailureListener { e ->
                       progressBar.visibility = View.GONE
                       Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                   }

           } else {
               progressBar.visibility = View.GONE
               Toast.makeText(this, "User ID not found in session", Toast.LENGTH_SHORT).show()
           }
       }

        return isValid
    }

    private fun loadUserProfile(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {


                    val coverImage = findViewById<TextView>(R.id.coverImage)
                    val coverImageView = findViewById<ImageView>(R.id.coverImageView)
                    val back_cover = doc.getString("background_cover_url")

                    if (!back_cover.isNullOrEmpty()) {
                        coverImage.visibility = View.GONE
                        coverImageView.visibility = View.VISIBLE
                        profile_background_edit_icon.visibility = View.GONE
                        profile_background_edit_icon2.visibility = View.VISIBLE
                        update_cover_text.visibility = View.VISIBLE
                        upload_cover_text.visibility = View.GONE

                        Glide.with(this)
                            .load(back_cover)
                            .into(coverImageView)
                    } else {
                        // Show TextView, hide ImageView
                        coverImage.visibility = View.VISIBLE
                        coverImageView.visibility = View.GONE
                        profile_background_edit_icon.visibility = View.VISIBLE
                        profile_background_edit_icon2.visibility = View.GONE
                        update_cover_text.visibility = View.GONE
                                upload_cover_text.visibility = View.VISIBLE
                    }


                    val imageUrl = doc.getString("profile_image_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(findViewById(R.id.UserProfileImage2))

                    }

                    val userName = doc.getString("username")
                    val companyName = doc.getString("company_name")
                    val role = doc.getString("role")

                    val username = findViewById<TextView>(R.id.username)

                    if (role == "Student") {
                        if (!userName.isNullOrEmpty()) {
                            username.text = userName
                            usernameEdit.setText(userName)

                            val collageName = doc.getString("collage_name") ?: ""
                            val collageView = findViewById<TextView>(R.id.collage_name)
                            collageView.text = collageName.split(" ").joinToString(" ") { word ->
                                word.replaceFirstChar { it.uppercase() }
                            }

                        }
                        internshipPostListLayout.visibility = View.GONE

                        var skillList = doc.getString("skill")
                        if (!skillList.isNullOrEmpty()){

                        }


                    } else if (role == "Company") {
                        if (!companyName.isNullOrEmpty()) {
                            username.text = companyName
                            usernameEdit.setText(companyName)
                            collage_name.visibility = View.GONE
                            internshipPostListLayout.visibility = View.VISIBLE
                        }

                        var SectionAddSection = findViewById<LinearLayout>(R.id.SectionAddSection)
                        SectionAddSection.visibility = View.GONE

                    } else {
                        username.text = "Guest"
                    }




                    val state = doc.getString("state") ?: ""
                    val city = doc.getString("city") ?: ""
                    val locationView = findViewById<TextView>(R.id.Location)
                    locationView.text = "$city, $state".trim().trimStart(',')
                    stateEdit.setText(state)
                    cityEdit.setText(city)

                    val gender = doc.getString("gender") ?: ""
                    if (gender == "Male") {
                        genderEdit.check(R.id.MaleRadioButton)
                    } else if (gender == "Female") {
                        genderEdit.check(R.id.FemaleRadioButton)
                    }

                    val headlineStr = doc.getString("headline") ?: ""
                    headLineEdit.setText(headlineStr)

                    if (!headlineStr.isNullOrEmpty()) {
                        headline.text = headlineStr
                    }
                    else{
                        headline.text = "Add Headline"
                    }

                    val dateOfBirth = doc.getString("date_of_birth") ?: ""
                    val dateOfBirtView = findViewById<TextView>(R.id.BirthDate)
                    val birthMonthView = findViewById<TextView>(R.id.BirthMonth)
                    val birthYearView = findViewById<TextView>(R.id.BirthYear)

                    // If date is in format "dd-MM-yyyy"
                    if (dateOfBirth.isNotEmpty() && dateOfBirth.contains("-")) {
                        val parts = dateOfBirth.split("-")
                        if (parts.size == 3) {
                            dateOfBirtView.text = parts[0]  // Day
                            birthMonthView.text = parts[1]  // Month
                            birthYearView.text = parts[2]   // Year
                        } else {
                            // fallback if format is wrong
                            dateOfBirtView.text = ""
                            birthMonthView.text = ""
                            birthYearView.text = ""
                        }
                    } else {
                        dateOfBirtView.text = ""
                        birthMonthView.text = ""
                        birthYearView.text = ""
                    }




                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
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