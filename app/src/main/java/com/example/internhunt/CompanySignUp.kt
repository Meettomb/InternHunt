package com.example.internhunt
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar


class CompanySignUp : AppCompatActivity() {

    private lateinit var companyName: EditText
    private lateinit var companyNameError: TextView

    private lateinit var companyLink: EditText
    private lateinit var companyLinkError: TextView

    private lateinit var companyDescription: EditText
    private lateinit var companyDescriptionError: TextView

    private lateinit var signUpButton2: TextView



    private lateinit var userEmail: String
    private lateinit var userName: String
    private lateinit var userPhone: String
    private lateinit var userPassword: String
    private lateinit var state: String
    private lateinit var city: String
    private lateinit var selectedUserType: String
    // Image Uplode
    private lateinit var profileImageView: ImageView
    private lateinit var ImageUplodeError: TextView
    private var selectedImageUri: Uri? = null

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_company_sign_up)


        companyName = findViewById(R.id.CompanyName)
        companyNameError = findViewById(R.id.CompanyNameError)

        companyLink = findViewById(R.id.CompanyLink)
        companyLinkError = findViewById(R.id.CompanyLinkError)

        companyDescription = findViewById(R.id.CompanyDescription)
        companyDescriptionError = findViewById(R.id.CompanyDescriptionError)

        signUpButton2 = findViewById(R.id.SignUpButton2)
        ImageUplodeError = findViewById(R.id.ImageUplodeError)
        progressBar = findViewById(R.id.progressBar2)



        profileImageView = findViewById(R.id.profileImageView2)
        val selectImageText = findViewById<TextView>(R.id.selectImageText2)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImageView.setImageURI(it)
            }
        }

        profileImageView.setOnClickListener { pickImage.launch("image/*") }
        selectImageText.setOnClickListener { pickImage.launch("image/*") }

        userEmail = intent.getStringExtra("email") ?: ""
        userName = intent.getStringExtra("username") ?: ""
        userPhone = intent.getStringExtra("phone") ?: ""
        userPassword = intent.getStringExtra("password") ?: ""

        state = intent.getStringExtra("state") ?: ""
        city = intent.getStringExtra("city") ?: ""
        selectedUserType = intent.getStringExtra("userType") ?: ""



        signUpButton2.setOnClickListener {
            var isValid = true

            val company_name = companyName.text.toString().trim()
            val company_link = companyLink.text.toString().trim()
            val description = companyDescription.text.toString().trim()
            val imageUplode = ImageUplodeError.text.toString().trim()

            // Validate Company Name
            if (company_name.isEmpty()) {
                companyNameError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                companyNameError.visibility = TextView.GONE
            }

            // Validate Company Link Format
            if (company_link.isEmpty()) {
                companyLinkError.text = "This field is required"
                companyLinkError.visibility = TextView.VISIBLE
                isValid = false
            } else if (!android.util.Patterns.WEB_URL.matcher(company_link).matches()) {
                companyLinkError.text = "Enter a valid URL (e.g., https://example.com)"
                companyLinkError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                companyLinkError.visibility = TextView.GONE
            }

            // Validate Company Description
            if (description.isEmpty()) {
                companyDescriptionError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                companyDescriptionError.visibility = TextView.GONE
            }

            // Validate Company Logo
            if (selectedImageUri == null) {
                ImageUplodeError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                ImageUplodeError.visibility = TextView.GONE
            }

            // Proceed if all is valid
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
                        .addOnSuccessListener{uri ->
                            val calendar = Calendar.getInstance()
                            val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0') // Month is 0-based
                            val year = calendar.get(Calendar.YEAR).toString()

                            val signupDate = "$day-$month-$year"

                            val user = hashMapOf(
                                "id" to newUserRef.id,
                                "city" to city,
                                "company_name" to company_name,
                                "company_url" to company_link,
                                "company_description" to description,
                                "email" to userEmail,
                                "password" to userPassword,
                                "phone" to userPhone,
                                "role" to selectedUserType,
                                "state" to state,
                                "username" to userName,
                                "profile_image_url" to uri.toString(),
                                "isactive" to true,
                                "signup_date" to signupDate
                            )
                                newUserRef.set(user) // Use set() to store data at that specific document ID
                                .addOnSuccessListener {
                                    progressBar.visibility = View.GONE // Hide loader
                                    Toast.makeText(this, "Registration successfully!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, Login::class.java))
                                    finish()
                                }

                                .addOnFailureListener { exception ->
                                    progressBar.visibility = View.GONE // Hide loader
                                    Toast.makeText(this, "Failed to create user: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE // Hide loader
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_LONG).show()
                        }
                } else{
                    progressBar.visibility = View.GONE // Hide loader
                    Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
                }




            }

        }


        companyName.addTextChangedListener {
            companyNameError.visibility = TextView.GONE
        }

        companyLink.addTextChangedListener {
            companyLinkError.visibility = TextView.GONE
        }

        companyDescription.addTextChangedListener {
            companyDescriptionError.visibility = TextView.GONE
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}