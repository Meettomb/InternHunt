package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.SwitchCompat


class Setting : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var notificationPrefs: android.content.SharedPreferences
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Session Check
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        // UI elements
        backButton = findViewById(R.id.backButton)
        notificationSwitch = findViewById(R.id.notificationSwitch)
        notificationPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Back Button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Load saved setting from Firestore + SharedPreferences
        userId?.let { loadNotificationSetting(it) }

        // Save state on toggle
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            userId?.let { saveNotificationSetting(it, isChecked) }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadNotificationSetting(userId: String) {
        // Load from SharedPreferences first
        val isEnabled = notificationPrefs.getBoolean("notifyInternship", false)
        notificationSwitch.isChecked = isEnabled

        // Then sync with Firestore
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val notifySetting = doc.getBoolean("notify_new_internships") ?: false
                    notificationSwitch.isChecked = notifySetting

                    // Update SharedPreferences
                    notificationPrefs.edit()
                        .putBoolean("notifyInternship", notifySetting)
                        .apply()
                }
            }
    }

    private fun saveNotificationSetting(userId: String, isEnabled: Boolean) {
        // Save locally
        notificationPrefs.edit().putBoolean("notifyInternship", isEnabled).apply()

        // Update Firestore
        db.collection("Users").document(userId)
            .update("notify_new_internships", isEnabled)
            .addOnSuccessListener {
                val msg = if (isEnabled) {
                    "Internship notifications enabled"
                } else {
                    "Internship notifications disabled"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update setting", Toast.LENGTH_SHORT).show()
            }
    }
}
