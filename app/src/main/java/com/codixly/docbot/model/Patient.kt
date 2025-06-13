package com.codixly.docbot.model

import java.io.Serializable

data class Patient(
    val paitent_id: Int,
    val paitent_name: String,
    val paitent_email: String,
    val paitent_mobile: String,
    val gender: String,
    val dob: String,
    val address: String,
    val inserted_date: String,
    val inserted_time: String,
    val token: String
) : Serializable


