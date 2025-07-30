package com.example.internhunt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var userProfileImage2: ImageView
    private lateinit var username: TextView
    private lateinit var uploadOverlay: FrameLayout
    private lateinit var uploadSection: LinearLayout
    private lateinit var dragHandle: TextView
    private lateinit var uploadClick: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var profile_background_edit_icon: TextView
    private lateinit var profile_background_edit_icon2: TextView
    private lateinit var upload_cover_text: TextView
    private lateinit var update_cover_text: TextView
    private lateinit var collage_name: TextView
    private lateinit var backButton: ImageView
    private val REQUEST_CODE_COVER_IMAGE = 1001
    private val REQUEST_CODE_PROFILE_IMAGE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_color)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        userProfileImage2 = findViewById(R.id.UserProfileImage2)
        username = findViewById(R.id.username)
        uploadOverlay = findViewById(R.id.upload_back_cover_overlay)
        uploadSection = findViewById(R.id.upload_section)
        dragHandle = findViewById(R.id.back_cover_drag_handle)
        uploadClick = findViewById(R.id.upload_cover_click)
        profile_background_edit_icon = findViewById(R.id.profile_background_edit_icon)
        profile_background_edit_icon2 = findViewById(R.id.profile_background_edit_icon2)
        progressBar = findViewById(R.id.progressBar)
        upload_cover_text = findViewById(R.id.upload_cover_text)
        update_cover_text = findViewById(R.id.update_cover_text)
        collage_name = findViewById(R.id.collage_name)
        backButton = findViewById(R.id.backButton)

        // Back Button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // Open panel
        profile_background_edit_icon.setOnClickListener {
            openUploadSection()
        }
        profile_background_edit_icon2.setOnClickListener {
            openUploadSection()
        }

        // Close on background tap
        uploadOverlay.setOnClickListener {
            closeUploadSection()
        }

        // Prevent closing when tapping inside
        uploadSection.setOnClickListener {
            // Do nothing (prevents click-through)
        }

        // Close on drag handle tap
        dragHandle.setOnClickListener {
            closeUploadSection()
        }




        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (uploadOverlay.visibility == View.VISIBLE) {
                    closeUploadSection()
                } else {
                    finish()
                }
            }
        })


        // Load Session
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val UserId = prefs.getString("userid", null)

        if (UserId != null){
            loadUserProfile(UserId)
        }
        else{
            Toast.makeText(this, "Please Login Again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }


        // Perform upload click
        uploadClick.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
            closeUploadSection()
        }


        userProfileImage2.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1002)  // use different requestCode than cover
        }






        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
        }
    }

    private fun openUploadSection() {
        uploadOverlay.visibility = View.VISIBLE
        uploadSection.translationY = uploadSection.height.toFloat()
        uploadSection.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
    }

    private fun closeUploadSection() {
        uploadSection.animate()
            .translationY(uploadSection.height.toFloat())
            .setDuration(300)
            .withEndAction {
                uploadOverlay.visibility = View.GONE
            }
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            uploadImageToFirebase(imageUri)
        }

        if (requestCode == 1002 && resultCode == RESULT_OK && data?.data != null) {
            val imageUri2 = data.data
            imageUri2?.let {
                uploadProfileImageToFirebase(it) // Now it is a non-null Uri
            }
        }

    }

    private fun uploadImageToFirebase(imageUri: android.net.Uri?) {
        progressBar.visibility = View.VISIBLE
        if (imageUri == null) return

        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = prefs.getString("userid", null) ?: return

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("Users").document(userId)

        // Step 1: Get existing image URL
        userRef.get().addOnSuccessListener { doc ->
            val existingUrl = doc.getString("background_cover_url")

            // Step 2: If old image exists, delete it
            if (!existingUrl.isNullOrEmpty()) {
                val oldImageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                    .getReferenceFromUrl(existingUrl)

                oldImageRef.delete()
                    .addOnSuccessListener {
                        // Old image deleted, now upload new
                        uploadNewCoverImage(userId, imageUri)
                    }
                    .addOnFailureListener {
                        // Even if deletion fails, proceed to upload new image
                        uploadNewCoverImage(userId, imageUri)
                    }
            } else {
                // No old image, just upload new
                uploadNewCoverImage(userId, imageUri)
            }
        }
    }
    private fun uploadNewCoverImage(userId: String, imageUri: android.net.Uri) {
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
            .reference.child("user_backgrounds/$userId.jpg")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()

                val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
                userRef.update("background_cover_url", downloadUrl)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Cover image updated successfully", Toast.LENGTH_SHORT).show()

                        // Instantly load new image into ImageView
                        val coverImageView = findViewById<ImageView>(R.id.coverImageView)
                        Glide.with(this)
                            .load(downloadUrl)
                            .centerCrop() // Optional: crop nicely
                            .into(coverImageView)

                        // Hide placeholder and edit icon if needed
                        findViewById<View>(R.id.coverImage).visibility = View.GONE
                        findViewById<View>(R.id.profile_background_edit_icon).visibility = View.GONE
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Image upload failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadProfileImageToFirebase(imageUri: android.net.Uri) {
        progressBar.visibility = View.VISIBLE

        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = prefs.getString("userid", null) ?: return

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("Users").document(userId)

        // Step 1: Fetch existing profile image URL
        userRef.get().addOnSuccessListener { doc ->
            val existingUrl = doc.getString("profile_image_url")

            // Step 2: Delete old image if exists
            if (!existingUrl.isNullOrEmpty()) {
                val oldImageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                    .getReferenceFromUrl(existingUrl)

                oldImageRef.delete()
                    .addOnSuccessListener {
                        // Proceed to upload new image
                        uploadNewProfileImage(userId, imageUri)
                    }
                    .addOnFailureListener {
                        // Even if deletion fails, upload new image
                        uploadNewProfileImage(userId, imageUri)
                    }
            } else {
                // No old image, directly upload new one
                uploadNewProfileImage(userId, imageUri)
            }
        }
    }
    private fun uploadNewProfileImage(userId: String, imageUri: android.net.Uri) {
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
            .reference.child("profile_images/$userId.jpg")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()

                val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId)
                userRef.update("profile_image_url", downloadUrl)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()

                        // Instantly show new image
                        Glide.with(this)
                            .load(downloadUrl)
                            .centerCrop()
                            .into(userProfileImage2)
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Image upload failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun loadUserProfile(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {


                    val coverImage = findViewById<TextView>(R.id.coverImage)
                    val coverImageView = findViewById<ImageView>(R.id.coverImageView)
                    val back_cover = doc.getString("background_cover_url")

                    if (!back_cover.isNullOrEmpty()) {
                        // Hide TextView, show ImageView and load image
                        coverImage.visibility = View.GONE
                        coverImageView.visibility = View.VISIBLE
                        profile_background_edit_icon.visibility = View.GONE
                        profile_background_edit_icon2.visibility = View.VISIBLE
                        update_cover_text.visibility = View.VISIBLE
                        upload_cover_text.visibility = View.GONE

                        Glide.with(this)
                            .load(back_cover)
                            .into(coverImageView)
                    } else {
                        // Show TextView, hide ImageView
                        coverImage.visibility = View.VISIBLE
                        coverImageView.visibility = View.GONE
                        profile_background_edit_icon.visibility = View.VISIBLE
                        profile_background_edit_icon2.visibility = View.GONE
                        update_cover_text.visibility = View.GONE
                                upload_cover_text.visibility = View.VISIBLE
                    }


                    val imageUrl = doc.getString("profile_image_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(findViewById(R.id.UserProfileImage2))

                    }

                    val userName = doc.getString("username")
                    val companyName = doc.getString("company_name")
                    val role = doc.getString("role")

                    val username = findViewById<TextView>(R.id.username)

                    if (role == "Student") {
                        if (!userName.isNullOrEmpty()) {
                            username.text = userName
                        }
                    } else if (role == "Company") {
                        if (!companyName.isNullOrEmpty()) {
                            username.text = companyName
                            collage_name.visibility = View.GONE
                        }
                    } else {
                        username.text = "Guest"
                    }

                    val collageName = doc.getString("collage_name") ?: ""
                    val collageView = findViewById<TextView>(R.id.collage_name)
                    collageView.text = collageName.split(" ").joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercase() }
                    }


                    val state = doc.getString("state") ?: ""
                    val city = doc.getString("city") ?: ""
                    val locationView = findViewById<TextView>(R.id.Location)
                    locationView.text = "$city, $state".trim().trimStart(',')

                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }


}