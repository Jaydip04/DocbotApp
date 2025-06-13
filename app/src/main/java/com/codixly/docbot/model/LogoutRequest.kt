package com.codixly.docbot.model

data class LogoutRequest(
    val customer_unique_id: String
)

// LogoutResponse.kt
data class LogoutResponse(
    val status: Boolean,
    val message: String
)