package com.example.internhunt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import kotlin.concurrent.thread

class EnterNewPassword : AppCompatActivity() {
    private lateinit var userEmail: String
    private lateinit var newPassword: EditText
    private lateinit var passwordError: TextView
    private lateinit var confirmPassword: EditText
    private lateinit var confirmPasswordError: TextView
    private lateinit var updatePasswordButton: TextView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_enter_new_password)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        newPassword = findViewById(R.id.Password)
        passwordError = findViewById(R.id.PasswordError)
        confirmPassword = findViewById(R.id.Confirm_Password)
        confirmPasswordError = findViewById(R.id.Confirm_PasswordError)
        updatePasswordButton = findViewById(R.id.UpdatePassword)

        togglePasswordVisibility(newPassword)
        togglePasswordVisibility(confirmPassword)

        userEmail = intent.getStringExtra("email") ?: ""

        updatePasswordButton.setOnClickListener {
            passwordError.visibility = TextView.GONE
            confirmPasswordError.visibility = TextView.GONE

            val newPass = newPassword.text.toString().trim()
            val confirmPass = confirmPassword.text.toString().trim()
            var isValid = true

            if (newPass.isEmpty()) {
                passwordError.text = "Password is required"
                passwordError.visibility = TextView.VISIBLE
                isValid = false
            } else if (newPass.length < 6) {
                passwordError.text = "Password must be at least 6 characters"
                passwordError.visibility = TextView.VISIBLE
                isValid = false
            }

            if (confirmPass.isEmpty()) {
                confirmPasswordError.text = "Please confirm password"
                confirmPasswordError.visibility = TextView.VISIBLE
                isValid = false
            } else if (newPass != confirmPass) {
                confirmPasswordError.text = "Passwords do not match"
                confirmPasswordError.visibility = TextView.VISIBLE
                isValid = false
            }

            if (isValid) {
                val hashedPassword = hashPassword(newPass)
                val db = FirebaseFirestore.getInstance()
                db.collection("Users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener { docs ->
                        if (!docs.isEmpty) {
                            val userDoc = docs.documents[0].reference
                            userDoc.update("password", hashedPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Password updated successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    sendFinelMessage()

                                    startActivity(Intent(this, Login::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Failed to update password: ${e.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
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
                    val touchAreaStart =
                        editText.right - drawable.bounds.width() - editText.paddingEnd
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
                        val iconRes =
                            if (isPasswordVisible) R.drawable.visibility_off else R.drawable.visibility
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(this, iconRes),
                            null
                        )

                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(password.toByteArray(Charsets.UTF_8))
        return result.joinToString("") { "%02x".format(it) }
    }

    private fun sendFinelMessage() {
        thread {
            try {

                val sender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                sender.sendEmail(
                    toEmail = userEmail,
                    subject = "Success Message",
                    body = "Your password has been changed Success fully."
                )

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }


}