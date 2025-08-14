package com.example.internhunt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Security : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var emailChange: LinearLayout
    private lateinit var passwordChange: LinearLayout
    private lateinit var termsAndConditions: LinearLayout
    private lateinit var privacy: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_security)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        backButton = findViewById(R.id.backButton)
        emailChange = findViewById(R.id.emailChange)
        passwordChange = findViewById(R.id.passwordChange)
        termsAndConditions = findViewById(R.id.terms_and_conditions)
        privacy = findViewById(R.id.privacy)

        backButton.setOnClickListener {
            finish()
        }

        emailChange.setOnClickListener {
            // Handle email change click
        }

        passwordChange.setOnClickListener {
            var intent = Intent(this, PasswordChange::class.java)
            startActivity(intent)
        }

        termsAndConditions.setOnClickListener {
            var intent = Intent(this, TermsAndConditions::class.java)
            startActivity(intent)
        }

        privacy.setOnClickListener {
            var intent = Intent(this, Privacy::class.java)
            startActivity(intent)
        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}