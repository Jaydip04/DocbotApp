package com.codixly.docbot.model

data class PatientRegistrationRequest(
    val paitent_name: String,
    val paitent_mobile: String,
    val paitent_email: String,
    val gender: String,
    val dob: String,
    val address: String,
    val customer_unique_id: String
)

data class PatientRegistrationResponse(
    val status: Boolean,
    val message: String,
    val paitent: PatientDetails
)

data class PatientDetails(
    val paitent_id: Int,
    val paitent_unique_id: String,
    val paitent_name: String,
    val paitent_email: String,
    val paitent_mobile: String,
    val gender: String,
    val dob: String,
    val age: String,
    val address: String,
    val inserted_date: String,
    val inserted_time: String,
    val customer_id: Int,
    val token: String
)

