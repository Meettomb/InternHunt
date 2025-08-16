    package com.example.internhunt

    import android.content.Context
    import android.content.Intent
    import android.os.Build
    import android.os.Bundle
    import android.view.View
    import android.view.inputmethod.InputMethodManager
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
    import com.google.firebase.firestore.FieldValue
    import com.google.firebase.firestore.FirebaseFirestore
    import java.util.Calendar

    class ProjectAdd : AppCompatActivity() {

        private lateinit var backButton: ImageView
        private lateinit var etProjectTitle: EditText
        private lateinit var etDescription: EditText
        private lateinit var etTechnologies: EditText
        private lateinit var etLink: EditText

        private lateinit var tvErrorTitle: TextView
        private lateinit var tvErrorDescription: TextView
        private lateinit var tvErrorTechnologies: TextView
        private lateinit var tvErrorLink: TextView
        private lateinit var btnSave: TextView
        private lateinit var progressBar: ProgressBar
        private lateinit var etStartMonth: EditText
        private lateinit var etStartYear: EditText
        private lateinit var tvDateError: TextView
        private lateinit var etEndMonth: EditText
        private lateinit var etEndYear: EditText
        private lateinit var tvEndDateError: TextView


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // enableEdgeToEdge()
            setContentView(R.layout.activity_project_add)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.card_background)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // For older versions, use a dark color with light icons
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
            }

            // Get session
            val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val userId = prefs.getString("userid", null)

            // Check if session exists
            if (userId == null) {
                Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
                return
            }

            backButton = findViewById(R.id.backButton)
            etProjectTitle = findViewById(R.id.etProjectTitle)
            etDescription = findViewById(R.id.etDescription)
            etTechnologies = findViewById(R.id.etTechnologies)
            etLink = findViewById(R.id.etLink)

            tvErrorTitle = findViewById(R.id.tvErrorTitle)
            tvErrorDescription = findViewById(R.id.tvErrorDescription)
            tvErrorTechnologies = findViewById(R.id.tvErrorTechnologies)
            tvErrorLink = findViewById(R.id.tvErrorLink)
            btnSave = findViewById(R.id.btnSave)
            progressBar = findViewById(R.id.progressBar)
            etStartMonth = findViewById(R.id.etStartMonth)
            etStartYear = findViewById(R.id.etStartYear)
            tvDateError = findViewById(R.id.tvStartDateError)
            etEndMonth = findViewById(R.id.etEndMonth)
            etEndYear = findViewById(R.id.etEndYear)
            tvEndDateError = findViewById(R.id.tvEndDateError)

            backButton.setOnClickListener {
                finish()
            }

            btnSave.setOnClickListener {
                if (validateFields()) {
                    Toast.makeText(this, "Project saved successfully!", Toast.LENGTH_SHORT).show()
                    saveProject(userId)
                    hideKeyboard(etProjectTitle)
                    finish()
                }
            }


            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        private fun validateFields(): Boolean {
            var isValid = true

            // Clear errors first
            tvErrorTitle.visibility = View.GONE
            tvErrorDescription.visibility = View.GONE
            tvErrorTechnologies.visibility = View.GONE
            tvDateError.visibility = View.GONE
            tvEndDateError.visibility = View.GONE
            tvErrorLink.visibility = View.GONE

            val title = etProjectTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val technologies = etTechnologies.text.toString().trim()
            val link = etLink.text.toString().trim()

            val monthStr = etStartMonth.text.toString().trim()
            val yearStr = etStartYear.text.toString().trim()
            val endMonthStr = etEndMonth.text.toString().trim()
            val endYearStr = etEndYear.text.toString().trim()

            // 🔹 Title check
            if (title.isEmpty()) {
                tvErrorTitle.text = "Project Title is required"
                tvErrorTitle.visibility = View.VISIBLE
                isValid = false
            }

            // 🔹 Description check
            if (description.isEmpty()) {
                tvErrorDescription.text = "Description is required"
                tvErrorDescription.visibility = View.VISIBLE
                isValid = false
            }

            // 🔹 Technologies check
            if (technologies.isEmpty()) {
                tvErrorTechnologies.text = "Please enter at least one technology"
                tvErrorTechnologies.visibility = View.VISIBLE
                isValid = false
            }

            // Link validation (optional)
            if (link.isNotEmpty() && !android.util.Patterns.WEB_URL.matcher(link).matches()) {
                tvErrorLink.text = "Invalid link format"
                tvErrorLink.visibility = View.VISIBLE
                isValid = false
            }

            // Start Date validation (Mandatory)
            if (monthStr.isEmpty() || yearStr.isEmpty()) {
                tvDateError.text = "Start date is required"
                tvDateError.visibility = View.VISIBLE
                return false
            }

            val month = monthStr.toIntOrNull()
            val year = yearStr.toIntOrNull()

            if (month == null || year == null || month !in 1..12) {
                tvDateError.text = "Please enter a valid month (1-12) and year"
                tvDateError.visibility = View.VISIBLE
                return false
            }

            val startDate = Calendar.getInstance()
            startDate.set(year, month - 1, 1)

            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            if (startDate.after(today)) {
                tvDateError.text = "Future dates are not allowed"
                tvDateError.visibility = View.VISIBLE
                return false
            }

            tvDateError.visibility = View.GONE

// End Date validation (Optional)
            if (endMonthStr.isNotEmpty() && endYearStr.isNotEmpty()) {
                val endMonth = endMonthStr.toIntOrNull()
                val endYear = endYearStr.toIntOrNull()

                if (endMonth == null || endYear == null || endMonth !in 1..12) {
                    tvEndDateError.text = "Please enter a valid end date"
                    tvEndDateError.visibility = View.VISIBLE
                    return false
                }

                val endDate = Calendar.getInstance()
                endDate.set(endYear, endMonth - 1, 1)

                if (endDate.before(startDate)) {
                    tvEndDateError.text = "End date cannot be before start date"
                    tvEndDateError.visibility = View.VISIBLE
                    return false
                } else if (endDate.after(today)) {
                    tvEndDateError.text = "Future dates are not allowed"
                    tvEndDateError.visibility = View.VISIBLE
                    return false
                } else {
                    tvEndDateError.visibility = View.GONE
                }
            } else {
                tvEndDateError.visibility = View.GONE
            }

            return isValid
        }

        private fun saveProject(userId: String) {
            progressBar.visibility = View.VISIBLE

            val projectTitle = etProjectTitle.text.toString()
            val projectDescription = etDescription.text.toString()
            val projectTechnologiesInput = etTechnologies.text.toString()
            val projectLink = etLink.text.toString()
            val projectStartDate = etStartMonth.text.toString() + "-" +
                    etStartYear.text.toString()

            val projectEndDate = if (etEndMonth.text.toString().isEmpty() && etEndYear.text.toString().isEmpty()) {
                "Present"
            } else {
                etEndMonth.text.toString() + "-" + etEndYear.text.toString()
            }



            // Convert technologies into array (split by comma)
            val technologiesList = projectTechnologiesInput
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val project = hashMapOf(
                "title" to projectTitle,
                "description" to projectDescription,
                "technologies" to technologiesList,
                "startDate" to projectStartDate,
                "end_date" to projectEndDate,
                "link" to projectLink
            )

            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("Users").document(userId)

            // Use FieldValue.arrayUnion() to add new project
            userDocRef.update("projects", FieldValue.arrayUnion(project))
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    hideKeyboard(etProjectTitle)
                    Toast.makeText(this, "Project added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    hideKeyboard(etProjectTitle)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }




        private fun hideKeyboard(view: View) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }