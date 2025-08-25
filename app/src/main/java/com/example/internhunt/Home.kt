package com.example.internhunt

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import androidx.gridlayout.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore

import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.firestoreSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.SetOptions



class Home : AppCompatActivity() {

    private lateinit var userImageView: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var logoutButton: TextView
    private lateinit var userImageView2: ImageView
    private lateinit var usernameText: TextView
    private lateinit var profile_drawer: GridLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomSheet: LinearLayout
    private lateinit var dragLine: View
    private lateinit var filterSection: FrameLayout
    private lateinit var bottomSheetOverlay : FrameLayout

    // Define checkbox variables at class level
    private lateinit var checkFullTime: CheckBox
    private lateinit var checkPartTime: CheckBox
    private lateinit var checkOnSite: CheckBox
    private lateinit var checkWorkFromHome: CheckBox
    private lateinit var checkHybrid: CheckBox

    private lateinit var applyButton: TextView
    private lateinit var clearButton: TextView

    private lateinit var adapter: InternshipAdapter
    private val internshipList = ArrayList<InternshipPostData>()  // master list
    private val filteredList = ArrayList<InternshipPostData>()    // list to show on RecyclerView

    private lateinit var noDataText: TextView
    private lateinit var search_bar: EditText






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }





        userImageView = findViewById(R.id.UserProfileImage)
        drawerLayout = findViewById(R.id.drawer_layout)
        userImageView2 = findViewById(R.id.UserProfileImage2)
        logoutButton = findViewById(R.id.LogoutButton)
        usernameText = findViewById(R.id.UserName)
        profile_drawer = findViewById(R.id.profile_drawer)
        recyclerView = findViewById(R.id.recyclerViewHome)
        bottomSheet = findViewById(R.id.bottom_sheet)
        dragLine = findViewById(R.id.drag_line)
        filterSection = findViewById(R.id.filter_section)
        bottomSheetOverlay = findViewById(R.id.bottom_sheet_overlay)

        checkFullTime = findViewById(R.id.checkFullTime)
        checkPartTime = findViewById(R.id.checkPartTime)
        checkOnSite = findViewById(R.id.checkOnSite)
        checkWorkFromHome = findViewById(R.id.checkWorkFromHome)
        checkHybrid = findViewById(R.id.checkHybrid)

        applyButton = findViewById(R.id.applyButton)
        clearButton = findViewById(R.id.clearButton)
        noDataText = findViewById(R.id.noDataText)
        search_bar = findViewById(R.id.search_bar)


        // Get Session
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)
        val role = prefs.getString("role", null)?.trim()?.lowercase()

        // Check if session exists
        if (userId == null || role == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        when (role) {
            "company" -> {
                if (this !is CompanyHomePage) { // Prevent reopening the same page
                    startActivity(Intent(this, CompanyHomePage::class.java))
                    finish()
                }
            }
            "student" -> {
                if (this !is Home) { // Prevent reopening same page
                    startActivity(Intent(this, Home::class.java))
                    finish()
                }
            }
            else -> {
                Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }


        // Fetch user from Firestore
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)




        adapter = InternshipAdapter(filteredList) { post, companyName, imageUrl ->
            val intent = Intent(this, InternshipDetails::class.java)
            intent.putExtra("id", post.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupBottomSheetListeners()


        applyButton.setOnClickListener {
            val selectedTimes = mutableListOf<String>()
            val selectedTypes = mutableListOf<String>()

            if (checkFullTime.isChecked) selectedTimes.add("Full Time")
            if (checkPartTime.isChecked) selectedTimes.add("Part Time")

            if (checkOnSite.isChecked) selectedTypes.add("On Site")
            if (checkWorkFromHome.isChecked) selectedTypes.add("Work from Home")
            if (checkHybrid.isChecked) selectedTypes.add("Hybrid")

            val result = internshipList.filter { post ->
                (selectedTimes.isEmpty() || selectedTimes.any { it.equals(post.internshipTime, ignoreCase = true) }) &&
                        (selectedTypes.isEmpty() || selectedTypes.any { it.equals(post.internshipType, ignoreCase = true) })
            }

            filteredList.clear()
            filteredList.addAll(result)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Filter Applied", Toast.LENGTH_SHORT).show()
            noDataText.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE

            bottomSheet.animate()
                .translationY(bottomSheet.height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    bottomSheet.visibility = View.GONE
                    bottomSheetOverlay.visibility = View.GONE
                }
                .start()
        }


        clearButton.setOnClickListener {
            checkFullTime.isChecked = false
            checkPartTime.isChecked = false
            checkOnSite.isChecked = false
            checkWorkFromHome.isChecked = false
            checkHybrid.isChecked = false

            filteredList.clear()
            filteredList.addAll(internshipList)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Filter Clear", Toast.LENGTH_SHORT).show()
            noDataText.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE

            bottomSheet.animate()
                .translationY(bottomSheet.height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    bottomSheet.visibility = View.GONE
                    bottomSheetOverlay.visibility = View.GONE
                }
                .start()
        }



        saveFcmToken(userId)



        userImageView.setOnClickListener {
            hideKeyboard(search_bar)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        profile_drawer.setOnClickListener {
            var intent = Intent(this, Profile::class.java)
            hideKeyboard(search_bar)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            val intent = intent
            hideKeyboard(search_bar)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }


        findViewById<TextView>(R.id.nav_security).setOnClickListener {
            var intent = Intent(this, Security::class.java)
            hideKeyboard(search_bar)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.nav_add_post).setOnClickListener {
            var intent = Intent(this, JobPost::class.java)
            hideKeyboard(search_bar)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.bookmark).setOnClickListener {
            var intent = Intent(this, Bookmark::class.java)
            hideKeyboard(search_bar)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.history).setOnClickListener {
            var intent = Intent(this, AppliedInternship::class.java)
            hideKeyboard(search_bar)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.notification).setOnClickListener {
            hideKeyboard(search_bar)
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.setting).setOnClickListener {
            hideKeyboard(search_bar)
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.help).setOnClickListener {
            hideKeyboard(search_bar)
            // Handle navigation to Update Details screen
        }

        findViewById<LinearLayout>(R.id.BottomHomeButton).setOnClickListener {
            hideKeyboard(search_bar)
            refreshHomePage()
        }

        findViewById<LinearLayout>(R.id.BottomCompanyButton).setOnClickListener {
            var intent = Intent(this, CompanyLists::class.java)
            hideKeyboard(search_bar)
            startActivity(intent)
        }


        findViewById<LinearLayout>(R.id.BottomBookmarkButton).setOnClickListener {
            var intent = Intent(this, Bookmark::class.java)
            hideKeyboard(search_bar)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.BottomSearchButton).setOnClickListener {
            search_bar = findViewById(R.id.search_bar)
            search_bar.requestFocus()

            // Open the keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(search_bar, InputMethodManager.SHOW_IMPLICIT)
        }



        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish() // Or use super.onBackPressed() if needed
            }
        }




        var userDegrees: List<String> = emptyList()
        var userSkills: List<String> = emptyList()

        userRef.addSnapshotListener { doc, error ->
            if (error != null) {
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (doc != null && doc.exists()) {

                val imageUrl = doc.getString("profile_image_url")
                if (!isFinishing && !isDestroyed) {
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(userImageView)

                        Glide.with(this)
                            .load(imageUrl)
                            .into(userImageView2)
                    }
                }
                // Get the education list as a List of Map<String, Any>
                val educationList = doc.get("education") as? List<Map<String, Any>> ?: emptyList()
                userDegrees = educationList.mapNotNull { it["degree_name"] as? String }

                val skillList = doc.get("skill") as? List<String> ?: emptyList()

                val hide_post = doc.get("hide_post") as? List<String> ?: emptyList()
                Log.d("hide_post", hide_post.toString())

                LoadInternshipPost(userDegrees, skillList, hide_post)


                val userName = doc.getString("username")
                val companyName = doc.getString("company_name")
                val role = doc.getString("role")

                if (role == "Student") {
                    if (!userName.isNullOrEmpty()) {
                        usernameText.text = userName
                    }
                    findViewById<TextView>(R.id.nav_add_post).visibility = View.GONE
                }
                else if(role == "Company"){
                    if (!companyName.isNullOrEmpty()){
                        usernameText.text = companyName
                    }

                    findViewById<TextView>(R.id.bookmark).visibility = View.GONE
                    findViewById<TextView>(R.id.history).visibility = View.GONE
                }
                else{
                    usernameText.text = "Guest"
                }

            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout action
        logoutButton.setOnClickListener {
            prefs.edit().clear().apply() // Clear session
            val intent = Intent(this, Login::class.java)

            // Clears all the previous activities in the back stack (user can’t press "Back" to go to Home again)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }


        search_bar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val query = search_bar.text.toString().trim()
                if (query.isNotEmpty()) {
                    val intent = Intent(this, SearchResult::class.java)
                    intent.putExtra("searchQuery", query)
                    startActivity(intent)
                }
                true
            } else {
                false
            }
        }




    }

    private fun showBottomSheet() {
        bottomSheetOverlay.visibility = View.VISIBLE
        bottomSheet.visibility = View.VISIBLE

        // Start from below screen
        bottomSheet.translationY = bottomSheet.height.toFloat()
        bottomSheet.alpha = 0f

        // Animate up
        bottomSheet.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .start()
    }
    private fun hideBottomSheet() {
        // Animate down
        bottomSheet.animate()
            .translationY(bottomSheet.height.toFloat())
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                bottomSheet.visibility = View.GONE
                bottomSheetOverlay.visibility = View.GONE
            }
            .start()
    }
    private fun setupBottomSheetListeners() {
        // Open bottom sheet
        filterSection.setOnClickListener { showBottomSheet() }

        // Close bottom sheet on drag or outside overlay click
        dragLine.setOnClickListener { hideBottomSheet() }
        bottomSheetOverlay.setOnClickListener { hideBottomSheet() }

        // Prevent closing when clicking inside the sheet
        bottomSheet.setOnClickListener { /* Do nothing */ }

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (bottomSheetOverlay.visibility == View.VISIBLE) {
                    hideBottomSheet()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    finish()
                }
            }

        })
    }


    private fun LoadInternshipPost(userDegrees: List<String>, userSkills: List<String>, hiddenIds: List<String>) {
        val db2 = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
        val today = Date()

        val lowerUserDegrees = userDegrees.map { it.lowercase() }
        val lowerUserSkills = userSkills.map { it.lowercase() }

        db2.collection("internshipPostsData")
            .get()
            .addOnSuccessListener { documents ->
                internshipList.clear()
                for (doc in documents) {
                    val post = doc.toObject(InternshipPostData::class.java)
                    if (hiddenIds.contains(post.id)) continue

                    try {
                        val deadlineDate = dateFormat.parse(post.applicationDeadline)
                        val postDegreesLower = post.degreeEligibility.map { it.lowercase() }
                        val postSkillsLower = post.skillsRequired.map { it.lowercase() }  // <-- here is the fix

                        val matchesDegree = postDegreesLower.any { it in lowerUserDegrees }
                        val matchesSkill = postSkillsLower.any { it in lowerUserSkills }

                        if (deadlineDate != null && deadlineDate.after(today) && (matchesDegree || matchesSkill)) {
                            internshipList.add(post)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                filteredList.clear()
                filteredList.addAll(internshipList)
                adapter.notifyDataSetChanged()
                noDataText.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }



    fun refreshHomePage() {
        recyclerView.scrollToPosition(0)
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val educationList = doc.get("education") as? List<Map<String, Any>> ?: emptyList()
                        val userDegrees = educationList.mapNotNull { it["degree_name"] as? String }
                        val skillList = doc.get("skill") as? List<String> ?: emptyList()
                        val hide_post = doc.get("hide_post") as? List<String> ?: emptyList()

                        LoadInternshipPost(userDegrees, skillList, hide_post) // now skillList is defined
                    }
                }
        }
    }

    private fun saveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                val token = task.result
                val db = FirebaseFirestore.getInstance()

                val data = hashMapOf("fcmToken" to token)

                db.collection("Users").document(userId)
                    .set(data, SetOptions.merge())  // 👈 create if not exist, update if exist
                    .addOnSuccessListener {
                        Log.d("FCM", "Token saved: $token")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Failed to save token", e)
                    }
            }
    }


    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
