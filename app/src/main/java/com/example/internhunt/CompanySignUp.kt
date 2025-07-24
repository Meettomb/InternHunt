package com.example.internhunt

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener


class CompanySignUp : AppCompatActivity() {

    private lateinit var companyName: EditText
    private lateinit var companyNameError: TextView

    private lateinit var companyLink: EditText
    private lateinit var companyLinkError: TextView

    private lateinit var companyDescription: EditText
    private lateinit var companyDescriptionError: TextView

    private lateinit var signUpButton2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_company_sign_up)


        companyName = findViewById(R.id.CompanyName)
        companyNameError = findViewById(R.id.CompanyNameError)

        companyLink = findViewById(R.id.CompanyLink)
        companyLinkError = findViewById(R.id.CompanyLinkError)

        companyDescription = findViewById(R.id.CompanyDescription)
        companyDescriptionError = findViewById(R.id.CompanyDescriptionError)

        signUpButton2 = findViewById(R.id.SignUpButton2)


        signUpButton2.setOnClickListener {
            var isValid = true

            val name = companyName.text.toString().trim()
            val link = companyLink.text.toString().trim()
            val description = companyDescription.text.toString().trim()

            // Validate Company Name
            if (name.isEmpty()) {
                companyNameError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                companyNameError.visibility = TextView.GONE
            }

            // Validate Company Link Format
            if (link.isEmpty()) {
                companyLinkError.text = "This field is required"
                companyLinkError.visibility = TextView.VISIBLE
                isValid = false
            } else if (!android.util.Patterns.WEB_URL.matcher(link).matches()) {
                companyLinkError.text = "Enter a valid URL (e.g., https://example.com)"
                companyLinkError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                companyLinkError.visibility = TextView.GONE
            }

            // Validate Company Description
            if (description.isEmpty()) {
                companyDescriptionError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                companyDescriptionError.visibility = TextView.GONE
            }

            // Proceed if all is valid
            if (isValid) {
                // TODO: Save data or proceed
            }
        }


        companyName.addTextChangedListener {
            companyNameError.visibility = TextView.GONE
        }

        companyLink.addTextChangedListener {
            companyLinkError.visibility = TextView.GONE
        }

        companyDescription.addTextChangedListener {
            companyDescriptionError.visibility = TextView.GONE
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}