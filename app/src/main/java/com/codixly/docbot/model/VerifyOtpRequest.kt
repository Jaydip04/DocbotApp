package com.codixly.docbot.model

data class VerifyOtpRequest(
    val mobile: String,
    val otp: Int,
    val existingPaitent: Boolean
)
