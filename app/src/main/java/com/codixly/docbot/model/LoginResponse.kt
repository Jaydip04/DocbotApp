package com.codixly.docbot.model

data class LoginResponse(
    val status: Boolean,
    val message: String,
    val customer: Customer?,
    val machineData: MachineData?
)

data class Customer(
    val customer_unique_id: String,
    val customer_profile: String?,
    val name: String,
    val username: String,
    val email: String,
    val mobile: String,
    val machine_id: String,
    val inserted_date: String,
    val inserted_time: String,
    val token: String
)

data class MachineData(
    val machine_id: Int,
    val machine_unique_id: String?,
    val blutooth_id: String
)
