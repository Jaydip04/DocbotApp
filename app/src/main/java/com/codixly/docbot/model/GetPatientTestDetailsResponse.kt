package com.codixly.docbot.model

data class GetPatientTestDetailsResponse(
    val status: Boolean,
    val message: String,
    val data: PatientTestData?
)

data class PatientTestData(
    val patient: PatientDetails,
    val reports: List<TestReport>
)

data class TestReport(
    val report_id: Int,
    val inserted_time: String,
    val inserted_date: String,
    val test_name: String,
    val que_id: Int,
    val module_name: String,
    val result_array: Map<String, String>,
    val color: String,
    val this_machine: Int
)
