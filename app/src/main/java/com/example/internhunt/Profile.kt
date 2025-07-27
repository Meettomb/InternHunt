package com.example.internhunt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var UserProfileImage: ImageView
    private lateinit var logoutButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        UserProfileImage = findViewById(R.id.UserProfileImage)
        logoutButton = findViewById(R.id.LogoutButton)


        // Get Session
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val UserId = prefs.getString("userid", null)

        if (UserId == null){
            Toast.makeText(this, "Please Login Again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        // Fetch User From Firebase
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(UserId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()){
                    val imageUrl = doc.getString("profile_image_url")

                    if (!imageUrl.isNullOrEmpty()){
                        Glide.with(this)
                            .load(imageUrl)
                            .into(UserProfileImage)
                    }
                }
                else{
                    Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }


        // Logout action
        logoutButton.setOnClickListener {
            prefs.edit().clear().apply() // Clear session
            val intent = Intent(this, Login::class.java)

            // Clears all the previous activities in the back stack (user can’t press "Back" to go to Home again)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}