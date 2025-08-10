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
import android.view.View
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







        userImageView.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        profile_drawer.setOnClickListener {
            var intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            val intent = intent
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }


        findViewById<TextView>(R.id.nav_security).setOnClickListener {
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.nav_add_post).setOnClickListener {
            var intent = Intent(this, JobPost::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.bookmark).setOnClickListener {
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.history).setOnClickListener {
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.notification).setOnClickListener {
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.setting).setOnClickListener {
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.help).setOnClickListener {
            // Handle navigation to Update Details screen
        }

        findViewById<LinearLayout>(R.id.BottomHomeButton).setOnClickListener {
            refreshHomePage()
        }

        findViewById<LinearLayout>(R.id.BottomCompanyButton).setOnClickListener {
            var intent = Intent(this, CompanyLists::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.BottomSearchButton).setOnClickListener {
            val searchBar = findViewById<EditText>(R.id.search_bar)
            searchBar.requestFocus()

            // Open the keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)
        }



        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish() // Or use super.onBackPressed() if needed
            }
        }




        var userDegrees: List<String> = emptyList()
        userRef.addSnapshotListener { doc, error ->
            if (error != null) {
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (doc != null && doc.exists()) {

                val imageUrl = doc.getString("profile_image_url")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(userImageView)

                    Glide.with(this)
                        .load(imageUrl)
                        .into(userImageView2)
                }
                // Get the education list as a List of Map<String, Any>
                val educationList = doc.get("education") as? List<Map<String, Any>> ?: emptyList()
                userDegrees = educationList.mapNotNull { it["degree_name"] as? String }
                LoadInternshipPost(userDegrees)

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


    private fun LoadInternshipPost(userDegrees: List<String>) {
        val db2 = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
        val today = Date()

        db2.collection("internshipPostsData")
            .get()
            .addOnSuccessListener { documents ->
                internshipList.clear()
                for (doc in documents) {
                    val post = doc.toObject(InternshipPostData::class.java)

                    try {
                         val deadlineDate = dateFormat.parse(post.applicationDeadline)

                        val matchesDegree = post.degreeEligibility.any { degree ->
                            userDegrees.contains(degree)
                        }

                        if (deadlineDate != null && deadlineDate.after(today) && matchesDegree) {
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
        // Scroll RecyclerView to the top
        recyclerView.scrollToPosition(0)

        // Reload data from Firestore
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
                        LoadInternshipPost(userDegrees) // This refreshes the data
                    }
                }
        }
    }





}