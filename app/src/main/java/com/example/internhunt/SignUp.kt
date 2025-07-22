package com.example.internhunt

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.concurrent.thread

class SignUp : AppCompatActivity() {

    private lateinit var signUpButton: TextView
    private lateinit var email: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        signUpButton = findViewById(R.id.SignUpButton)
        email = findViewById(R.id.Email)


        signUpButton.setOnClickListener {
            val userEmail = email.text.toString().trim()

            if (userEmail.isNotEmpty()){
                thread {
                    try{
                        val otp = (1000..9999).random()

                        val sender = JakartaMailSender("patelmeet23032005@gmail.com", "mcdw ahkj pxbk iipc")
                        sender.sendEmail(
                            toEmail = userEmail,
                            subject = "Verify Your Email Id With OTP",
                            body = "Your OTP is: $otp"
                        )
                    } catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
            else{
                email.error = "Email Required"
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}