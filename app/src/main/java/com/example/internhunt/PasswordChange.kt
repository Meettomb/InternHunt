package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import java.security.MessageDigest
import java.security.Timestamp
import kotlin.concurrent.thread

class PasswordChange : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var currentPasswordLayout: TextInputLayout
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var savePasswordBtn: TextView
    private lateinit var currentPasswordError: TextView
    private lateinit var newPasswordError: TextView
    private lateinit var confirmPasswordError: TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password_change)
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

        backButton = findViewById(R.id.backButton)
        currentPasswordLayout = findViewById(R.id.currentPasswordLayout)
        newPasswordLayout = findViewById(R.id.newPasswordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        savePasswordBtn = findViewById(R.id.savePasswordBtn)
        currentPasswordError = findViewById(R.id.currentPasswordError)
        newPasswordError = findViewById(R.id.newPasswordError)
        confirmPasswordError = findViewById(R.id.confirmPasswordError)
        progressBar = findViewById(R.id.progressBar)

        backButton.setOnClickListener {
            finish()
        }

        val currentPasswordEditText = currentPasswordLayout.editText as TextInputEditText
        val newPasswordEditText = newPasswordLayout.editText as TextInputEditText
        val confirmPasswordEditText = confirmPasswordLayout.editText as TextInputEditText

        savePasswordBtn.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            var isValid = true


            if (currentPassword.isEmpty()) {
                currentPasswordError.text = "Current password is required"
                isValid = false
            } else {
                currentPasswordError.text = null

            }

            if (newPassword.isEmpty()) {
                newPasswordError.text = "New password is required"
                isValid = false
            } else if (newPassword.length < 6) {
                newPasswordError.text = "New password must be at least 6 characters"
                isValid = false
            } else {
                newPasswordError.text = null
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordError.text = "Confirm password is required"
                isValid = false
            } else if (newPassword != confirmPassword) {
                confirmPasswordError.text = "Passwords do not match"
                isValid = false
            } else {
                confirmPasswordError.text = null

            }

            if (isValid) {
                saeveResetPassword(userId)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saeveResetPassword(userId: String){
        progressBar.visibility = View.VISIBLE
        savePasswordBtn.isEnabled = false

        var db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var currentPasswordDatabase = document.getString("password")

                    val currentPasswordEditText = currentPasswordLayout.editText as TextInputEditText
                    val currentPassword = currentPasswordEditText.text.toString().trim()

                    val newPasswordEditText = newPasswordLayout.editText as TextInputEditText
                    val newPassword = newPasswordEditText.text.toString().trim()

                    var convertcurrentPassword = hashPassword(currentPassword)

                    Log.d("PasswordChange", "Current Password Database: $currentPasswordDatabase")
                    Log.d("PasswordChange", "Current Password: $convertcurrentPassword")

                    if (currentPasswordDatabase == convertcurrentPassword){
                        val newHashedPassword = hashPassword(newPassword)
                        db.collection("Users").document(userId).update("password", newHashedPassword)
                            .addOnSuccessListener {
                                hideKeyboard(savePasswordBtn)
                                savePasswordBtn.isEnabled = true
                                sendFinelMessage(userId)
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                hideKeyboard(savePasswordBtn)
                                savePasswordBtn.isEnabled = true
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show()
                            }

                    }
                    else{
                        hideKeyboard(savePasswordBtn)
                        savePasswordBtn.isEnabled = true
                        progressBar.visibility = View.GONE
                        currentPasswordError.text = "Current password is incorrect"
                    }

                }
            }

    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(password.toByteArray(Charsets.UTF_8))
        return result.joinToString("") { "%02x".format(it) }
    }

    private fun sendFinelMessage(userId: String) {
        val dateTime = com.google.firebase.Timestamp.now()
        val dateString = dateTime.toDate().toString()
        val body = "Your password has been changed successfully at \n$dateString."
        var db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    val userEmail = documentSnapshot.getString("email")
                    if (!userEmail.isNullOrEmpty()){
                        thread {
                            try {

                                val sender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                                sender.sendEmail(
                                    toEmail = userEmail,
                                    subject = "Success Message",
                                    body = body
                                )

                            } catch (e: Exception) {
                                e.printStackTrace()

                            }
                        }
                    }
                    else{
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

            }

    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}