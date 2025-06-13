package com.codixly.docbot.model

data class DeleteAccountRequest(
    val customer_unique_id: String,
    val token: String
)
