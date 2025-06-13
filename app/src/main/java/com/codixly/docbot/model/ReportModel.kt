package com.codixly.docbot.model

data class ReportModel(
    val title: String,
    val date: String,
    val time: String,
    val imageResId: Int // Example: R.drawable.ic_patients
)