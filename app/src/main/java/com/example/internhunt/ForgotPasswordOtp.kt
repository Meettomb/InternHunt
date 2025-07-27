package com.example.internhunt

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.concurrent.thread
import android.os.Handler

class ForgotPasswordOtp : AppCompatActivity() {

    private lateinit var otpInput: EditText
    private lateinit var verifyButton: TextView
    private lateinit var resendButton: TextView
    private lateinit var backButton: TextView
    private lateinit var otpMessage: TextView
    private lateinit var timerText: TextView

    private var currentOtp: String = ""
    private var timer: CountDownTimer? = null
    private lateinit var userEmail: String

    private lateinit var progressBar: ProgressBar

    private var otpExpiryTime: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password_otp)

        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)
        resendButton = findViewById(R.id.resendButton)
        backButton = findViewById(R.id.backButton)
        otpMessage = findViewById(R.id.otpMessage)
        timerText = findViewById(R.id.timerText)

        progressBar = findViewById(R.id.progressBar)


        currentOtp = intent.getStringExtra("otp") ?: ""
        otpExpiryTime = System.currentTimeMillis() + 2 * 60 * 1000
        userEmail = intent.getStringExtra("email") ?: ""


        startTimer()


        verifyButton.setOnClickListener {
            verifyButton.isEnabled = false // Disable immediately

            val currentTime = System.currentTimeMillis()
            val enteredOtp = otpInput.text.toString().trim()

            Handler(Looper.getMainLooper()).postDelayed({
                verifyButton.isEnabled = true // Re-enable after delay
            }, 2000)

            // Check for OTP expiry
            if (currentTime > otpExpiryTime) {
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                otpMessage.text = "OTP has expired. Please request a new one."
                resendButton.visibility = View.VISIBLE
                backButton.visibility = View.VISIBLE
                return@setOnClickListener // Exit here after re-enabling is scheduled
            }

            // Check OTP length
            if (enteredOtp.length != 4) {
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                otpMessage.text = "Please enter a valid 4-digit OTP"
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            if (enteredOtp == currentOtp) {
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                otpMessage.text = "OTP Verified Successfully!"
                progressBar.visibility = View.GONE

                val intent = Intent(this, EnterNewPassword::class.java)
                intent.putExtra("email", userEmail)
                startActivity(intent)
                finish()
            } else {
                progressBar.visibility = View.GONE
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                otpMessage.text = "Invalid OTP. Please try again."
            }
        }


        resendButton.setOnClickListener {
            sendNewOtp()
        }

        backButton.setOnClickListener {
            finish() // Go back to SignUp page
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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