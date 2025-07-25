package com.example.internhunt

import InputFilterMinMax
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.concurrent.thread
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.MotionEvent
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import okhttp3.OkHttpClient
import java.util.Calendar

import android.widget.ArrayAdapter
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest


class SignUp : AppCompatActivity() {

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var birthDate: EditText
    private lateinit var birthMonth: EditText
    private lateinit var birthYear: EditText
    private lateinit var signUpButton: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var usernameError: TextView
    private lateinit var emailError: TextView
    private lateinit var phoneError: TextView
    private lateinit var birthDateError: TextView
    private lateinit var birthMonthError: TextView
    private lateinit var birthYearError: TextView

    private lateinit var stateError: TextView
    private lateinit var cityError: TextView
    private lateinit var passwordError: TextView
    private lateinit var confirmPasswordError: TextView
    private lateinit var radioGroupbutton: RadioGroup
    private lateinit var radioError: TextView

    private lateinit var genderRadioButton: RadioGroup
    private lateinit var genderRadioGroupError: TextView

    // For state API
    private lateinit var stateSpinner: Spinner
    private lateinit var cityText: EditText
    private val client = OkHttpClient()


    private lateinit var textViewLogin: TextView

    private var isPasswordVisible = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)


        username = findViewById(R.id.Username)
        email = findViewById(R.id.Email)
        phone = findViewById(R.id.Phone)
        password = findViewById(R.id.Password)
        confirmPassword = findViewById(R.id.Confirm_Password)
        birthDate = findViewById(R.id.BirthDate)
        birthMonth = findViewById(R.id.BirthMonth)
        birthYear = findViewById(R.id.BirthYear)
        signUpButton = findViewById(R.id.SignUpButton)
        progressBar = findViewById(R.id.progressBar)
        usernameError = findViewById(R.id.UsernameError)
        emailError = findViewById(R.id.EmailError)
        phoneError = findViewById(R.id.PhoneError)
        birthDateError = findViewById(R.id.BirthDateError)
        birthMonthError = findViewById(R.id.BirthMonthError)
        birthYearError = findViewById(R.id.BirthYearError)
        stateError = findViewById(R.id.StateError)
        cityError = findViewById(R.id.CityError)
        passwordError = findViewById(R.id.PasswordError)
        confirmPasswordError = findViewById(R.id.ConfirmPasswordError)

        togglePasswordVisibility(password)
        togglePasswordVisibility(confirmPassword)

        radioGroupbutton = findViewById(R.id.radioGroup1)
        radioError = findViewById(R.id.RadioError)

        genderRadioButton = findViewById(R.id.genderRadioGroup1)
        genderRadioGroupError = findViewById(R.id.GenderRadioError)

        stateSpinner = findViewById(R.id.stateSpinner)
        fetchStatesFromAPI()

        cityText = findViewById(R.id.City)

        textViewLogin = findViewById(R.id.textViewLogin)

        textViewLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }


        var selectedState = ""

        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedState = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@SignUp, "Selected: $selectedState", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }




        signUpButton.setOnClickListener {
            val userEmail = email.text.toString().trim()
            val userName = username.text.toString().trim()
            val userPhone = phone.text.toString().trim()
            val userPassword = password.text.toString().trim()
            val userConfirmPassword = confirmPassword.text.toString().trim()
            val dobDate = birthDate.text.toString().trim()
            val dobMonth = birthMonth.text.toString().trim()
            val dobYear = birthYear.text.toString().trim()
            val state = stateSpinner.selectedItem.toString().trim()
            val city = cityText.text.toString().trim()



            var valid = true

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            if (userName.isEmpty()) {
                username.error = "Required"
                username.setBackgroundResource(R.drawable.border_error)
                usernameError.visibility = View.VISIBLE
                valid = false
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                email.error = "Invalid email"
                email.setBackgroundResource(R.drawable.border_error)
                email.requestFocus()
                emailError.visibility = View.VISIBLE
                valid = false
            }

            if (userPhone.isEmpty()) {
                phone.setBackgroundResource(R.drawable.border_error)
                phoneError.text = "Phone is required"
                phoneError.visibility = View.VISIBLE
                valid = false
            } else if (userPhone.length != 10) {
                phone.setBackgroundResource(R.drawable.border_error)
                phoneError.text = "Phone must be 10 digits"
                phoneError.visibility = View.VISIBLE
                valid = false
            }

            if (dobDate.isEmpty()) {
                birthDate.setBackgroundResource(R.drawable.border_error)
                birthDateError.text = "Date is required"
                birthDateError.visibility = View.VISIBLE
                valid = false
            }

            if (dobMonth.isEmpty()) {
                birthMonth.setBackgroundResource(R.drawable.border_error)
                birthMonthError.text = "Month is required"
                birthMonthError.visibility = View.VISIBLE
                birthMonth.filters = arrayOf(InputFilterMinMax(1, 12))

                valid = false
            } else {
                val month = dobMonth.toIntOrNull()
                if (month == null || month !in 1..12) {
                    birthMonth.setBackgroundResource(R.drawable.border_error)
                    birthMonthError.text = "Month must be between 1 and 12"
                    birthMonthError.visibility = View.VISIBLE
                    valid = false
                }
            }

            if (dobYear.isEmpty()) {
                birthYear.setBackgroundResource(R.drawable.border_error)
                birthYearError.text = "Year is required"
                birthYearError.visibility = View.VISIBLE
                valid = false
            } else {
                val year = dobYear.toIntOrNull()
                if (year == null || year !in 1900..currentYear) {
                    birthYear.setBackgroundResource(R.drawable.border_error)
                    birthYearError.text = "Year must be between 1900 and $currentYear"
                    birthYearError.visibility = View.VISIBLE
                    valid = false
                }
            }

            // Check date only if month and year are valid
            val date = dobDate.toIntOrNull()
            val month = dobMonth.toIntOrNull()
            val year = dobYear.toIntOrNull()

            if (date != null && month != null && year != null) {
                try {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month - 1) // Month is 0-based in Calendar
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    if (date !in 1..maxDay) {
                        birthDate.setBackgroundResource(R.drawable.border_error)
                        birthDateError.text = "Date must be between 1 and $maxDay"
                        birthDateError.visibility = View.VISIBLE
                        valid = false
                    }
                } catch (e: Exception) {
                    birthDate.setBackgroundResource(R.drawable.border_error)
                    birthDateError.text = "Invalid date"
                    birthDateError.visibility = View.VISIBLE
                    valid = false
                }
            }

            if (state == "Select State") {
                stateSpinner.setBackgroundResource(R.drawable.border_error)
                stateError.visibility = View.VISIBLE
                valid = false
            }
            if (city.isEmpty()) {
                cityText.error = "Required"
                cityText.setBackgroundResource(R.drawable.border_error)
                cityError.visibility = View.VISIBLE
                valid = false
            }


            if (userPassword.isEmpty()) {
                password.error = "Required"
                password.setBackgroundResource(R.drawable.border_error)
                valid = false
            }
            if (userConfirmPassword.isEmpty()) {
                confirmPassword.error = "Required"
                confirmPassword.setBackgroundResource(R.drawable.border_error)
                passwordError.visibility = View.VISIBLE
                valid = false
            } else if (userConfirmPassword != userPassword) {
                confirmPassword.error = "Password mismatch"
                confirmPassword.setBackgroundResource(R.drawable.border_error)
                confirmPasswordError.visibility = View.VISIBLE
                valid = false
            }

            val selectedRadioId = radioGroupbutton.checkedRadioButtonId
            val selectedGenderRadioId = genderRadioButton.checkedRadioButtonId

            var selectedUserType = ""
            var selectedGender = ""

            if (selectedRadioId == -1) {
                radioError.visibility = View.VISIBLE
                valid = false
            } else {
                radioError.visibility = View.GONE
                val radioButton: RadioButton = findViewById(selectedRadioId)
                selectedUserType = radioButton.text.toString()
            }

            if (selectedGenderRadioId == -1) {
                genderRadioGroupError.visibility = View.VISIBLE
                valid = false
            } else {
                genderRadioGroupError.visibility = View.GONE
                val genderButton: RadioButton = findViewById(selectedGenderRadioId)
                selectedGender = genderButton.text.toString()
            }



            if (!valid) return@setOnClickListener

            progressBar.visibility = View.VISIBLE

            val db = FirebaseFirestore.getInstance()

            // Check for existing user by email or phone
            db.collection("Users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { emailDocs ->
                    if (!emailDocs.isEmpty) {
                        progressBar.visibility = View.GONE
                        emailError.text = "Email already exists"
                        emailError.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    db.collection("Users")
                        .whereEqualTo("phone", userPhone)
                        .get()
                        .addOnSuccessListener { phoneDocs ->
                            if (!phoneDocs.isEmpty) {
                                progressBar.visibility = View.GONE
                                phoneError.text = "Phone already exists"
                                phoneError.visibility = View.VISIBLE
                                return@addOnSuccessListener
                            }

                            //  If no existing user, send OTP
                            val otp = (1000..9999).random()
                            thread {
                                try {
                                    val sender = JakartaMailSender("internhunt2@gmail.com", "cayw smpo qwvu terg")
                                    sender.sendEmail(
                                        toEmail = userEmail,
                                        subject = "Verify Your Email Id With OTP",
                                        body = "Your OTP is: $otp"
                                    )
                                    runOnUiThread {
                                        progressBar.visibility = View.GONE
                                        val intent = Intent(this, VerifyOtp::class.java)
                                        intent.putExtra("otp", otp.toString())
                                        intent.putExtra("email", userEmail)
                                        intent.putExtra("username", userName)
                                        intent.putExtra("phone", userPhone)

                                        val hashedPassword = hashPassword(userPassword)
                                        intent.putExtra("password", hashedPassword)

                                        intent.putExtra("dobDate", dobDate)
                                        intent.putExtra("dobMonth", dobMonth)
                                        intent.putExtra("dobYear", dobYear)
                                        intent.putExtra("state", state)
                                        intent.putExtra("city", city)
                                        intent.putExtra("userType", selectedUserType)
                                        intent.putExtra("gender", selectedGender)
                                        startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    runOnUiThread {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(this, "Failed to send email", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Phone check failed", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Email check failed", Toast.LENGTH_LONG).show()
                }

        }

        // Reset border when user starts typing
        fun resetBorderOnTextChange(editText: EditText, errorTextView: TextView? = null) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    editText.setBackgroundResource(R.drawable.border_all_sides)
                    errorTextView?.visibility = View.GONE
                }
            })
        }

        // Reset border for spinner
        fun resetSpinnerBorderOnChange(spinner: Spinner, errorTextView: TextView? = null) {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    spinner.setBackgroundResource(R.drawable.border_all_sides)
                    errorTextView?.visibility = View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        // Reset RadioGroup error
        fun resetRadioGroupOnChange(radioGroup: RadioGroup, errorTextView: TextView) {
            radioGroup.setOnCheckedChangeListener { _, _ ->
                errorTextView.visibility = View.GONE
            }
        }

        resetBorderOnTextChange(username)
        resetBorderOnTextChange(email)
        resetBorderOnTextChange(phone)
        resetBorderOnTextChange(password)
        resetBorderOnTextChange(confirmPassword)
        resetBorderOnTextChange(birthDate)
        resetBorderOnTextChange(birthMonth)
        resetBorderOnTextChange(birthYear)

        resetBorderOnTextChange(username, usernameError)
        resetBorderOnTextChange(email, emailError)
        resetBorderOnTextChange(phone, phoneError)
        resetBorderOnTextChange(password, passwordError)
        resetBorderOnTextChange(confirmPassword, confirmPasswordError)
        resetBorderOnTextChange(birthDate, birthDateError)
        resetBorderOnTextChange(birthMonth, birthMonthError)
        resetBorderOnTextChange(birthYear, birthYearError)
        resetBorderOnTextChange(cityText, cityError)
        resetRadioGroupOnChange(radioGroupbutton, radioError)
        resetRadioGroupOnChange(genderRadioButton, genderRadioGroupError)



        resetSpinnerBorderOnChange(stateSpinner, stateError)




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    private fun togglePasswordVisibility(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // Index for drawableEnd
                val drawable = editText.compoundDrawables[drawableEnd]

                drawable?.let {
                    val touchAreaStart = editText.right - drawable.bounds.width() - editText.paddingEnd
                    if (event.rawX >= touchAreaStart) {
                        isPasswordVisible = !isPasswordVisible

                        // Toggle input type
                        editText.inputType = if (isPasswordVisible) {
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        } else {
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        }
                        editText.setSelection(editText.text.length)

                        // Toggle icon
                        val iconRes = if (isPasswordVisible) R.drawable.visibility_off else R.drawable.visibility
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, iconRes), null)

                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }


    private fun fetchStatesFromAPI() {
        val request = Request.Builder()
            .url("https://country-state-city-search-rest-api.p.rapidapi.com/states-by-countrycode?countrycode=IN")
            .get()
            .addHeader("x-rapidapi-key", "3f03d9797emsh73ca4574bd5e4d8p18d1c3jsn45f14e7f6f51")
            .addHeader("x-rapidapi-host", "country-state-city-search-rest-api.p.rapidapi.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val stateNames = mutableListOf<String>()

                    // 🟡 Add "Select State" as the first item
                    stateNames.add("Select State")

                    val jsonArray = JSONArray(responseBody)

                    for (i in 0 until jsonArray.length()) {
                        val stateObj = jsonArray.getJSONObject(i)
                        stateNames.add(stateObj.getString("name"))
                    }

                    runOnUiThread {
                        val adapter = ArrayAdapter(
                            this@SignUp,
                            R.layout.spinner_selected_item,
                            stateNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        stateSpinner.adapter = adapter
                    }
                }
            }
        })
    }

}

