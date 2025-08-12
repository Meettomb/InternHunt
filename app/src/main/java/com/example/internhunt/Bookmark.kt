package com.example.internhunt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath


class Bookmark : AppCompatActivity() {
    private lateinit var userImageView: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var logoutButton: TextView
    private lateinit var userImageView2: ImageView
    private lateinit var usernameText: TextView
    private lateinit var profile_drawer: GridLayout
    private lateinit var recyclerView: RecyclerView

    private lateinit var bookmarkAdapter: BookmarkAdapter
    private val internshipList = mutableListOf<InternshipPostData>()


    private lateinit var search_bar: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_bookmark)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }


        userImageView = findViewById(R.id.UserProfileImage)
        userImageView2 = findViewById(R.id.UserProfileImage2)
        logoutButton = findViewById(R.id.LogoutButton)
        usernameText = findViewById(R.id.UserName)
        profile_drawer = findViewById(R.id.profile_drawer)
        drawerLayout = findViewById(R.id.drawer_layout)
        search_bar = findViewById(R.id.search_bar)

        recyclerView = findViewById(R.id.recyclerViewBookmark) // your RecyclerView id
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookmarkAdapter = BookmarkAdapter(internshipList) { post, companyName, imageUrl ->
            val intent = Intent(this, InternshipDetails::class.java)
            intent.putExtra("id", post.id)
            startActivity(intent)
        }
        recyclerView.adapter = bookmarkAdapter



        // Get session
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = prefs.getString("userid", null)

        val db = FirebaseFirestore.getInstance()

        // Check if session exists
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        val userRef = db.collection("Users").document(userId)
        // Fetch user from Firestore



        userImageView.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        profile_drawer.setOnClickListener {
            var intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            var intent = Intent(this, Home::class.java)
            startActivity(intent)
        }


        findViewById<TextView>(R.id.nav_security).setOnClickListener {
            // Handle navigation to Update Details screen
        }

        findViewById<TextView>(R.id.nav_add_post).setOnClickListener {
            var intent = Intent(this, JobPost::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.bookmark).setOnClickListener {
            var intent = Intent(this, Bookmark::class.java)
            startActivity(intent)
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
            var intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.BottomCompanyButton).setOnClickListener {
            var intent = Intent(this, CompanyLists::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.BottomBookmarkButton).setOnClickListener {
            loadBookmarkPost(userId, db)
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

                loadBookmarkPost(userId, db)

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




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadBookmarkPost(userId: String, db: FirebaseFirestore) {
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val bookmarkList = userDoc.get("bookmark") as? List<String> ?: emptyList()

                    if (bookmarkList.isEmpty()) {
                        findViewById<TextView>(R.id.noDataText).visibility = View.VISIBLE
                        internshipList.clear()
                        bookmarkAdapter.notifyDataSetChanged()
                    } else {
                        db.collection("internshipPostsData")
                            .whereIn(FieldPath.documentId(), bookmarkList)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val internships = querySnapshot.documents.mapNotNull {
                                    it.toObject(InternshipPostData::class.java)
                                }
                                if (internships.isEmpty()) {
                                    findViewById<TextView>(R.id.noDataText).visibility = View.VISIBLE
                                    internshipList.clear()
                                    bookmarkAdapter.notifyDataSetChanged()
                                } else {
                                    findViewById<TextView>(R.id.noDataText).visibility = View.GONE
                                    internshipList.clear()
                                    internshipList.addAll(internships)
                                    bookmarkAdapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to load bookmarks: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
