package com.example.internhunt

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.Button
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
    private lateinit var filterSection: LinearLayout
    private lateinit var bottomSheetOverlay : FrameLayout




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
        recyclerView = findViewById(R.id.recyclerView)


        bottomSheet = findViewById(R.id.bottom_sheet)
        dragLine = findViewById(R.id.drag_line)
        filterSection = findViewById(R.id.filter_section)
        bottomSheetOverlay = findViewById(R.id.bottom_sheet_overlay)

        // Show bottom sheet
        filterSection.setOnClickListener {
            bottomSheetOverlay.visibility = View.VISIBLE
            bottomSheet.visibility = View.VISIBLE
        }

        // Close bottom sheet on drag line click
        dragLine.setOnClickListener {
            bottomSheetOverlay.visibility = View.GONE
            bottomSheet.visibility = View.GONE
        }

        // Close when clicking outside the bottom sheet
        bottomSheetOverlay.setOnClickListener {
            bottomSheetOverlay.visibility = View.GONE
            bottomSheet.visibility = View.GONE
        }

        // Prevent outside click closing when clicking on the bottom sheet itself
        bottomSheet.setOnClickListener {
            // Do nothing
        }

        // Handle back press to close the sheet
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomSheetOverlay.visibility == View.VISIBLE) {
                    bottomSheetOverlay.visibility = View.GONE
                    bottomSheet.visibility = View.GONE
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    finish()
                }
            }
        })




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



        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish() // Or use super.onBackPressed() if needed
            }
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

        // Fetch user from Firestore
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)


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



        // get intern ship post data from databse
        val db2 = FirebaseFirestore.getInstance()
        val internshipList = ArrayList<InternshipPostData>()
        val adapter = InternshipAdapter(internshipList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        db2.collection("internshipPostsData")
            .get()
            .addOnSuccessListener { documents ->
                internshipList.clear()
                for (doc in documents){
                    val post = doc.toObject(InternshipPostData::class.java)
                    internshipList.add(post)
                }
                adapter.notifyDataSetChanged()
            }

            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }

    }
}