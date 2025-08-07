package com.example.internhunt

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.eclipse.angus.mail.imap.protocol.ID

class CompanyDetail : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var companyLogo: ImageView
    private lateinit var companyName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_company_detail)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        companyLogo = findViewById(R.id.CompanyLogo)
        companyName = findViewById(R.id.companyName)
        val companyId = intent.getStringExtra("companyId")

        if (companyId != null) {
            loadCompanyDetail(companyId)
        } else {
            Toast.makeText(this, "Company ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadCompanyDetail(companyID: String) {
        val db = FirebaseFirestore.getInstance()

        // Step 1: Fetch the company from "Users" collection using document ID
        db.collection("Users").document(companyID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val companyNameValue = document.getString("company_name") ?: "N/A"
                    val profileImageUrl = document.getString("profile_image_url") ?: ""
                    val userUniqueId = document.getString("id") ?: ""

                    companyName.text = companyNameValue
                    Glide.with(this)
                        .load(profileImageUrl)
                        .into(companyLogo)

                    // Step 2: Use the `id` to fetch more details from another collection


                } else {
                    Toast.makeText(this, "Company not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading company", Toast.LENGTH_SHORT).show()
            }
    }

}