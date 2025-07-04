package com.codixly.docbot.model

data class GetPatientTestDetailsRequest(
    val paitent_unique_id: String,
    val customer_unique_id: String
)
