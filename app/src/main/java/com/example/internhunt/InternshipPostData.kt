package com.example.internhunt

import android.R
import android.icu.text.CaseMap
import com.google.firebase.Timestamp


data class InternshipPostData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val stipend: String = "",
    val duration: String = "",
    val skillsRequired: List<String> = emptyList(),
    val responsibilities: List<String> = emptyList(),
    val companyId: String = "",
    val postedDate: Timestamp? = null,
    val applicationDeadline: String = "",
    val openings: String = "",
    val type: String = "",
    val internshipType: String = "",
    val internshipTime: String = "",
    val perks: List<String> = emptyList(),
    val hiredApplicants: List<String> = emptyList(),
    val status: Boolean = true,
    val degreeEligibility: List<String> = emptyList(),
    val otherFields: String = ""
)

