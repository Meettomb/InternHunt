package com.example.internhunt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.ScrollView
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

class ProfileCompany : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var closeUpdateDetail: ImageView

    private lateinit var profile_background_edit_icon: TextView
    private lateinit var profile_background_edit_icon2: TextView
    private lateinit var detailScrollView: ScrollView
    private lateinit var detailEditIcon: TextView
    private lateinit var updateButton: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var uploadOverlay: FrameLayout
    private lateinit var uploadSection: LinearLayout

    private lateinit var upload_cover_text: TextView
    private lateinit var update_cover_text: TextView
    private lateinit var dragHandle: TextView
    private lateinit var uploadClick: LinearLayout
    private lateinit var userProfileImage2: ImageView
    private lateinit var companyname: TextView
    private lateinit var description: TextView
    private lateinit var company_url: TextView
    private lateinit var state_city: TextView
    private lateinit var fulladdress: TextView
    private lateinit var email: TextView
    private lateinit var phone: TextView
    private lateinit var company_description: EditText
    private lateinit var CompanyName: EditText
    private lateinit var CompanyUrl: EditText
    private lateinit var state: EditText
    private lateinit var City: EditText
    private lateinit var FullAddress: EditText
    private lateinit var company_descriptionError: TextView
    private lateinit var CompanyNameError: TextView
    private lateinit var CompanyUrlError: TextView
    private lateinit var StateError: TextView
    private lateinit var CityError: TextView
    private lateinit var FullAddressError: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_company)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For older versions, use a dark color with light icons
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        // Load Session
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val UserId = prefs.getString("userid", null)

        if (UserId != null){
            loadProfile(UserId)
        }
        else{
            Log.d("ProfileCompany", "Redirecting to login")
            Toast.makeText(this, "Please Login Again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }
        backButton = findViewById(R.id.backButton)
        profile_background_edit_icon = findViewById(R.id.profile_background_edit_icon)
        profile_background_edit_icon2 = findViewById(R.id.profile_background_edit_icon2)
        closeUpdateDetail = findViewById(R.id.closeUpdatedetail)
        detailScrollView = findViewById(R.id.detail_update_scroll_view)
        detailEditIcon = findViewById(R.id.detail_edit_icon)
        updateButton = findViewById(R.id.UpdateButton)
        progressBar = findViewById(R.id.progressBar)
        uploadOverlay = findViewById(R.id.upload_back_cover_overlay)
        upload_cover_text = findViewById(R.id.upload_cover_text)
        update_cover_text = findViewById(R.id.update_cover_text)
        uploadSection = findViewById(R.id.upload_section)
        dragHandle = findViewById(R.id.back_cover_drag_handle)
        uploadClick = findViewById(R.id.upload_cover_click)
        userProfileImage2 = findViewById(R.id.UserProfileImage2)
        companyname = findViewById(R.id.companyname)
        description = findViewById(R.id.description)
        company_url = findViewById(R.id.company_url)
        state_city = findViewById(R.id.state_city)
        fulladdress = findViewById(R.id.fulladdress)
        email = findViewById(R.id.email)
        phone = findViewById(R.id.phone)
        company_description = findViewById(R.id.company_description)
        CompanyName = findViewById(R.id.CompanyName)
        CompanyUrl = findViewById(R.id.CompanyUrl)
        state = findViewById(R.id.state)
        City = findViewById(R.id.City)
        FullAddress = findViewById(R.id.FullAddress)
        company_descriptionError = findViewById(R.id.company_descriptionError)
        CompanyNameError = findViewById(R.id.CompanyNameError)
        CompanyUrlError = findViewById(R.id.CompanyUrlError)
        StateError = findViewById(R.id.StateError)
        CityError = findViewById(R.id.CityError)
        FullAddressError = findViewById(R.id.FullAddressError)





        // Back Button
        backButton.setOnClickListener {
            finish()
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


        detailEditIcon.setOnClickListener {
            detailScrollView.visibility = View.VISIBLE
        }
        closeUpdateDetail.setOnClickListener {
            hideKeyboard(detailScrollView)
            detailScrollView.visibility = View.GONE
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (detailScrollView.visibility == View.VISIBLE) {
                    hideKeyboard(detailScrollView)
                    detailScrollView.visibility = View.GONE
                } else {
                    // Default behavior (finish the activity or go back)
                    isEnabled = false  // disable this callback
                    onBackPressedDispatcher.onBackPressed() // call default back behavior
                }
            }
        })

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

        updateButton.setOnClickListener {
            updateDetails(UserId)
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
                        if (!isFinishing && !isDestroyed) {
                            Glide.with(this)
                                .load(downloadUrl)
                                .centerCrop() // Optional: crop nicely
                                .into(coverImageView)
                        }

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

    private fun loadProfile(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val coverImage = findViewById<TextView>(R.id.coverImage)
                    val coverImageView = findViewById<ImageView>(R.id.coverImageView)
                    val back_cover = snapshot.getString("background_cover_url")

                    if (!back_cover.isNullOrEmpty()) {
                        coverImage.visibility = View.GONE
                        coverImageView.visibility = View.VISIBLE
                        profile_background_edit_icon.visibility = View.GONE
                        profile_background_edit_icon2.visibility = View.VISIBLE
                        update_cover_text.visibility = View.VISIBLE
                        upload_cover_text.visibility = View.GONE

                        if (!isDestroyed && !isFinishing) {
                            Glide.with(this)
                                .load(back_cover)
                                .centerCrop()
                                .into(coverImageView)
                        }
                    } else {
                        coverImage.visibility = View.VISIBLE
                        coverImageView.visibility = View.GONE
                        profile_background_edit_icon.visibility = View.VISIBLE
                        profile_background_edit_icon2.visibility = View.GONE
                        update_cover_text.visibility = View.GONE
                        upload_cover_text.visibility = View.VISIBLE
                    }

                    val imageUrl = snapshot.getString("profile_image_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        if (!isDestroyed && !isFinishing) {
                            Glide.with(this)
                                .load(imageUrl)
                                .into(findViewById(R.id.UserProfileImage2))
                        }
                    }

                    val Description = snapshot.getString("company_description") ?: ""
                    if (Description.isNotEmpty()) {
                        description.text = Description
                        company_description.setText(Description)
                    }

                    val Companyname = snapshot.getString("company_name") ?: ""
                    if (Companyname.isNotEmpty()) {
                        companyname.text = Companyname
                        CompanyName.setText(Companyname)
                    }

                    val Companyurl = snapshot.getString("company_url") ?: ""
                    if (Companyurl.isNotEmpty()) {
                        CompanyUrl.setText(Companyurl)
                        company_url.text = Companyurl
                        company_url.movementMethod = LinkMovementMethod.getInstance()
                    }

                    val State = snapshot.getString("state") ?: ""
                    if (State.isNotEmpty()) {
                        state.setText(State)
                    }

                    val city = snapshot.getString("city") ?: ""
                    if (city.isNotEmpty()) {
                        state_city.text = "$State - $city"
                        City.setText(city)
                    }

                    val fullAddress = snapshot.getString("full_address") ?: ""
                    if (fullAddress.isNotEmpty()) {
                        fulladdress.text = fullAddress
                        FullAddress.setText(fullAddress)
                    } else {
                        fulladdress.visibility = View.GONE
                    }

                    val Email = snapshot.getString("email") ?: ""
                    if (Email.isNotEmpty()) {
                        email.text = Email
                    }

                    val Phone = snapshot.getString("phone") ?: ""
                    if (Phone.isNotEmpty()) {
                        phone.text = Phone
                    }
                }
            }
    }

    private fun updateDetails(userId: String) {
        progressBar.visibility = View.VISIBLE
        val description = company_description.text.toString().trim()
        val companyName = CompanyName.text.toString().trim()
        val companyUrl = CompanyUrl.text.toString().trim()
        val stateText = state.text.toString().trim()
        val cityText = City.text.toString().trim()
        val fullAddressText = FullAddress.text.toString().trim()

        var isValid = true

        // 🔹 Validation
        if (description.isEmpty()) {
            company_descriptionError.visibility = View.VISIBLE
            isValid = false
        } else company_descriptionError.visibility = View.GONE

        if (companyName.isEmpty()) {
            CompanyNameError.visibility = View.VISIBLE
            isValid = false
        } else CompanyNameError.visibility = View.GONE

        if (companyUrl.isEmpty() || !Patterns.WEB_URL.matcher(companyUrl).matches()) {
            CompanyUrlError.text = "Enter a valid website URL"
            CompanyUrlError.visibility = View.VISIBLE
            isValid = false
        } else {
            CompanyUrlError.visibility = View.GONE
        }

        if (stateText.isEmpty()) {
            StateError.visibility = View.VISIBLE
            isValid = false
        } else StateError.visibility = View.GONE

        if (cityText.isEmpty()) {
            CityError.visibility = View.VISIBLE
            isValid = false
        } else CityError.visibility = View.GONE

        if (fullAddressText.isEmpty()) {
            FullAddressError.visibility = View.VISIBLE
            isValid = false
        } else FullAddressError.visibility = View.GONE

        // 🔹 If not valid, stop here
        if (!isValid) return

        // 🔹 Firestore Update
        val db = FirebaseFirestore.getInstance()
        val updates = hashMapOf<String, Any>(
            "company_description" to description,
            "company_name" to companyName,
            "company_url" to companyUrl,
            "state" to stateText,
            "city" to cityText,
            "full_address" to fullAddressText
        )

        db.collection("Users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show()
                detailScrollView.visibility = View.GONE // hide update form if needed
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}