package com.example.internhunt

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONArray

class add_skills : AppCompatActivity() {

    private lateinit var skillsContainer: LinearLayout
    private var skillCount = 1 // first skill is already in XML

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_skills)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        skillsContainer = findViewById(R.id.skillsContainer)
        val closeBtn = findViewById<ImageView>(R.id.closeBtn)
        val addMoreSkillBtn = findViewById<Button>(R.id.addMoreSkillBtn)
        val saveSkillsBtn = findViewById<Button>(R.id.saveSkillsBtn)

        // Close Activity
        closeBtn.setOnClickListener {
            hideKeyboard()
            finish()
        }

        // Add More Skill
        addMoreSkillBtn.setOnClickListener {
            skillCount++
            addSkillField()
        }


        // Save Skills
        saveSkillsBtn.setOnClickListener {
            val skillsList = mutableListOf<String>()
            var hasError = false

            val specialCharsRegex = Regex("""[,\.\!@#$%^&*()_+\-=\[\]{};':"\\|<>/?]""")

            for (i in 0 until skillsContainer.childCount) {
                val view = skillsContainer.getChildAt(i)
                if (view is EditText) {
                    val skill = view.text.toString().trim()

                    if (skill.isNotEmpty()) {
                        // Check for special characters or comma/dot
                        if (specialCharsRegex.containsMatchIn(skill)) {
                            view.error = "Please do not use special characters or punctuation"
                            hasError = true
                        } else {
                            skillsList.add(skill)
                            view.error = null
                        }
                    } else {
                        // empty field - no error, just skip adding
                        view.error = null
                    }
                }
            }

            if (hasError) {
                Toast.makeText(this, "Please fix errors before saving.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (skillsList.isNotEmpty()) {
                saveSkills(skillsList)
                setResult(Activity.RESULT_OK, intent)
            } else {
                Toast.makeText(this, "Please enter at least one valid skill.", Toast.LENGTH_SHORT).show()
            }
        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveSkills(newSkillsList: List<String>) {
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = prefs.getString("userid", null)

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("Users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get existing skills or empty list
                    val existingSkills = document.get("skill") as? List<String> ?: emptyList()

                    // Combine existing skills with new skills, avoiding duplicates
                    val combinedSkills = (existingSkills + newSkillsList).distinct()

                    // Update Firestore with combined skills list
                    userDocRef.set(mapOf("skill" to combinedSkills), SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Skills saved successfully!", Toast.LENGTH_SHORT).show()
                            hideKeyboard()

                            finish()
                        }
                        .addOnFailureListener { e ->
                            hideKeyboard()
                            Toast.makeText(this, "Error saving skills: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                } else {
                    // If user doc doesn't exist, just set new skills list
                    userDocRef.set(mapOf("skill" to newSkillsList), SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Skills saved successfully!", Toast.LENGTH_SHORT).show()
                            hideKeyboard()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving skills: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                hideKeyboard()
                Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }



    // Dynamically add new EditText
    private fun addSkillField() {
        val editText = EditText(this)
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 8.toPx(), 0, 0)
        }
        editText.hint = "Enter Skill"
        editText.setTextColor(ContextCompat.getColor(this, R.color.primary_text))
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.hint_text))

        // Convert 10dp and 15dp to pixels
        editText.setPadding(10.toPx(), 15.toPx(), 10.toPx(), 15.toPx())
        editText.background = ContextCompat.getDrawable(this, R.drawable.border_all_sides)
        skillsContainer.addView(editText)
    }

    // Extension function to convert dp to px
    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()


    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }



}