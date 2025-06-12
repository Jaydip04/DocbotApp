package com.codixly.docbot.model

data class CustomerDataRequest(
    val customer_unique_id: String,
    val token: String
)