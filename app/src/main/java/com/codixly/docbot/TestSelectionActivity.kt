package com.codixly.docbot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TestSelectionActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TestSelectionActivity"
    }

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

        findViewById<Button>(R.id.btn_blood_pressure).setOnClickListener {
            navigateToTest("BLOOD_PRESSURE")
        }

        findViewById<Button>(R.id.btn_height).setOnClickListener {
            navigateToTest("HEIGHT")
        }

        findViewById<Button>(R.id.btn_bca).setOnClickListener {
            navigateToTest("BCA")
        }

        findViewById<Button>(R.id.btn_temperature).setOnClickListener {
            navigateToTest("TEMPERATURE")
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }

        Log.d(TAG, "TestSelectionActivity created with device: $serialNumber")
    }

    private fun navigateToTest(testType: String) {
        Log.d(TAG, "Navigating to test: $testType")

        val intent = Intent(this, TestExecutionActivity::class.java).apply {
            // Primary test parameters
            putExtra("test_type", testType)
            putExtra("auth_key", "VmtaYVUyRnJNVVpPVlZaV1YwZG9VRmxYZEVkTk1WSldWV3RLYTAxRVJrVlVWV2h2VkRKV2MySkVVbFZOUmtwaFZHdFZOVkpXUmxsYVJUVlRVbFZaZWc9PQ==")

            // Pass all device data to the test execution activity
            putExtra("serial_number", getIntent().getStringExtra("serial_number"))
            putExtra("firmware_version", getIntent().getStringExtra("firmware_version"))
            putExtra("blood_pressure_module", getIntent().getStringExtra("blood_pressure_module"))
            putExtra("cholesterol_uric_acid_module", getIntent().getStringExtra("cholesterol_uric_acid_module"))
            putExtra("glucometer_module", getIntent().getStringExtra("glucometer_module"))
            putExtra("hemoglobin_module", getIntent().getStringExtra("hemoglobin_module"))
            putExtra("pulse_oximetry_module", getIntent().getStringExtra("pulse_oximetry_module"))
            putExtra("rdt_module", getIntent().getStringExtra("rdt_module"))
            putExtra("ecg_module", getIntent().getStringExtra("ecg_module"))
            putExtra("temperature", getIntent().getStringExtra("temperature"))

            // Add BCA-specific parameters if needed (with default values)
            putExtra("age", 25) // Default age for BCA
            putExtra("height", 170) // Default height in cm for BCA
            putExtra("is_female", false) // Default gender for BCA

            // Add additional parameters that might be useful
            putExtra("device_name", getIntent().getStringExtra("device_name") ?: "Unknown Device")
            putExtra("connection_status", getIntent().getStringExtra("connection_status") ?: "Connected")
        }

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "TestSelectionActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "TestSelectionActivity paused")
    }
}