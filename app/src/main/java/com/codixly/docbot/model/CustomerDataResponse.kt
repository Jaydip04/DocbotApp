package com.codixly.docbot.model

data class CustomerDataResponse(
    val status: Boolean,
    val message: String,
    val customer_data: CustomerData?
)

data class CustomerData(
    val customer_unique_id: String,
    val customer_profile: String?,
    val name: String,
    val username: String,
    val email: String,
    val mobile: String,
    val machine_unique_id: String
)
