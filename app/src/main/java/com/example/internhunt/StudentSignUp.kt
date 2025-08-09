package com.example.internhunt

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.firebase.storage.FirebaseStorage
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import java.util.Calendar
import kotlin.concurrent.thread


class StudentSignUp : AppCompatActivity() {

    private lateinit var collageName: EditText
    private lateinit var collageNameError: TextView
    private lateinit var degreeName: EditText
    private lateinit var degreeError: TextView

    private lateinit var graduationStartYear: EditText
    private lateinit var graduationStartYearError: TextView
    private lateinit var graduationYear: EditText
    private lateinit var graduationYearError: TextView

    private lateinit var signUpButton2: TextView

    private lateinit var userEmail: String
    private lateinit var userName: String
    private lateinit var userPhone: String
    private lateinit var userPassword: String
    private lateinit var dobDate: String
    private lateinit var dobMonth: String
    private lateinit var dobYear: String
    private lateinit var state: String
    private lateinit var city: String
    private lateinit var selectedUserType: String
    private lateinit var selectedGender: String

    // Image Uplode
    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var ImageUplodeError: TextView


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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_sign_up)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        collageName = findViewById(R.id.CollageName)
        collageNameError = findViewById(R.id.CollageNameError)
        degreeName = findViewById(R.id.DegreeName)

        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_item,         // your LinearLayout layout
            R.id.text1,                   // ID of TextView inside it
            degrees
        )
        // ✅ custom layout
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.DegreeName)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 1

        degreeError = findViewById(R.id.DegreeError)
        graduationStartYear = findViewById(R.id.GraduationStartYear)
        graduationStartYearError = findViewById(R.id.GraduationStartYearError)

        graduationYear = findViewById(R.id.GraduationYear)
        graduationYearError = findViewById(R.id.GraduationYearError)

        signUpButton2 = findViewById(R.id.SignUpButton2)
        progressBar = findViewById(R.id.progressBar)

        ImageUplodeError = findViewById(R.id.imageUplodeError)


        userEmail = intent.getStringExtra("email") ?: ""
        userName = intent.getStringExtra("username") ?: ""
        userPhone = intent.getStringExtra("phone") ?: ""

        userPassword = intent.getStringExtra("password") ?: ""
        val dobDay = intent.getStringExtra("dobDate") ?: ""
        val dobMonth = intent.getStringExtra("dobMonth") ?: ""
        val dobYear = intent.getStringExtra("dobYear") ?: ""

        val dateOfBirth = if (dobDay.isNotEmpty() && dobMonth.isNotEmpty() && dobYear.isNotEmpty()) {
            dobDay.padStart(2, '0') + "-" + dobMonth.padStart(2, '0') + "-" + dobYear
        } else {
            "" // Leave empty if any part is missing
        }

        state = intent.getStringExtra("state") ?: ""
        city = intent.getStringExtra("city") ?: ""
        selectedUserType = intent.getStringExtra("userType") ?: ""
        selectedGender = intent.getStringExtra("gender") ?: ""


        profileImageView = findViewById(R.id.profileImageView)
        val selectImageText = findViewById<TextView>(R.id.selectImageText)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImageView.setImageURI(it)
            }
        }

        profileImageView.setOnClickListener { pickImage.launch("image/*") }
        selectImageText.setOnClickListener { pickImage.launch("image/*") }


        signUpButton2.setOnClickListener {
            var isValid = true
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val educationList = mutableListOf<Map<String, String>>()

            val collage = collageName.text.toString().trim()
            val degree = degreeName.text.toString().trim()
            val gradStartYearStr = graduationStartYear.text.toString().trim()
            val gradYearStr = graduationYear.text.toString().trim()
            val imageUplode = ImageUplodeError.text.toString().trim()

            val educationEntry = mapOf(
                "collage_name" to collage,
                "degree_name" to degree,
                "graduation_start_year" to gradStartYearStr,
                "graduation_end_year" to gradYearStr
            )
            educationList.add(educationEntry)
            val gradStartYear = gradStartYearStr.toIntOrNull()
            val gradYear = gradYearStr.toIntOrNull()
            val maxYear = currentYear + 5

            // Validate College Name
            if (collage.isEmpty()) {
                collageNameError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                collageNameError.visibility = TextView.GONE
            }

            // Validate Degree Name
            if (degree.isEmpty()) {
                degreeError.text = "Degree is required"
                degreeError.visibility = TextView.VISIBLE
                isValid = false
            } else if (!degrees.contains(degree)) {
                degreeError.text = "Please select your degree from the list provided."
                degreeError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                degreeError.visibility = TextView.GONE
            }


            // Validate Graduation Start Year
            if (gradStartYearStr.isEmpty()) {
                graduationStartYearError.text = "This field is required"
                graduationStartYearError.visibility = TextView.VISIBLE
                isValid = false
            } else if (gradStartYear == null || gradStartYear < 1900 || gradStartYear > maxYear) {
                graduationStartYearError.text = "Enter a valid year between 1900 and $maxYear"
                graduationStartYearError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                graduationStartYearError.visibility = TextView.GONE
            }

            // Validate Graduation Year
            if (gradYearStr.isEmpty()) {
                graduationYearError.text = "This field is required"
                graduationYearError.visibility = TextView.VISIBLE
                isValid = false
            } else if (gradYear == null || gradYear < 1900 || gradYear > maxYear) {
                graduationYearError.text = "Enter a valid year between 1900 and $maxYear"
                graduationYearError.visibility = TextView.VISIBLE
                isValid = false
            } else if (gradStartYear != null && gradYear < gradStartYear) {
                graduationYearError.text = "Graduation year cannot be earlier than the start year"
                graduationYearError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                graduationYearError.visibility = TextView.GONE
            }

            if (selectedImageUri == null) {
                ImageUplodeError.visibility = View.VISIBLE
                isValid = false
            } else {
                ImageUplodeError.visibility = View.GONE
            }

            // If all is valid, proceed
            if (isValid) {

                progressBar.visibility = View.VISIBLE

                val db = FirebaseFirestore.getInstance()
                val newUserRef = db.collection("Users").document() // Generate a document reference


                if (selectedImageUri != null) {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child("profile_images/${newUserRef.id}.jpg")

                    imageRef.putFile(selectedImageUri!!)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                throw task.exception ?: Exception("Upload failed")
                            }
                            imageRef.downloadUrl
                        }
                        .addOnSuccessListener { uri ->
                            val calendar = Calendar.getInstance()
                            val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0') // Month is 0-based
                            val year = calendar.get(Calendar.YEAR).toString()

                            val signupDate = "$day-$month-$year"

                            val user = hashMapOf(
                                "id" to newUserRef.id,
                                "date_of_birth" to dateOfBirth,
                                "city" to city,
                                "education" to educationList,
                                "email" to userEmail,
                                "gender" to selectedGender,
                                "password" to userPassword,
                                "phone" to userPhone,
                                "role" to selectedUserType,
                                "state" to state,
                                "username" to userName,
                                "profile_image_url" to uri.toString(),
                                "isactive" to true,
                                "signup_date" to signupDate
                            )

                            newUserRef.set(user)
                                .addOnSuccessListener {
                                    progressBar.visibility = View.GONE // Hide loader
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    sendFinelMessage()
                                    startActivity(Intent(this, Login::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE // Hide loader
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_LONG).show()
                                }

                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE // Hide loader
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_LONG).show()
                        }

                } else {
                    progressBar.visibility = View.GONE // Hide loader
                    Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
                }




            }
        }

        // Clear error when typing in College Name
        collageName.addTextChangedListener {
            collageNameError.visibility = TextView.GONE
        }

        // Clear error when typing in Degree Name
        degreeName.addTextChangedListener {
            degreeError.visibility = TextView.GONE
        }

        // Clear error when typing in Graduation Start Year
        graduationStartYear.addTextChangedListener {
            graduationStartYearError.visibility = TextView.GONE
        }

        // Clear error when typing in Graduation Year
        graduationYear.addTextChangedListener {
            graduationYearError.visibility = TextView.GONE
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun sendFinelMessage() {
        thread {
            try {

                val sender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                sender.sendEmail(
                    toEmail = userEmail,
                    subject = "Success Message",
                    body = "Your Registration Success fully Complete."
                )

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

}