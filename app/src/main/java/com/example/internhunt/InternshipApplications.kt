package com.example.internhunt

import com.google.firebase.Timestamp

data class InternshipApplications(
    var applyDate: Timestamp? = null,
    var companyId: String = "",
    var id: String = "",
    var pdfUrl: String = "",
    var postId: String = "",
    var userId: String = ""
)
