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
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class Login : AppCompatActivity() {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private lateinit var textViewSignUp: TextView
    private lateinit var loginButton: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView

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

        textViewSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
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
                        emailError.text = "Email not found"
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


        var isPasswordVisible = false

        val passwordEditText = findViewById<EditText>(R.id.Password)
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        // Set initial eye icon (open eye → "password is hidden")
        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
            null, null,
            ContextCompat.getDrawable(this, R.drawable.visibility), null
        )

        passwordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // index for drawableEnd
                val drawable = passwordEditText.compoundDrawables[drawableEnd]
                if (drawable != null) {
                    val bounds = drawable.bounds
                    val x = event.rawX.toInt()
                    val width = passwordEditText.right - passwordEditText.left
                    val drawableStartX = width - bounds.width() - passwordEditText.paddingEnd

                    if (x >= drawableStartX + passwordEditText.left) {
                        // Toggle visibility
                        isPasswordVisible = !isPasswordVisible
                        if (isPasswordVisible) {
                            passwordEditText.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                                null, null,
                                ContextCompat.getDrawable(this, R.drawable.visibility_off), null
                            )
                        } else {
                            passwordEditText.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                                null, null,
                                ContextCompat.getDrawable(this, R.drawable.visibility), null
                            )
                        }
                        // Move cursor to end after toggling
                        passwordEditText.setSelection(passwordEditText.text.length)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}