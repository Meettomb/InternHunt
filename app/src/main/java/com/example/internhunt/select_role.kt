package com.example.internhunt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class select_role : AppCompatActivity() {
    private lateinit var radioGroupbutton: RadioGroup
    private lateinit var radioError: TextView
    private lateinit var nextButton: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_role)

        radioGroupbutton = findViewById(R.id.radioGroup1)
        radioError = findViewById(R.id.RadioError)
        nextButton = findViewById(R.id.NextButton)

        nextButton.setOnClickListener {

            var valid = true

            val selectedRadioId = radioGroupbutton.checkedRadioButtonId
            var selectedUserType = ""

            if (selectedRadioId == -1) {
                radioError.visibility = View.VISIBLE
                valid = false
            } else {
                radioError.visibility = View.GONE
                val radioButton: RadioButton = findViewById(selectedRadioId)
                selectedUserType = radioButton.text.toString()
            }

            var intent= Intent(this, SignUp::class.java)
            intent.putExtra("userType", selectedUserType)
            startActivity(intent)

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}