package com.example.internhunt

data class EducationEntry(
    val collage_name: String = "",
    val degree_name: String = "",
    val graduation_start_year: String = "",
    val graduation_end_year: String = ""
)
data class ProjectsEntry(
    val description: String = "",
    val duration: String = "",
    val link: String = "",
    val startDate: String = "",
    val technologies: List<String> = emptyList(),
    val title: String = "",
)

data class Users(
    val city: String = "",
    val date_of_birth: String = "",
    val email: String = "",
    val gender: String = "",
    val headline: String = "",
    val hide_post: List<String> = emptyList(),
    val removed_applied_posts: List<String> = emptyList(),
    val education: List<EducationEntry> = emptyList(),
    val bookmark: List<String> = emptyList(),
    val id: String = "",
    val isactive: Boolean = true,
    val password: String = "",
    val phone: String = "",
    val profile_image_url: String = "",
    val role: String = "",
    val signup_date: String = "",
    val state: String = "",
    val skill: List<String> = emptyList(),
    val projects: List<ProjectsEntry> = emptyList(),
    val experience: List<String> = emptyList(),
    val username: String = "",
    val company_name: String = "",
    val company_url: String = "",
    val company_description: String = "",
    val notify_new_internships: Boolean = true
)
