        package com.example.internhunt

        import android.content.Context
        import android.content.Intent
        import android.os.Build
        import android.os.Bundle
        import android.util.Log
        import android.view.View
        import android.widget.CheckBox
        import android.widget.FrameLayout
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

        class CompanyLists : AppCompatActivity() {

            private lateinit var userImageView: ImageView
            private lateinit var drawerLayout: DrawerLayout
            private lateinit var logoutButton: TextView
            private lateinit var userImageView2: ImageView
            private lateinit var usernameText: TextView
            private lateinit var profile_drawer: GridLayout
            private lateinit var recyclerView: RecyclerView
            private lateinit var adapter: UsersAdapter
            private val companyLists = ArrayList<Users>()

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
        //        enableEdgeToEdge()
                setContentView(R.layout.activity_company_lists)

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

                recyclerView = findViewById(R.id.recyclerView)
                adapter = UsersAdapter(companyLists) { selectedCompany ->
                    val intent = Intent(this, CompanyDetail::class.java)
                    intent.putExtra("companyId", selectedCompany.id)
                    startActivity(intent)
                }

                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this)



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
                LoadCompanys()


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
//                            findViewById<TextView>(R.id.MyPosts).visibility = View.GONE
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




                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }

            private fun LoadCompanys() {
                val db2 = FirebaseFirestore.getInstance()
                db2.collection("Users")
                    .whereEqualTo("role", "Company")
                    .get()
                    .addOnSuccessListener { documents ->
                        companyLists.clear()
                        for (doc in documents) {
                            val companyL = doc.toObject(Users::class.java)
                            companyLists.add(companyL)
                        }
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }

                Log.d("LoadCompanys", "Fetched ${companyLists.size} companies")

            }


        }