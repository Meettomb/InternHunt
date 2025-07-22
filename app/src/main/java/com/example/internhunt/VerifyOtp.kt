package com.example.internhunt

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_otp)

        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)
        resendButton = findViewById(R.id.resendButton)
        backButton = findViewById(R.id.backButton)
        otpMessage = findViewById(R.id.otpMessage)
        timerText = findViewById(R.id.timerText)

        currentOtp = intent.getStringExtra("otp") ?: ""
        userEmail = intent.getStringExtra("email") ?: ""

        startTimer()
        // Get OTP from intent
        val correctOtp = intent.getStringExtra("otp")

        verifyButton.setOnClickListener {
            val enteredOtp = otpInput.text.toString().trim()

            if (enteredOtp == correctOtp) {
                otpMessage.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                otpMessage.text = "OTP Verified Successfully!"

                val intent = Intent(this, MainActivity::class.java)
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


