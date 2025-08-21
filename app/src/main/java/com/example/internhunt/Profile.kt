package com.example.internhunt

import InputFilterMinMax
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import org.w3c.dom.Text


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


    private lateinit var skillsRecyclerView: RecyclerView
    private lateinit var skillsAdapter: SkillsAdapter
    private val skillsList = mutableListOf<String>()

    private lateinit var educationContainer: LinearLayout

    private lateinit var addMoreEducation: ImageView
    private lateinit var addMoreProjects: ImageView
    private lateinit var projectLinearLayout: LinearLayout
    private var profileListener: ListenerRegistration? = null
    private val degrees = arrayOf(
        "B.Tech (Bachelor of Technology)",
        "B.E. (Bachelor of Engineering)",
        "B.Sc (Bachelor of Science)",
        "BCA (Bachelor of Computer Applications)",
        "BBA (Bachelor of Business Administration)",
        "B.Com (Bachelor of Commerce)",
        "B.A. (Bachelor of Arts)",
        "M.Tech (Master of Technology)",
        "M.E. (Master of Engineering)",
        "M.Sc (Master of Science)",
        "MCA (Master of Computer Applications)",
        "MBA (Master of Business Administration)",
        "M.Com (Master of Commerce)",
        "PhD (Doctor of Philosophy)",
        "Diploma in Engineering",
        "Polytechnic Diploma",
        "B.Ed (Bachelor of Education)",
        "M.Ed (Master of Education)",
        "LLB (Bachelor of Laws)",
        "LLM (Master of Laws)",
        "MBBS (Bachelor of Medicine & Surgery)",
        "BDS (Bachelor of Dental Surgery)",
        "B.Pharm (Bachelor of Pharmacy)",
        "M.Pharm (Master of Pharmacy)",
        "B.Arch (Bachelor of Architecture)",
        "M.Arch (Master of Architecture)"
    )


    private lateinit var projectAdd: TextView
    private val projectAddLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val userId = prefs.getString("userid", null)
            if (userId != null) {
                loadProject(userId)      // Reload projects
                loadUserSkills(userId)   // Reload skills if needed
            }
        }
    }





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


        skillsRecyclerView = findViewById(R.id.SkillrecyclerView)
        skillsRecyclerView.layoutManager = LinearLayoutManager(this)
        skillsAdapter = SkillsAdapter(this, skillsList) { position, skill ->
            Toast.makeText(this, "Edit skill: $skill", Toast.LENGTH_SHORT).show()
            editSkillAtPosition(position)
        }

        skillsRecyclerView.adapter = skillsAdapter

        educationContainer = findViewById(R.id.edu_layout)
        addMoreEducation = findViewById(R.id.addMoreEducation)
        addMoreEducation.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_education, null)

            val etCollageName = dialogView.findViewById<EditText>(R.id.etCollageName)
            val etDegreeName = dialogView.findViewById<AutoCompleteTextView>(R.id.etDegreeName) // Change this in your XML
            val etStartYear = dialogView.findViewById<EditText>(R.id.etStartYear)
            val etEndYear = dialogView.findViewById<EditText>(R.id.etEndYear)

            // Set up AutoCompleteTextView adapter for degrees
            val adapter = ArrayAdapter(this, R.layout.dropdown_item, degrees)
            etDegreeName.setAdapter(adapter)
            etDegreeName.threshold = 1


            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
            .create()

            alertDialog.show()

            val saveButton = dialogView.findViewById<TextView>(R.id.saveTextView)
            val cancelButton = dialogView.findViewById<TextView>(R.id.cancelTextView)


            saveButton.setOnClickListener {
                val collageName = etCollageName.text.toString().trim()
                val degreeName = etDegreeName.text.toString().trim()
                val startYearStr = etStartYear.text.toString().trim()
                val endYearStr = etEndYear.text.toString().trim()

                // Basic empty checks
                if (collageName.isEmpty() || degreeName.isEmpty() || startYearStr.isEmpty() || endYearStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validate degree is in the predefined list
                if (!degrees.contains(degreeName)) {
                    Toast.makeText(this, "Please select a valid degree from the list", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check if years are valid numbers
                val startYear = startYearStr.toIntOrNull()
                val endYear = endYearStr.toIntOrNull()

                if (startYear == null || endYear == null) {
                    Toast.makeText(this, "Start Year and End Year must be valid numbers", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check year length (usually 4 digits)
                if (startYearStr.length != 4 || endYearStr.length != 4) {
                    Toast.makeText(this, "Please enter valid 4-digit years", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check logical order of years
                if (startYear > endYear) {
                    Toast.makeText(this, "Start Year cannot be after End Year", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                val currentUserId = prefs.getString("userid", null)

                if (!currentUserId.isNullOrEmpty()) {
                    saveEducationToFirestore(collageName, degreeName, startYearStr, endYearStr, currentUserId)
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                hideKeyboard(dialogView)
                alertDialog.dismiss()
            }
        }


        projectAdd = findViewById(R.id.addProjects)
        addMoreProjects = findViewById(R.id.addMoreProjects)
        projectLinearLayout = findViewById(R.id.projectLinearLayout)

        projectAdd.setOnClickListener {
            var intent = Intent(this, ProjectAdd::class.java)
            startActivity(intent)
        }
        addMoreProjects.setOnClickListener {
            var intent = Intent(this, ProjectAdd::class.java)
            startActivity(intent)
        }





        // Back Button
        backButton.setOnClickListener {
            finish()
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
            hideKeyboard(detailScrollView)
            detailScrollView.visibility = View.GONE
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (detailScrollView.visibility == View.VISIBLE) {
                    hideKeyboard(detailScrollView)
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



        val sectionAddTextView = findViewById<TextView>(R.id.SectionAddId)
        val newSectionLayout = findViewById<LinearLayout>(R.id.newSectionLayout)

        sectionAddTextView.setOnClickListener {
            if (newSectionLayout.visibility == View.GONE) {
                newSectionLayout.visibility = View.VISIBLE
            } else {
                newSectionLayout.visibility = View.GONE
            }
        }


        findViewById<TextView>(R.id.addSkills).setOnClickListener {
            val intent = Intent(this, add_skills::class.java)
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.addMoreSkills).setOnClickListener {
            var intent = Intent(this, add_skills::class.java)
            startActivity(intent)
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
                        if (!isFinishing && !isDestroyed) {
                            Glide.with(this)
                                .load(downloadUrl)
                                .centerCrop() // Optional: crop nicely
                                .into(coverImageView)
                        }

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

        // ✅ Always store in DD-MM-YYYY format with leading zeros
        val dateOfBirth = String.format(
            "%02d-%02d-%04d",
            dobDate.toIntOrNull() ?: 0,
            dobMonth.toIntOrNull() ?: 0,
            dobYear.toIntOrNull() ?: 0
        )

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

        if (isValid) {
            progressBar.visibility = View.VISIBLE
            val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
            val UserId = prefs.getString("userid", null)

            if (UserId != null) {
                val db = FirebaseFirestore.getInstance()
                val updateMap = hashMapOf<String, Any>(
                    "headline" to headLineEdit.text.toString(),
                    "username" to username,
                    "date_of_birth" to dateOfBirth, // ✅ Stored as DD-MM-YYYY
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
                        val usernameText = findViewById<TextView>(R.id.username)
                        usernameText.text = usernameEdit.text.toString()
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

        db.collection("Users").document(userId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

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

                        if (!isDestroyed && !isFinishing) {
                            Glide.with(this)
                                .load(back_cover)
                                .centerCrop() // optional
                                .into(coverImageView)
                        }
                    } else {
                        coverImage.visibility = View.VISIBLE
                        coverImageView.visibility = View.GONE
                        profile_background_edit_icon.visibility = View.VISIBLE
                        profile_background_edit_icon2.visibility = View.GONE
                        update_cover_text.visibility = View.GONE
                        upload_cover_text.visibility = View.VISIBLE
                    }


                    val imageUrl = doc.getString("profile_image_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        if (!isDestroyed && !isFinishing) {
                            Glide.with(this)
                                .load(imageUrl)
                                .into(findViewById(R.id.UserProfileImage2))
                        }
                    }


                    val userName = doc.getString("username")
                    val companyName = doc.getString("company_name")
                    val role = doc.getString("role")

                    val username = findViewById<TextView>(R.id.username)

                    if (role == "Student") {
                        if (!userName.isNullOrEmpty()) {
                            username.text = userName
                            usernameEdit.setText(userName)
                        }

                        loadUserSkills(userId)
                        loadEducation(userId)
                        loadProject(userId)

                        val sectionAddSection = findViewById<LinearLayout>(R.id.SectionAddSection)
                        val addProjects = findViewById<TextView>(R.id.addProjects)
                        val projectLinearLayout = findViewById<LinearLayout>(R.id.projectLinearLayout)
                        val addSkills = findViewById<TextView>(R.id.addSkills)

                        val skillList = doc.get("skill") as? List<String> ?: emptyList()
                        val projectList = doc.get("projects") as? List<Map<String, Any>> ?: emptyList()

                        sectionAddSection.visibility =
                            if (skillList.isNotEmpty() && projectList.isNotEmpty()) View.GONE else View.VISIBLE
                        addProjects.visibility = if (projectList.isNotEmpty()) View.GONE else View.VISIBLE
                        projectLinearLayout.visibility = if (projectList.isEmpty()) View.GONE else View.VISIBLE
                        addSkills.visibility = if (skillList.isNotEmpty()) View.GONE else View.VISIBLE

                        val skillLists = doc.get("skill") as? List<String> ?: emptyList()
                        if (skillLists.isEmpty()) {
                            findViewById<LinearLayout>(R.id.SkillSection).visibility = View.GONE
                        }
                    } else if (role == "Company") {
                        if (!companyName.isNullOrEmpty()) {
                            username.text = companyName
                            usernameEdit.setText(companyName)
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
                    stateEdit.setText(state)
                    cityEdit.setText(city)

                    val gender = doc.getString("gender") ?: ""
                    when (gender) {
                        "Male" -> genderEdit.check(R.id.MaleRadioButton)
                        "Female" -> genderEdit.check(R.id.FemaleRadioButton)
                    }

                    val headlineStr = doc.getString("headline") ?: ""
                    headLineEdit.setText(headlineStr)
                    headline.text = if (headlineStr.isNotEmpty()) headlineStr else "Add Headline"

                    val dateOfBirth = doc.getString("date_of_birth") ?: ""
                    val dateOfBirtView = findViewById<TextView>(R.id.BirthDate)
                    val birthMonthView = findViewById<TextView>(R.id.BirthMonth)
                    val birthYearView = findViewById<TextView>(R.id.BirthYear)

                    if (dateOfBirth.isNotEmpty() && dateOfBirth.contains("-")) {
                        val parts = dateOfBirth.split("-")
                        if (parts.size == 3) {
                            dateOfBirtView.text = parts[0]
                            birthMonthView.text = parts[1]
                            birthYearView.text = parts[2]
                        } else {
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
    private fun editSkillAtPosition(position: Int) {
        val skillToEdit = skillsList[position]

        val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
        val editTextSkill = dialogView.findViewById<EditText>(R.id.editTextSkill)
        val cancelTextView = dialogView.findViewById<TextView>(R.id.cancelTextView)
        val saveTextView = dialogView.findViewById<TextView>(R.id.saveTextView)
        val deleteTextView = dialogView.findViewById<TextView>(R.id.deleteTextView)

        editTextSkill.setText(skillToEdit) // pre-fill current skill

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.show()

        cancelTextView.setOnClickListener {
            hideKeyboard(dialogView)
            dialog.dismiss()
        }

        saveTextView.setOnClickListener {
            val newSkill = editTextSkill.text.toString().trim()
            if (newSkill.isNotEmpty()) {
                skillsList[position] = newSkill
                skillsAdapter.notifyItemChanged(position)

                // Update Firestore database with the updated list
                saveSkills(skillsList)
                hideKeyboard(dialogView)
                dialog.dismiss()
            } else {
                editTextSkill.error = "Skill cannot be empty"
            }
        }

        deleteTextView.setOnClickListener {
            skillsList.removeAt(position)
            skillsAdapter.notifyItemRemoved(position)
            saveSkills(skillsList)
            hideKeyboard(dialogView)
            dialog.dismiss()
        }
    }
    private fun saveSkills(updatedSkillsList: List<String>) {
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = prefs.getString("userid", null)

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("Users").document(userId)

        userDocRef.set(mapOf("skill" to updatedSkillsList), SetOptions.merge())
            .addOnSuccessListener {

                Toast.makeText(this, "Skills updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating skills: ${e.message}", Toast.LENGTH_LONG).show()
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

                            educationView.findViewById<ImageView>(R.id.editIcon).setOnClickListener {
                                openEditEducationDialog(education, index, userId)
                            }

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
    private fun saveEducationToFirestore(collageName: String, degreeName: String, startYear: String, endYear: String, userId: String) {
        val db = FirebaseFirestore.getInstance()

        val newEducationMap = hashMapOf(
            "collage_name" to collageName,
            "degree_name" to degreeName,
            "graduation_start_year" to startYear,
            "graduation_end_year" to endYear
        )

        db.collection("Users").document(userId)
            .update("education", FieldValue.arrayUnion(newEducationMap))
            .addOnSuccessListener {
                Toast.makeText(this, "Education added successfully", Toast.LENGTH_SHORT).show()

                hideKeyboard(educationContainer)
                loadEducation(userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add education: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun openEditEducationDialog(education: EducationEntry,position: Int,userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_education, null)

        val etCollegeName = dialogView.findViewById<EditText>(R.id.etCollageName)
        val etDegreeName = dialogView.findViewById<AutoCompleteTextView>(R.id.etDegreeName)
        val etStartYear = dialogView.findViewById<EditText>(R.id.etStartYear)
        val etEndYear = dialogView.findViewById<EditText>(R.id.etEndYear)

        // Pre-fill data
        etCollegeName.setText(education.collage_name)
        etDegreeName.setText(education.degree_name)
        etStartYear.setText(education.graduation_start_year)
        etEndYear.setText(education.graduation_end_year)

        val adapter = ArrayAdapter(this, R.layout.dropdown_item, degrees)
        etDegreeName.setAdapter(adapter)
        etDegreeName.threshold = 1

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        alertDialog.show()

        dialogView.findViewById<TextView>(R.id.saveTextView).setOnClickListener {
            val newCollegeName = etCollegeName.text.toString().trim()
            val newDegreeName = etDegreeName.text.toString().trim()
            val newStartYear = etStartYear.text.toString().trim()
            val newEndYear = etEndYear.text.toString().trim()

            // Add your validations here...

            val updatedEducation = EducationEntry(
                collage_name = newCollegeName,
                degree_name = newDegreeName,
                graduation_start_year = newStartYear,
                graduation_end_year = newEndYear
            )

            updateEducationList(userId, updatedEducation, position) { success ->
                if (success) {
                    Toast.makeText(this, "Education updated", Toast.LENGTH_SHORT).show()
                    hideKeyboard(dialogView)
                    alertDialog.dismiss()
                    loadEducation(userId)  // reload to update UI
                } else {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    hideKeyboard(dialogView)
                }
            }
        }

        dialogView.findViewById<TextView>(R.id.deleteTextView)?.setOnClickListener {
            deleteEducationEntry(userId, position) { success ->
                if (success) {
                    Toast.makeText(this, "Education deleted", Toast.LENGTH_SHORT).show()
                    hideKeyboard(dialogView)
                    alertDialog.dismiss()
                    loadEducation(userId)
                } else {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                    hideKeyboard(dialogView)
                }
            }
        }

        dialogView.findViewById<TextView>(R.id.cancelTextView).setOnClickListener {
            hideKeyboard(dialogView)
            alertDialog.dismiss()
        }
    }
    private fun updateEducationList(userId: String,updatedEducation: EducationEntry, position: Int, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("Users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(Users::class.java)
                    if (user != null) {
                        val eduList = user.education.toMutableList()
                        if (position in eduList.indices) {
                            eduList[position] = updatedEducation

                            userDocRef.update("education", eduList)
                                .addOnSuccessListener { callback(true) }
                                .addOnFailureListener { callback(false) }
                        } else {
                            callback(false)
                        }
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { callback(false) }
    }
    private fun deleteEducationEntry(userId: String, position: Int,callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("Users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(Users::class.java)
                    if (user != null) {
                        val eduList = user.education.toMutableList()
                        if (position in eduList.indices) {
                            eduList.removeAt(position)

                            userDocRef.update("education", eduList)
                                .addOnSuccessListener { callback(true) }
                                .addOnFailureListener { callback(false) }
                        } else {
                            callback(false)
                        }
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { callback(false) }
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
                        projectView.findViewById<ImageView>(R.id.editIcon).setOnClickListener {
                            val intent = Intent(this, project_edit::class.java)
                            intent.putExtra("position", index)
                            intent.putExtra("project", project)
                            startActivity(intent)
                        }

                        // Add view to layout
                        projectLayout.addView(projectView)
                    }
                } else {
                    Log.d("loadProject", "No projects found in document.")
                }
            }
    }


    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onDestroy() {
        super.onDestroy()
        profileListener?.remove() // stop listening when activity is gone
    }

}