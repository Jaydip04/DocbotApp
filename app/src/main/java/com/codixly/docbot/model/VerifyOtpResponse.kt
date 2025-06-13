package com.codixly.docbot.model

data class VerifyOtpResponse(
    val status: Boolean,
    val message: String,
    val paitent: Patient
)
