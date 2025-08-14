package com.example.internhunt

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TermsAndConditions : AppCompatActivity() {

    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        backButton = findViewById(R.id.backButton)

        // Status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primary_color)
            window.decorView.systemUiVisibility = 0 // light status bar off for colored header
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.primary_color)
        }

        backButton.setOnClickListener { finish() }

        val termsTextView: TextView = findViewById(R.id.termsText)
        termsTextView.text = Html.fromHtml(getString(R.string.terms_and_conditions_text), Html.FROM_HTML_MODE_LEGACY)


        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
