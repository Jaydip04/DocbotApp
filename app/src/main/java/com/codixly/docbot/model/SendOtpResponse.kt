package com.codixly.docbot.model

data class SendOtpResponse(
    val status: Boolean,
    val message: String,
    val otp: Int,
    val existingPaitent: Boolean
)
