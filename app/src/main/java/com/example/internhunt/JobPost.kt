package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.protobuf.DescriptorProtos
import java.util.Calendar
import androidx.appcompat.app.AlertDialog


class JobPost : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var internshipTitle: EditText
    private lateinit var description: EditText
    private lateinit var location: EditText
    private lateinit var stipend: EditText
    private lateinit var duration: EditText
    private lateinit var companyId: EditText
    private lateinit var skills: EditText
    private lateinit var responsibilities: EditText
    private lateinit var date: EditText
    private lateinit var month: EditText
    private lateinit var year: EditText
    private lateinit var opening: EditText

    private lateinit var internshipTypeDropdown: AutoCompleteTextView
    private lateinit var internshipTimeDropdown: AutoCompleteTextView
    private lateinit var internshipTitleError: TextView
    private lateinit var descriptionError: TextView
    private lateinit var locationError: TextView
    private lateinit var stipendError: TextView
    private lateinit var durationError: TextView
    private lateinit var skillsError: TextView
    private lateinit var dateError: TextView
    private lateinit var monthError: TextView
    private lateinit var yearError: TextView
    private lateinit var openingError: TextView
    private lateinit var internshipTypeDropdownError: TextView
    private lateinit var internshipTimeDropdownError: TextView

    private lateinit var perksDropdown: TextView
    private lateinit var perksError: TextView
    private val selectedPerks = mutableListOf<String>()

    private lateinit var progressBar: ProgressBar

    private lateinit var postButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_post)


        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        backButton = findViewById(R.id.backButton)
        internshipTitle = findViewById(R.id.InternshipTitle)
        description = findViewById(R.id.Description)
        location = findViewById(R.id.Location)
        stipend = findViewById(R.id.Stipend)
        duration = findViewById(R.id.Duration)
        companyId = findViewById(R.id.CompanyId)
        skills = findViewById(R.id.Skills)
        responsibilities = findViewById(R.id.Responsibilities)
        date = findViewById(R.id.Date)
        month = findViewById(R.id.Month)
        year = findViewById(R.id.Year)
        opening = findViewById(R.id.Opening)
        internshipTypeDropdown = findViewById(R.id.internshipType)
        internshipTimeDropdown = findViewById(R.id.internshipTime)

        internshipTitleError = findViewById(R.id.InternshipTitleError)
        descriptionError = findViewById(R.id.DescriptionError)
        locationError = findViewById(R.id.LocationError)
        stipendError = findViewById(R.id.StipendError)
        durationError = findViewById(R.id.DurationError)
        skillsError = findViewById(R.id.skillsError)
        dateError = findViewById(R.id.DateError)
        monthError = findViewById(R.id.MonthError)
        yearError = findViewById(R.id.YearError)
        openingError = findViewById(R.id.OpeningError)
        internshipTypeDropdownError = findViewById(R.id.internshipTypeError)
        internshipTimeDropdownError = findViewById(R.id.internshipTimeError)


        progressBar = findViewById(R.id.progressBar)

        postButton = findViewById(R.id.PostButton)


        backButton.setOnClickListener {
            onBackPressed()
        }

        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        LoadUserdata(userId.toString());

        companyId.setText(userId)

        perksDropdown = findViewById(R.id.perksDropdown)
        perksError = findViewById(R.id.perksError)

        val perksOptions = arrayOf(
            "Internship Certificate",
            "Flexible Working Hours",
            "Work From Home",
            "Free Snacks or Lunch",
            "Letter of Recommendation",
            "Pre-Placement Offer (PPO)",
            "Mentorship by Seniors",
            "Access to Paid Tools or Courses",
            "Networking Opportunities",
            "Team Outings or Events"
        )


        val checkedItems = BooleanArray(perksOptions.size)

        perksDropdown.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Perks")

            builder.setMultiChoiceItems(perksOptions, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedPerks.add(perksOptions[which])
                } else {
                    selectedPerks.remove(perksOptions[which])
                }
            }

            builder.setPositiveButton("OK") { _, _ ->
                perksDropdown.text = if (selectedPerks.isEmpty()) "" else selectedPerks.joinToString(", ")
                perksError.visibility = View.GONE
            }

            builder.setNegativeButton("Cancel", null)

            builder.create().show()
        }


        postButton.setOnClickListener {
            var isValid = true

            var jobtitle = internshipTitle.text.toString()
            var desc = description.text.toString()
            var loc = location.text.toString()
            var stipend = stipend.text.toString()
            var duration = duration.text.toString()
            var skills = skills.text.toString()
            val dayStr = date.text.toString()
            val monthStr = month.text.toString()
            val yearStr = year.text.toString()
            val opening = opening.text.toString()
            var internshipType = internshipTypeDropdown.text.toString()
            var internshipTime = internshipTimeDropdown.text.toString()

            if (jobtitle.isEmpty()) {
                internshipTitleError.visibility = View.VISIBLE
                internshipTitleError.text = "Field cannot be empty"
                isValid = false
            } else {
                internshipTitleError.visibility = View.GONE
            }
            if (desc.isEmpty()) {
                descriptionError.visibility = View.VISIBLE
                descriptionError.text = "Field cannot be empty"
                isValid = false
            } else {
                descriptionError.visibility = View.GONE
            }
            if (loc.isEmpty()) {
                locationError.visibility = View.VISIBLE
                locationError.text = "Field cannot be empty"
                isValid = false
            } else {
                locationError.visibility = View.GONE
            }
            if (stipend.isEmpty()) {
                stipendError.visibility = View.VISIBLE
                stipendError.text = "Field cannot be empty"
                isValid = false
            } else {
                stipendError.visibility = View.GONE
            }
            if (duration.isEmpty()) {
                durationError.visibility = View.VISIBLE
                durationError.text = "Field cannot be empty"
                isValid = false
            } else {
                durationError.visibility = View.GONE
            }
            if (skills.isEmpty()) {
                skillsError.visibility = View.VISIBLE
                skillsError.text = "Field cannot be empty"
                isValid = false
            } else {
                skillsError.visibility = View.GONE
            }




            if (dayStr.isEmpty()) {
                dateError.visibility = View.VISIBLE
                dateError.text = "Day is required"
                isValid = false
            } else {
                dateError.visibility = View.GONE
            }

            if (monthStr.isEmpty()) {
                monthError.visibility = View.VISIBLE
                monthError.text = "Month is required"
                isValid = false
            } else {
                monthError.visibility = View.GONE
            }

            if (yearStr.isEmpty()) {
                yearError.visibility = View.VISIBLE
                yearError.text = "Year is required"
                isValid = false
            } else {
                yearError.visibility = View.GONE
            }

            // Validate full date only if all fields are filled
            if (dayStr.isNotEmpty() && monthStr.isNotEmpty() && yearStr.isNotEmpty()) {
                try {
                    val day = dayStr.toInt()
                    val month = monthStr.toInt()
                    val year = yearStr.toInt()

                    if (day !in 1..31) {
                        dateError.visibility = View.VISIBLE
                        dateError.text = "Invalid day"
                        isValid = false
                    }

                    if (month !in 1..12) {
                        monthError.visibility = View.VISIBLE
                        monthError.text = "Invalid month"
                        isValid = false
                    }

                    if (isValid) {
                        val selectedCal = Calendar.getInstance()
                        selectedCal.set(year, month - 1, day) // Month is 0-based in Calendar

                        val today = Calendar.getInstance()
                        today.set(Calendar.HOUR_OF_DAY, 0)
                        today.set(Calendar.MINUTE, 0)
                        today.set(Calendar.SECOND, 0)
                        today.set(Calendar.MILLISECOND, 0)

                        if (selectedCal.before(today)) {
                            dateError.visibility = View.VISIBLE
                            dateError.text = "Cannot select a past date"
                            isValid = false
                        }
                    }
                } catch (e: Exception) {
                    dateError.visibility = View.VISIBLE
                    dateError.text = "Invalid date"
                    isValid = false
                }
            }

            if (opening.isEmpty()) {
                openingError.visibility = View.VISIBLE
                openingError.text = "Field cannot be empty"
                isValid = false
            } else {
                openingError.visibility = View.GONE
            }


            if (internshipType.isEmpty()) {
                internshipTypeDropdown.error = "Field cannot be empty"
                internshipTypeDropdownError.visibility = View.VISIBLE
                isValid = false
            }
            if (internshipTime.isEmpty()) {
                internshipTimeDropdownError.visibility = View.VISIBLE
                internshipTimeDropdown.error = "Field cannot be empty"
                isValid = false
            }

            if (selectedPerks.isEmpty()) {
                perksError.visibility = View.VISIBLE
                isValid = false
            }


            if (isValid) {
                savePostToFirestore(userId.toString())
            }


        }


        // Dropdown items
        val internshipTypeOptions = listOf("Hybrid", "On Site", "Work From Home")
        val internshipTypeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, internshipTypeOptions)
        internshipTypeDropdown.setAdapter(internshipTypeAdapter)
        // Hide error when user selects internship type
        internshipTypeDropdown.setOnItemClickListener { _, _, _, _ ->
            internshipTypeDropdownError.visibility = View.GONE
            internshipTypeDropdown.error = null
        }

        val internshipTimeOptions = listOf("Full Time", "Part Time")
        val internshipTimeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, internshipTimeOptions)
        internshipTimeDropdown.setAdapter(internshipTimeAdapter)
        // Hide error when user selects internship time
        internshipTimeDropdown.setOnItemClickListener { _, _, _, _ ->
            internshipTimeDropdownError.visibility = View.GONE
            internshipTimeDropdown.error = null
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
        resetBorderOnTextChange(internshipTitle, internshipTitleError)
        resetBorderOnTextChange(description, descriptionError)
        resetBorderOnTextChange(location, locationError)
        resetBorderOnTextChange(stipend, stipendError)
        resetBorderOnTextChange(duration, durationError)
        resetBorderOnTextChange(skills, skillsError)
        resetBorderOnTextChange(date, dateError)
        resetBorderOnTextChange(month, monthError)
        resetBorderOnTextChange(year, yearError)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun savePostToFirestore(userId: String) {

        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val newUserRef = db.collection("internshipPostsData").document()

        val title = internshipTitle.text.toString().trim()
        val desc = description.text.toString().trim()
        val loc = location.text.toString().trim()
        val stipendValue = stipend.text.toString().trim()
        val durationValue = duration.text.toString().trim()
        val skillsList = skills.text.toString().trim().split(",").map { it.trim() }
        val responsibilities = responsibilities.text.toString().trim().split(",").map { it.trim() }
        val day = date.text.toString().toInt()
        val month = month.text.toString().toInt()
        val year = year.text.toString().toInt()
        val opening = opening.text.toString().trim()
        val internshipTypeValue = internshipTypeDropdown.text.toString()
        val internshipTimeValue = internshipTimeDropdown.text.toString()


        // Combine date fields into single string or store as Date object (optional)
        val deadlineDate = "$day/$month/$year"

        // Prepare data
        val postData = hashMapOf(
            "id" to newUserRef.id,
            "title" to title,
            "description" to desc,
            "location" to loc,
            "stipend" to "₹$stipendValue/month",
            "duration" to "$durationValue/month",
            "skillsRequired" to skillsList,
            "responsibilities" to responsibilities,
            "companyId" to userId,
            "postedDate" to com.google.firebase.Timestamp.now(),
            "applicationDeadline" to deadlineDate,
            "openings" to opening,
            "type" to "internship",
            "internshipType" to internshipTypeValue,
            "internshipTime" to internshipTimeValue,
            "perks" to selectedPerks,
            "status" to "open"
        )

        // Save to Firestore
        newUserRef.set(postData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Internship posted successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Profile::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to post internship: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun LoadUserdata(userId: String){
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()){
                    var city = doc.getString("city")
                    var state = doc.getString("state")

                    location.setText("$city, $state")
                }
            }
    }


}