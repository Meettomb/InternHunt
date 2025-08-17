package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.MultiAutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.toString

class EditInternship : AppCompatActivity() {

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
    private lateinit var degreeName: MultiAutoCompleteTextView
    private lateinit var degreeError: TextView


    private val degrees = arrayOf(
        "B.Tech (Bachelor of Technology)",
        "B.E. (Bachelor of Engineering)",
        "B.Sc (Bachelor of Science)",
        "BCA (Bachelor of Computer Applications)",
        "BBA (Bachelor of Business Administration)",
        "B.Com (Bachelor of Commerce)",
        "B.A. (Bachelor of Arts)",
        "M.Tech (Master of Technology)",
        "M.E. (Master of Engineering)",
        "M.Sc (Master of Science)",
        "MCA (Master of Computer Applications)",
        "MBA (Master of Business Administration)",
        "M.Com (Master of Commerce)",
        "PhD (Doctor of Philosophy)",
        "Diploma in Engineering",
        "Polytechnic Diploma",
        "B.Ed (Bachelor of Education)",
        "M.Ed (Master of Education)",
        "LLB (Bachelor of Laws)",
        "LLM (Master of Laws)",
        "MBBS (Bachelor of Medicine & Surgery)",
        "BDS (Bachelor of Dental Surgery)",
        "B.Pharm (Bachelor of Pharmacy)",
        "M.Pharm (Master of Pharmacy)",
        "B.Arch (Bachelor of Architecture)",
        "M.Arch (Master of Architecture)"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_internship)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

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

        degreeName = findViewById(R.id.DegreeName)
        degreeError = findViewById(R.id.degreeError)

        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_item,
            R.id.text1,
            degrees
        )

        degreeName = findViewById(R.id.DegreeName)
        degreeName.setAdapter(adapter)

        // Set tokenizer to comma (you can customize)
        degreeName.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        degreeName.threshold = 1


        backButton.setOnClickListener {
            onBackPressed()
        }


        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        // Check if session exists
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        companyId.setText(userId)

        val internshipId = intent.getStringExtra("id") ?: ""
        if (internshipId != null){
            Log.d("TAG", "internshipId: $internshipId")
        }

        loadInternshipData(internshipId)
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

            val degreeInput = degreeName.text.toString().trim()

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

            // Validate Degree Name
            if (degreeInput.isEmpty()) {
                degreeError.text = "Degree is required"
                degreeError.visibility = TextView.VISIBLE
                isValid = false
            } else {
                // Split input by commas, trim spaces, filter out empty strings
                val enteredDegrees = degreeInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                // Check if every entered degree is in the allowed degrees list
                val invalidDegrees = enteredDegrees.filter { it !in degrees }

                if (invalidDegrees.isNotEmpty()) {
                    degreeError.text = "Please select degrees from the list provided."
                    degreeError.visibility = TextView.VISIBLE
                    isValid = false
                } else {
                    degreeError.visibility = TextView.GONE
                }
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


            val day = dayStr.toInt()
            val month = monthStr.toInt()
            val year = yearStr.toInt()

            val selectedCal = Calendar.getInstance()
            selectedCal.set(year, month - 1, day) // Month is 0-based in Calendar

            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            // Check if date is in the past
            if (selectedCal.before(today)) {
                dateError.visibility = View.VISIBLE
                dateError.text = "Cannot select a past date"
                isValid = false
            }

            // Check if date is more than 4 months ahead
            val maxDate = Calendar.getInstance()
            maxDate.add(Calendar.MONTH, 4)
            if (selectedCal.after(maxDate)) {
                dateError.visibility = View.VISIBLE
                dateError.text = "Date cannot be more than 4 months from today"
                isValid = false
            }


            if (isValid) {
                updatePostToFirestore(userId.toString())
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


    private fun loadInternshipData(internshipId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("internshipPostsData").document(internshipId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data

                    internshipTitle.setText(data?.get("title") as? String ?: "")
                    description.setText(data?.get("description") as? String ?: "")
                    location.setText(data?.get("location") as? String ?: "")
                    stipend.setText(data?.get("stipend") as? String ?: "")
                    duration.setText(data?.get("duration") as? String ?: "")
                    companyId.setText(data?.get("companyId") as? String ?: "")
                    opening.setText(data?.get("openings") as? String ?: "")

                    // Internship Type / Time
                    internshipTypeDropdown.setText(data?.get("internshipType") as? String ?: "", false)
                    internshipTimeDropdown.setText(data?.get("internshipTime") as? String ?: "", false)

                    // Responsibilities (array)
                    val responsibilitiesList = data?.get("responsibilities") as? List<String>
                    responsibilities.setText(responsibilitiesList?.joinToString(", ") ?: "")

                    // Skills (array)
                    val skillsList = data?.get("skillsRequired") as? List<String>
                    skills.setText(skillsList?.joinToString(", ") ?: "")

                    // Degree Eligibility (array) -> MultiAutoCompleteTextView
                    val degreesList = data?.get("degreeEligibility") as? List<String>
                    degreeName.setText(degreesList?.joinToString(", ") ?: "")

                    // Perks (array) -> TextView (if you are displaying selected perks as text)
                    val perksList = data?.get("perks") as? List<String>
                    perksDropdown.text = perksList?.joinToString(", ") ?: ""

                    // Application Deadline (string) -> split to date, month, year
                    val deadlineStr = data?.get("applicationDeadline") as? String
                    if (!deadlineStr.isNullOrEmpty()) {
                        val parts = deadlineStr.split("/")
                        if (parts.size == 3) {
                            date.setText(parts[0])
                            month.setText(parts[1])
                            year.setText(parts[2])
                        }
                    }

                    // Hide progress bar after loading
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load internship data: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }
    private fun updatePostToFirestore(userId: String) {
        postButton.isEnabled = false
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
        val degreesText = degreeName.text.toString().trim()
        val degreesList = degreesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }


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
            "degreeEligibility" to degreesList,
            "status" to true
        )

        // Save to Firestore
        newUserRef.set(postData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Internship posted successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Profile::class.java))
                finish()
                postButton.isEnabled = true
            }
            .addOnFailureListener { e ->
                postButton.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to post internship: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }



}