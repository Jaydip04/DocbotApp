package com.codixly.docbot.model

data class VerifyKeyResponse(
    val status: Boolean,
    val message: String,
    val machine_verify_key: String
)
