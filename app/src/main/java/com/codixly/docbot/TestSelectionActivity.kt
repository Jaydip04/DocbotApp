package com.codixly.docbot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TestSelectionActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        setContentView(R.layout.activity_test_selection)

        // Display device status
        val serialNumber = intent.getStringExtra("serial_number") ?: "N/A"
        findViewById<TextView>(R.id.tv_device_status).text = "Connected Device: $serialNumber"

        // Setup test buttons
        findViewById<Button>(R.id.btn_pulse_oximetry).setOnClickListener {
            navigateToTest("PULSE_OXIMETRY")
        }

        findViewById<Button>(R.id.btn_weight_temperature).setOnClickListener {
            navigateToTest("WEIGHT_TEMPERATURE")
        }

        findViewById<Button>(R.id.btn_blood_pressure).setOnClickListener {
            navigateToTest("BLOOD_PRESSURE")
        }

        findViewById<Button>(R.id.btn_glucose_test).setOnClickListener {
            navigateToTest("GLUCOSE_TEST")
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun navigateToTest(testType: String) {
        val intent = Intent(this, TestExecutionActivity::class.java)
        intent.putExtra("test_type", testType)
        intent.putExtra("auth_key", "VmtaYVUyRnJNVVpPVlZaV1YwZG9VRmxYZEVkTk1WSldWV3RLYTAxRVJrVlVWV2h2VkRKV2MySkVVbFZOUmtwaFZHdFZOVkpXUmxsYVJUVlRVbFZaZWc9PQ==")

        // Pass all device data to the test execution activity
        intent.putExtra("serial_number", intent.getStringExtra("serial_number"))
        intent.putExtra("firmware_version", intent.getStringExtra("firmware_version"))
        intent.putExtra("blood_pressure_module", intent.getStringExtra("blood_pressure_module"))
        intent.putExtra("cholesterol_uric_acid_module", intent.getStringExtra("cholesterol_uric_acid_module"))
        intent.putExtra("glucometer_module", intent.getStringExtra("glucometer_module"))
        intent.putExtra("hemoglobin_module", intent.getStringExtra("hemoglobin_module"))
        intent.putExtra("pulse_oximetry_module", intent.getStringExtra("pulse_oximetry_module"))
        intent.putExtra("rdt_module", intent.getStringExtra("rdt_module"))
        intent.putExtra("ecg_module", intent.getStringExtra("ecg_module"))
        intent.putExtra("temperature", intent.getStringExtra("temperature"))

        startActivity(intent)
    }
}