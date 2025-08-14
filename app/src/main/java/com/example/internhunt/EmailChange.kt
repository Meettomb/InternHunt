package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.concurrent.thread
import kotlin.jvm.java
import kotlin.math.log

class EmailChange : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var newEmailEditText: TextInputEditText
    private lateinit var newEmailLayout: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var newEmailError: TextView
    private lateinit var nextBtn: TextView
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_email_change)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Get session
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)

        // Check if session exists
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }




        backButton = findViewById(R.id.backButton)
        newEmailEditText = findViewById(R.id.newEmailEditText)
        newEmailLayout = findViewById(R.id.newEmailLayout)
        progressBar = findViewById(R.id.progressBar)
        newEmailError = findViewById(R.id.newEmailError)
        nextBtn = findViewById(R.id.nextBtn)

        backButton.setOnClickListener {
            finish()
        }

        nextBtn.setOnClickListener {
            var newEmail = newEmailEditText.text.toString()
            var isValid = true

            if (newEmail.isEmpty()) {
                newEmailError.text = "Email cannot be empty"
                return@setOnClickListener

            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                newEmailError.text = "Invalid email"
                return@setOnClickListener
            }
            else{
                newEmailError.text = null
            }

            if(isValid){
                sendEmailVerification(userId)
            }

        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun sendEmailVerification(userId: String){
        progressBar.visibility = View.VISIBLE
        nextBtn.isEnabled = false
        val newEmail = newEmailEditText.text.toString().trim()
        val db = FirebaseFirestore.getInstance()

        // Get current user's email first
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val currentEmail = documentSnapshot.getString("email").toString()

                if (currentEmail == newEmail){
                    progressBar.visibility = View.GONE
                    nextBtn.isEnabled = true
                    newEmailError.text = "Email already exists"
                    Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Check if any other user already has this email
                db.collection("Users")
                    .whereEqualTo("email", newEmail)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            progressBar.visibility = View.GONE
                            nextBtn.isEnabled = true
                            newEmailError.text = "Email is already used by another account"
                            Toast.makeText(this, "Email is already in use", Toast.LENGTH_SHORT).show()

                        } else {
                            val CurrentEmailOtp = (1000..9999).random()
                            val NewEmailOtp = (1000..9999).random()

                            thread {
                                try{

                                    runOnUiThread {
                                        newEmailError.text = null
                                    }

                                    val currentEmailSender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                                    currentEmailSender.sendEmail(
                                        toEmail = currentEmail,
                                        subject = "Verify Your Email Id With OTP",
                                        body = "Your OTP is: $CurrentEmailOtp"
                                    )

                                    val newEmailSender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                                    newEmailSender.sendEmail(
                                        toEmail = newEmail,
                                        subject = "Verify Your Email Id With OTP",
                                        body = "Your OTP is: $NewEmailOtp"
                                    )
                                    runOnUiThread {
                                        nextBtn.isEnabled = true
                                        progressBar.visibility = View.GONE
                                        hideKeyboard(newEmailEditText)
                                        // Start next activity here
                                        val intent = Intent(this, new_and_old_email_verify::class.java)
                                        intent.putExtra("newEmail", newEmail)
                                        intent.putExtra("newEmailOtp", NewEmailOtp)
                                        intent.putExtra("currentEmail", currentEmail)
                                        intent.putExtra("currentEmailOtp", CurrentEmailOtp)
                                        startActivity(intent)
                                        finish()
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    runOnUiThread {
                                        progressBar.visibility = View.GONE
                                        hideKeyboard(newEmailEditText)
                                        Toast.makeText(this, "Failed to send email", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }


                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to check email: ${e.message}", Toast.LENGTH_SHORT).show()
                        nextBtn.isEnabled = true
                        progressBar.visibility = View.GONE
                        hideKeyboard(newEmailEditText)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch user info: ${e.message}", Toast.LENGTH_SHORT).show()
                nextBtn.isEnabled = true
                progressBar.visibility = View.GONE
                hideKeyboard(newEmailEditText)
            }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}


