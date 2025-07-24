package com.example.internhunt

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_sign_up)

        collageName = findViewById(R.id.CollageName)
        collageNameError = findViewById(R.id.CollageNameError)
        degreeName = findViewById(R.id.DegreeName)
        degreeError = findViewById(R.id.DegreeError)
        graduationStartYear = findViewById(R.id.GraduationStartYear)
        graduationStartYearError = findViewById(R.id.GraduationStartYearError)

        graduationYear = findViewById(R.id.GraduationYear)
        graduationYearError = findViewById(R.id.GraduationYearError)

        signUpButton2 = findViewById(R.id.SignUpButton2)


        userEmail = intent.getStringExtra("email") ?: ""
        userName = intent.getStringExtra("username") ?: ""
        userPhone = intent.getStringExtra("phone") ?: ""
        userPassword = intent.getStringExtra("password") ?: ""
        dobDate = intent.getStringExtra("dobDate") ?: ""
        dobMonth = intent.getStringExtra("dobMonth") ?: ""
        dobYear = intent.getStringExtra("dobYear") ?: ""
        state = intent.getStringExtra("state") ?: ""
        city = intent.getStringExtra("city") ?: ""
        selectedUserType = intent.getStringExtra("userType") ?: ""
        selectedGender = intent.getStringExtra("gender") ?: ""


        signUpButton2.setOnClickListener {
            var isValid = true
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

            val collage = collageName.text.toString().trim()
            val degree = degreeName.text.toString().trim()
            val gradStartYearStr = graduationStartYear.text.toString().trim()
            val gradYearStr = graduationYear.text.toString().trim()

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

            // If all is valid, proceed
            if (isValid) {
                val db = FirebaseFirestore.getInstance()
                val newUserRef = db.collection("Users").document() // Generate a document reference

                val user = hashMapOf(
                    "id" to newUserRef.id,
                    "b_date" to dobDate,
                    "b_month" to dobMonth,
                    "b_year" to dobYear,
                    "city" to city,
                    "collage_name" to collage,
                    "degree_name" to degree,
                    "email" to userEmail,
                    "gender" to selectedGender,
                    "graduation_start_year" to gradStartYearStr,
                    "graduation_end_year" to gradYearStr,
                    "password" to userPassword,
                    "phone" to userPhone,
                    "role" to selectedUserType,
                    "state" to state,
                    "username" to userName
                )


                newUserRef.set(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to create user: ", Toast.LENGTH_LONG).show()
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
}