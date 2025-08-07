package com.example.internhunt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class VerifyOtp : AppCompatActivity() {

    private lateinit var otpInput: EditText
    private lateinit var verifyButton: TextView
    private lateinit var resendButton: TextView
    private lateinit var backButton: TextView
    private lateinit var otpMessage: TextView
    private lateinit var timerText: TextView

    private var currentOtp: String = ""
    private var timer: CountDownTimer? = null
    private lateinit var userEmail: String
    private lateinit var userName: String
    private lateinit var userPhone: String
    private lateinit var userPassword: String
    private lateinit var dobDate: String
    private lateinit var dobMonth: String
    private lateinit var dobYear: String
    private lateinit var state: String
    private lateinit var city: String
    private lateinit var selectedUserType: String
    private lateinit var selectedGender: String
    private var otpExpiryTime: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_otp)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)
        resendButton = findViewById(R.id.resendButton)
        backButton = findViewById(R.id.backButton)
        otpMessage = findViewById(R.id.otpMessage)
        timerText = findViewById(R.id.timerText)

        currentOtp = intent.getStringExtra("otp") ?: ""
        otpExpiryTime = System.currentTimeMillis() + 2 * 60 * 1000

        userEmail = intent.getStringExtra("email") ?: ""
        userName = intent.getStringExtra("username") ?: ""
        userPhone = intent.getStringExtra("phone") ?: ""
        userPassword = intent.getStringExtra("password") ?: ""
        dobDate = intent.getStringExtra("dobDate") ?: ""
        dobMonth = intent.getStringExtra("dobMonth") ?: ""
        dobYear = intent.getStringExtra("dobYear") ?: ""
        state = intent.getStringExtra("state") ?: ""
        city = intent.getStringExtra("city") ?: ""
        selectedUserType = intent.getStringExtra("userType") ?: ""
        selectedGender = intent.getStringExtra("gender") ?: ""


        startTimer()
        // Get OTP from intent
        val correctOtp = intent.getStringExtra("otp")

        verifyButton.setOnClickListener {
            val enteredOtp = otpInput.text.toString().trim()
            val currentTime = System.currentTimeMillis()

            if (currentTime > otpExpiryTime) {
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                otpMessage.text = "OTP has expired. Please request a new one."
                resendButton.visibility = Button.VISIBLE
                backButton.visibility = Button.VISIBLE
                return@setOnClickListener
            }

            if (enteredOtp == currentOtp) {
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                otpMessage.text = "OTP Verified Successfully!"


                val intent = if (selectedUserType == "Student") {
                    Intent(this, StudentSignUp::class.java)
                } else {
                    Intent(this, CompanySignUp::class.java)
                }

                intent.putExtra("email", userEmail)
                intent.putExtra("username", userName)
                intent.putExtra("phone", userPhone)
                intent.putExtra("password", userPassword)
                intent.putExtra("dobDate", dobDate)
                intent.putExtra("dobMonth", dobMonth)
                intent.putExtra("dobYear", dobYear)
                intent.putExtra("state", state)
                intent.putExtra("city", city)
                intent.putExtra("userType", selectedUserType)
                intent.putExtra("gender", selectedGender)

                startActivity(intent)
                finish()
            } else {
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                otpMessage.text = "Incorrect OTP. Please try again."
            }
        }


        resendButton.setOnClickListener {
            sendNewOtp()
        }

        backButton.setOnClickListener {
            finish() // Go back to SignUp page
        }


    }

    private fun startTimer() {
        timer?.cancel() // Cancel any existing timer
        timer = object : CountDownTimer(2 * 60 * 1000, 1000) { // 2 minutes
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                otpMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                otpMessage.text = "OTP expired. Please resend OTP."
                resendButton.visibility = Button.VISIBLE
                backButton.visibility = Button.VISIBLE
            }
        }.start()
    }

    private fun sendNewOtp() {
        otpMessage.setTextColor(getColor(android.R.color.black))
        otpMessage.text = "Sending new OTP..."
        resendButton.visibility = Button.GONE
        backButton.visibility = Button.GONE


        thread {
            try {
                val newOtp = (1000..9999).random().toString()
                currentOtp = newOtp
                otpExpiryTime = System.currentTimeMillis() + 2 * 60 * 1000 // 2 minutes
                val sender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                sender.sendEmail(
                    toEmail = userEmail,
                    subject = "New OTP",
                    body = "Your new OTP is: $newOtp"
                )
                runOnUiThread {
                    otpMessage.setTextColor(getColor(android.R.color.holo_green_dark))
                    otpMessage.text = "New OTP sent successfully!"
                    startTimer()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    otpMessage.setTextColor(getColor(android.R.color.holo_red_dark))
                    otpMessage.text = "Failed to resend OTP."
                    resendButton.visibility = Button.VISIBLE
                    backButton.visibility = Button.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

}


