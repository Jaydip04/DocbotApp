package com.codixly.docbot.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.R
import com.codixly.docbot.model.GetPatientTestDetailsRequest
import com.codixly.docbot.model.GetPatientTestDetailsResponse
import com.codixly.docbot.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientProfileActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var heartRateTextView: TextView
    private lateinit var bloodGroupTextView: TextView
    private lateinit var weightTextView: TextView

    private var doubleBackToLogout = false
    private val backHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Custom back click logic
        toolbar.setNavigationOnClickListener {
            if (doubleBackToLogout) {
                getSharedPreferences("paitent_data", Context.MODE_PRIVATE).edit().clear().apply()
                Toast.makeText(this, "Patient logout", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                doubleBackToLogout = true
                Toast.makeText(this, "Click again to logout patient", Toast.LENGTH_SHORT).show()
                backHandler.postDelayed({ doubleBackToLogout = false }, 2000)
            }
        }

        // Bind views
        userNameTextView = findViewById(R.id.userName)
        userIdTextView = findViewById(R.id.userId)
        heartRateTextView = findViewById(R.id.heartRateValue)
        bloodGroupTextView = findViewById(R.id.bloodGroupValue)
        weightTextView = findViewById(R.id.weightValue)

        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)

        val patientPref = getSharedPreferences("paitent_data", Context.MODE_PRIVATE)
        val patientId = patientPref.getString("paitent_unique_id", null)

        if (!customerId.isNullOrEmpty() && !patientId.isNullOrEmpty()) {
            getPatientDetailsAndReports(patientId, customerId)
        } else {
            Toast.makeText(this, "Missing customer or patient ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPatientDetailsAndReports(patientId: String, customerId: String) {
        val request = GetPatientTestDetailsRequest(
            paitent_unique_id = patientId,
            customer_unique_id = customerId
        )
        Log.d("Request", request.toString())

        ApiClient.instance.getPatientTestDetails(request).enqueue(object :
            Callback<GetPatientTestDetailsResponse> {
            override fun onResponse(
                call: Call<GetPatientTestDetailsResponse>,
                response: Response<GetPatientTestDetailsResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data
                    Log.d("Api Response", data.toString())

                    data?.patient?.let { patient ->
                        getSharedPreferences("paitent_data", MODE_PRIVATE).edit().apply {
                            putString("paitent_id", patient.paitent_id.toString())
                            putString("paitent_unique_id", patient.paitent_unique_id)
                            putString("paitent_name", patient.paitent_name)
                            putString("paitent_email", patient.paitent_email)
                            putString("paitent_mobile", patient.paitent_mobile)
                            putString("gender", patient.gender)
                            putString("dob", patient.dob)
                            putString("age", patient.age)
                            putString("address", patient.address)
                            apply()
                        }

                        userNameTextView.text = patient.paitent_name
                        userIdTextView.text = "ID : ${patient.paitent_id}"
                    }

                    data?.reports?.forEach { report ->
                        Log.d("Report", "Test: ${report.test_name}, Date: ${report.inserted_date}, Results: ${report.result_array}")

                        when (report.test_name.lowercase()) {
                            "blood pressure" -> {
                                heartRateTextView.text = report.result_array["MCV"] ?: "0"
                            }
                            "cholesterol" -> {
                                bloodGroupTextView.text = report.result_array["MCH"] ?: "--"
                            }
                            "weight" -> {
                                weightTextView.text = report.result_array["MCHC"] ?: "0"
                            }
                        }
                    }

                } else {
                    Toast.makeText(this@PatientProfileActivity, "Failed to fetch patient details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetPatientTestDetailsResponse>, t: Throwable) {
                Toast.makeText(this@PatientProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @Suppress("MissingSuperCall")
    override fun onBackPressed() {
    }
}
