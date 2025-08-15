package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class CompanyHomePage : AppCompatActivity() {
    private var popupWindow: PopupWindow? = null
    private lateinit var UserProfileImage: ImageView
    private lateinit var company_name: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContentView(R.layout.activity_company_home_page)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Get session
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        val role = prefs.getString("role", null)?.trim()?.lowercase()

        // Check if session exists
        if (userId == null || role == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        when (role) {
            "company" -> {
                if (this !is CompanyHomePage) { // Prevent reopening the same page
                    startActivity(Intent(this, CompanyHomePage::class.java))
                    finish()
                }
            }
            "student" -> {
                if (this !is Home) { // Prevent reopening same page
                    startActivity(Intent(this, Home::class.java))
                    finish()
                }
            }
            else -> {
                Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }



        val headerProfile: View = findViewById(R.id.header_profile)
        UserProfileImage = findViewById(R.id.UserProfileImage)
        company_name = findViewById(R.id.company_name)

        if (userId != null) {
            lodeCompanyDetail(userId)
        }

        headerProfile.setOnClickListener {
            if (popupWindow != null && popupWindow!!.isShowing) {
                popupWindow!!.dismiss()
            } else {
                showProfileMenu(it)
            }
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun lodeCompanyDetail(userId: String){
        var db = FirebaseFirestore.getInstance()
        var userRef = db.collection("Users").document(userId)
        userRef.addSnapshotListener { doc, error ->
            if (error != null) {
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                var companyName = doc.getString("company_name")
                var profileImage = doc.getString("profile_image_url")

                if (companyName != null) {
                    company_name.text = "$companyName!"
                }
                if (profileImage != null) {
                    Glide.with(this)
                        .load(profileImage)
                        .into(UserProfileImage)
                }
            }
        }
    }

    private fun showProfileMenu(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.profile_dropdown, null)
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        val role = prefs.getString("role", null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true // Allow outside touch dismiss
        ).apply {
            isOutsideTouchable = true
            showAsDropDown(anchor, -150, 10) // Position adjust
        }

        // Menu item clicks
        popupView.findViewById<View>(R.id.manageProfile).setOnClickListener {
            // Handle Manage Profile
            popupWindow?.dismiss()
        }
        popupView.findViewById<View>(R.id.home).setOnClickListener {
            // Handle Home Redirect
            popupWindow?.dismiss()
        }

        popupView.findViewById<View>(R.id.settings).setOnClickListener {
            // Handle Settings
            popupWindow?.dismiss()
        }

        popupView.findViewById<View>(R.id.logout).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

}