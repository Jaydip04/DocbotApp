package com.codixly.docbot.model

data class VerifyTokenRequest(
    val token: String
)

// VerifyTokenResponse.kt
data class VerifyTokenResponse(
    val status: Boolean,
    val message: String,
    val customer: Customer?
)

