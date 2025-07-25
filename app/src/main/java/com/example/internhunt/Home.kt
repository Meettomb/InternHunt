package com.example.internhunt

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore

import android.widget.ImageView
import com.bumptech.glide.Glide


class Home : AppCompatActivity() {

    private lateinit var logoutButton: TextView
    private lateinit var userImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        logoutButton = findViewById(R.id.LogoutButton)
        val userImageView = findViewById<ImageView>(R.id.UserProfileImage)




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

        // Fetch user from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val username = doc.getString("username") ?: "Unknown"

                    val imageUrl = doc.getString("profile_image_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(userImageView)
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }




        // Logout action
        logoutButton.setOnClickListener {
            prefs.edit().clear().apply() // Clear session
            val intent = Intent(this, Login::class.java)

            // Clears all the previous activities in the back stack (user can’t press "Back" to go to Home again)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish() // closes the current Home activity
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}