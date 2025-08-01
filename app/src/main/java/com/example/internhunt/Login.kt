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

import android.content.Context
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firestore.v1.FirestoreGrpc
import java.security.MessageDigest
import kotlin.concurrent.thread

class Login : AppCompatActivity() {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    private var isPasswordVisible = false

    private lateinit var textViewSignUp: TextView
    private lateinit var loginButton: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView
    private lateinit var forgotPasswordLink: TextView

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        textViewSignUp = findViewById(R.id.textViewSignUp)
        loginButton = findViewById(R.id.Login)
        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.Password)
        emailError = findViewById(R.id.EmailError)
        passwordError = findViewById(R.id.PasswordError)
        forgotPasswordLink = findViewById(R.id.ForgotPasswordLink)

        progressBar = findViewById(R.id.progressBar)

        togglePasswordVisibility(passwordEditText)


        textViewSignUp.setOnClickListener {
            val intent = Intent(this, select_role::class.java)
            startActivity(intent)
        }


        forgotPasswordLink.setOnClickListener {
            var email = emailEditText.text.toString().trim()
            emailError.visibility = TextView.GONE

            if(email.isEmpty()){
                emailError.text = "Email is required"
                emailError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }


            val db = FirebaseFirestore.getInstance()

            progressBar.visibility = View.VISIBLE
            db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { document ->
                    if (document.isEmpty()){
                        emailError.text = "Email not found"
                        emailError.visibility = TextView.VISIBLE
                    }
                    else{

                        var otp = (1000.. 9999).random()
                        thread {
                            try {

                                var sender =
                                    JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")

                                sender.sendEmail(
                                    toEmail = email,
                                    subject = "Verifay Your OTP for Forgot Password",
                                    body = "Your OTP is: $otp"
                                )
                                runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    var intent = Intent(this, ForgotPasswordOtp::class.java)
                                    intent.putExtra("otp", otp.toString())
                                    intent.putExtra("email", email)
                                    startActivity(intent)
                                }
                            } catch(e: Exception){
                                e.printStackTrace()
                                runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, "Failed to send email", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Database error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            emailError.visibility = TextView.GONE
            passwordError.visibility = TextView.GONE

            if (email.isEmpty()) {
                emailError.text = "Email is required"
                emailError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordError.text = "Password is required"
                passwordError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        emailError.text = "Email not found."
                        emailError.visibility = TextView.VISIBLE
                    } else {
                        val doc = documents.documents[0]
                        val enteredPassword = passwordEditText.text.toString().trim()
                        val hashedInput = hashPassword(enteredPassword)
                        val storedPassword = doc.getString("password")
                        if (storedPassword == hashedInput) {
                            // Save to session using SharedPreferences
                            val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("userid", doc.id)
                                .putString("email", email)
                                .apply()

                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Home::class.java))
                            finish()
                        } else {
                            passwordError.text = "Incorrect password"
                            passwordError.visibility = TextView.VISIBLE
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Login failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }







        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // Index for drawableEnd
                val drawable = editText.compoundDrawables[drawableEnd]

                drawable?.let {
                    val touchAreaStart = editText.right - drawable.bounds.width() - editText.paddingEnd
                    if (event.rawX >= touchAreaStart) {
                        isPasswordVisible = !isPasswordVisible

                        // Toggle input type
                        editText.inputType = if (isPasswordVisible) {
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        } else {
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        }
                        editText.setSelection(editText.text.length)

                        // Toggle icon
                        val iconRes = if (isPasswordVisible) R.drawable.visibility_off else R.drawable.visibility
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, iconRes), null)

                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }



}
