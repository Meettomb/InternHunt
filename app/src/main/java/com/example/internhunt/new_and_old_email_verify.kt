package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.concurrent.timer

class new_and_old_email_verify : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var currentOtpEditText: EditText
    private lateinit var currentOtpError: TextView
    private lateinit var newOtpEditText: EditText
    private lateinit var newOtpError: TextView
    private lateinit var timerText: TextView
    private lateinit var timerError: TextView
    private lateinit var verifyBtn: TextView
    private lateinit var resendOtpBtn: TextView
    private lateinit var otpSentMessage: TextView
    private lateinit var otpVerifyMessage: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var newEmail: String
    private var newEmailOtp: Int = 0
    private lateinit var currentEmail: String
    private var currentEmailOtp: Int = 0
    private lateinit var userId: String
    private var timer: CountDownTimer? = null

    private var otpExpiryTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_and_old_email_verify)

        // Status bar setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Get session userId
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = prefs.getString("userid", null) ?: ""
        if (userId.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        // Bind views
        backButton = findViewById(R.id.backButton)
        currentOtpEditText = findViewById(R.id.currentOtpEditText)
        currentOtpError = findViewById(R.id.currentOtpError)
        newOtpEditText = findViewById(R.id.newOtpEditText)
        newOtpError = findViewById(R.id.newOtpError)
        timerText = findViewById(R.id.timerText)
        timerError = findViewById(R.id.timerError)
        verifyBtn = findViewById(R.id.verifyBtn)
        resendOtpBtn = findViewById(R.id.resendOtpBtn)
        otpSentMessage = findViewById(R.id.otpSentMessage)
        otpVerifyMessage = findViewById(R.id.otpVerifyMessage)
        progressBar = findViewById(R.id.progressBar)

        backButton.setOnClickListener { finish() }

        // Get intent extras
        newEmail = intent.getStringExtra("newEmail") ?: ""
        newEmailOtp = intent.getIntExtra("newEmailOtp", 0)
        currentEmail = intent.getStringExtra("currentEmail") ?: ""
        currentEmailOtp = intent.getIntExtra("currentEmailOtp", 0)
        otpExpiryTime = System.currentTimeMillis() + 2 * 60 * 1000

        startTimer()

        Log.d("OTPVerify", "New Email: $newEmail, OTP: $newEmailOtp")
        Log.d("OTPVerify", "Current Email: $currentEmail, OTP: $currentEmailOtp")
        Log.d("OTPVerify", "UserId: $userId")

        verifyBtn.setOnClickListener {
            hideKeyboard(currentOtpEditText)
            hideKeyboard(newOtpEditText)

            val currentOtp = currentOtpEditText.text.toString()
            val newOtp = newOtpEditText.text.toString()

            // Validate OTPs
            var hasError = false

            if (currentOtp.isEmpty()) {
                currentOtpError.visibility = View.VISIBLE
                currentOtpError.text = "Please enter the current OTP"
                hasError = true
            } else if (currentOtp.length != 4) {
                currentOtpError.visibility = View.VISIBLE
                currentOtpError.text = "OTP must be 4 digits"
                hasError = true
            } else {
                currentOtpError.visibility = View.GONE
                currentOtpError.text = null
            }

            if (newOtp.isEmpty()) {
                newOtpError.visibility = View.VISIBLE
                newOtpError.text = "Please enter the new OTP"
                hasError = true
            } else if (newOtp.length != 4) {
                newOtpError.visibility = View.VISIBLE
                newOtpError.text = "OTP must be 4 digits"
                hasError = true
            } else {
                newOtpError.visibility = View.GONE
                newOtpError.text = null
            }

            if (!hasError) {
                verifyOtp()
            }
        }

        resendOtpBtn.setOnClickListener {
            resendOtp()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun verifyOtp() {
        progressBar.visibility = View.VISIBLE
        val currentOtp = currentOtpEditText.text.toString()
        val newOtp = newOtpEditText.text.toString()
        val currentTime = System.currentTimeMillis()

        if (currentTime > otpExpiryTime) {
            progressBar.visibility = View.GONE
            timerError.visibility = View.VISIBLE
            timerError.text = "OTP has expired. Please request a new one."
            resendOtpBtn.visibility = View.VISIBLE
            return
        }

        timerError.visibility = View.GONE

        if (currentOtp == currentEmailOtp.toString() && newOtp == newEmailOtp.toString()) {


            val db = FirebaseFirestore.getInstance()
            db.collection("Users").document(userId).update("email", newEmail)
                .addOnSuccessListener {
                    hideKeyboard(currentOtpEditText)
                    hideKeyboard(newOtpEditText)
                    progressBar.visibility = View.GONE
                    timer?.cancel()
                    otpVerifyMessage.visibility = View.VISIBLE
                    Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()

                    // Redirect to login page and clear the back stack
                    val intent = Intent(this, Security::class.java)
                    startActivity(intent)
                    finish()
                }


                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    hideKeyboard(currentOtpEditText)
                    hideKeyboard(newOtpEditText)
                }
        } else {
            progressBar.visibility = View.GONE
            if (currentOtp != currentEmailOtp.toString()) {
                currentOtpError.visibility = View.VISIBLE
                currentOtpError.text = "Invalid OTP"
            }
            else{
                currentOtpError.visibility = View.GONE
            }
            if (newOtp != newEmailOtp.toString()) {
                newOtpError.visibility = View.VISIBLE
                newOtpError.text = "Invalid OTP"
            }
            else{
                newOtpError.visibility = View.GONE
            }
        }
    }


    private fun resendOtp() {
        currentOtpEditText.text.clear()
        newOtpEditText.text.clear()
        currentOtpError.visibility = View.GONE
        newOtpError.visibility = View.GONE

        timerError.visibility = View.GONE
        otpSentMessage.text = "Sending new OTP..."
        resendOtpBtn.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        // Update OTPs
        currentEmailOtp = (1000..9999).random()
        newEmailOtp = (1000..9999).random()
        otpExpiryTime = System.currentTimeMillis() + 2 * 60 * 1000

        startTimer()

        Thread {
            try {
                val currentEmailSender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                currentEmailSender.sendEmail(
                    toEmail = currentEmail,
                    subject = "Verify Your Email Id With OTP",
                    body = "Your OTP is: $currentEmailOtp"
                )

                val newEmailSender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                newEmailSender.sendEmail(
                    toEmail = newEmail,
                    subject = "Verify Your Email Id With OTP",
                    body = "Your OTP is: $newEmailOtp"
                )

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    otpSentMessage.text = "New OTP sent successfully!"
                    timerError.visibility = View.GONE
                    timerText.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to send OTP emails", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
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
                timerError.setTextColor(ContextCompat.getColor(this@new_and_old_email_verify, android.R.color.holo_red_dark))
                timerError.text = "OTP expired. Please resend OTP."
                resendOtpBtn.visibility = View.VISIBLE
            }
        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }


    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
