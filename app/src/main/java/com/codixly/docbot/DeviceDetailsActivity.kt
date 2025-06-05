//package com.codixly.docbot
//
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//
//class DeviceDetailsActivity : AppCompatActivity() {
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
//        setContentView(R.layout.activity_device_details)
//
//        val serialNumber = intent.getStringExtra("serial_number") ?: "N/A"
//        val firmwareVersion = intent.getStringExtra("firmware_version") ?: "N/A"
//
//        findViewById<TextView>(R.id.tv_serial_number).text = "Serial Number: $serialNumber"
//        findViewById<TextView>(R.id.tv_firmware_version).text = "Firmware: $firmwareVersion"
//
//        // Setup show details button to show device details dialog
//        findViewById<Button>(R.id.btn_show_details).setOnClickListener {
//            showDeviceDetailsDialog()
//        }
//
//        // Setup continue button to navigate to test selection screen
//        findViewById<Button>(R.id.btn_continue).setOnClickListener {
//            navigateToTestSelection()
//        }
//    }
//
//    private fun showDeviceDetailsDialog() {
//        // Get all device data from intent
//        val serialNumber = intent.getStringExtra("serial_number") ?: "HCSE030140322000390"
//        val firmwareVersion = intent.getStringExtra("firmware_version") ?: "MML 2.907.3"
//        val bloodPressureModule = intent.getStringExtra("blood_pressure_module") ?: "ACTIVE"
//        val cholesterolModule = intent.getStringExtra("cholesterol_uric_acid_module") ?: "ACTIVE"
//        val glucometerModule = intent.getStringExtra("glucometer_module") ?: "ACTIVE"
//        val hemoglobinModule = intent.getStringExtra("hemoglobin_module") ?: "ACTIVE"
//        val pulseOximetryModule = intent.getStringExtra("pulse_oximetry_module") ?: "ACTIVE"
//        val rdtModule = intent.getStringExtra("rdt_module") ?: "ACTIVE"
//        val ecgModule = intent.getStringExtra("ecg_module") ?: "INACTIVE"
//        val temperature = intent.getStringExtra("temperature") ?: "0.0"
//
//        // Create device info message in the format shown in the image
//        val deviceInfo = buildString {
//            appendLine("Introspection")
//            appendLine()
//            appendLine("HCDeviceData")
//            appendLine("serialNumber=$serialNumber")
//            appendLine("firmwareVersion=$firmwareVersion")
//            appendLine("bloodPressureModule=$bloodPressureModule")
//            appendLine("cholestrolUricAcidModule=$cholesterolModule")
//            appendLine("glucometerModule=$glucometerModule")
//            appendLine("hemoglobinModule=$hemoglobinModule")
//            appendLine("pulseOximetryModule=$pulseOximetryModule")
//            appendLine("rdtModule=$rdtModule")
//            appendLine("ecgModule=$ecgModule")
//            append("temperature=$temperature")
//        }
//
//        // Create and show the dialog
//        AlertDialog.Builder(this)
//            .setTitle("Device Information")
//            .setMessage(deviceInfo)
//            .setPositiveButton("OK") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .setCancelable(true)
//            .show()
//    }
//
//    private fun navigateToTestSelection() {
//        val intent = Intent(this, TestSelectionActivity::class.java)
//        // Pass all device data to the test selection activity
//        intent.putExtra("serial_number", intent.getStringExtra("serial_number"))
//        intent.putExtra("firmware_version", intent.getStringExtra("firmware_version"))
//        intent.putExtra("blood_pressure_module", intent.getStringExtra("blood_pressure_module"))
//        intent.putExtra("cholesterol_uric_acid_module", intent.getStringExtra("cholesterol_uric_acid_module"))
//        intent.putExtra("glucometer_module", intent.getStringExtra("glucometer_module"))
//        intent.putExtra("hemoglobin_module", intent.getStringExtra("hemoglobin_module"))
//        intent.putExtra("pulse_oximetry_module", intent.getStringExtra("pulse_oximetry_module"))
//        intent.putExtra("rdt_module", intent.getStringExtra("rdt_module"))
//        intent.putExtra("ecg_module", intent.getStringExtra("ecg_module"))
//        intent.putExtra("temperature", intent.getStringExtra("temperature"))
//
//        startActivity(intent)
//    }
//}
package com.codixly.docbot

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DeviceDetailsActivity : AppCompatActivity() {

    // Data class to hold device information
    private data class DeviceData(
        val serialNumber: String,
        val firmwareVersion: String,
        val bloodPressureModule: String,
        val cholesterolUricAcidModule: String,
        val glucometerModule: String,
        val hemoglobinModule: String,
        val pulseOximetryModule: String,
        val rdtModule: String,
        val ecgModule: String,
        val temperature: String
    )

    private lateinit var deviceData: DeviceData

    companion object {
        // Intent extra keys
        const val EXTRA_SERIAL_NUMBER = "serial_number"
        const val EXTRA_FIRMWARE_VERSION = "firmware_version"
        const val EXTRA_BLOOD_PRESSURE_MODULE = "blood_pressure_module"
        const val EXTRA_CHOLESTEROL_URIC_ACID_MODULE = "cholesterol_uric_acid_module"
        const val EXTRA_GLUCOMETER_MODULE = "glucometer_module"
        const val EXTRA_HEMOGLOBIN_MODULE = "hemoglobin_module"
        const val EXTRA_PULSE_OXIMETRY_MODULE = "pulse_oximetry_module"
        const val EXTRA_RDT_MODULE = "rdt_module"
        const val EXTRA_ECG_MODULE = "ecg_module"
        const val EXTRA_TEMPERATURE = "temperature"

        // Default values
        private const val DEFAULT_SERIAL_NUMBER = "HCSE030140322000390"
        private const val DEFAULT_FIRMWARE_VERSION = "MML 2.907.3"
        private const val DEFAULT_MODULE_STATUS = "ACTIVE"
        private const val DEFAULT_ECG_MODULE_STATUS = "INACTIVE"
        private const val DEFAULT_TEMPERATURE = "0.0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        setContentView(R.layout.activity_device_details)

        // Initialize device data from intent
        initializeDeviceData()

        // Setup UI
        setupUI()
        setupClickListeners()
    }

    private fun initializeDeviceData() {
        deviceData = DeviceData(
            serialNumber = intent.getStringExtra(EXTRA_SERIAL_NUMBER) ?: DEFAULT_SERIAL_NUMBER,
            firmwareVersion = intent.getStringExtra(EXTRA_FIRMWARE_VERSION) ?: DEFAULT_FIRMWARE_VERSION,
            bloodPressureModule = intent.getStringExtra(EXTRA_BLOOD_PRESSURE_MODULE) ?: DEFAULT_MODULE_STATUS,
            cholesterolUricAcidModule = intent.getStringExtra(EXTRA_CHOLESTEROL_URIC_ACID_MODULE) ?: DEFAULT_MODULE_STATUS,
            glucometerModule = intent.getStringExtra(EXTRA_GLUCOMETER_MODULE) ?: DEFAULT_MODULE_STATUS,
            hemoglobinModule = intent.getStringExtra(EXTRA_HEMOGLOBIN_MODULE) ?: DEFAULT_MODULE_STATUS,
            pulseOximetryModule = intent.getStringExtra(EXTRA_PULSE_OXIMETRY_MODULE) ?: DEFAULT_MODULE_STATUS,
            rdtModule = intent.getStringExtra(EXTRA_RDT_MODULE) ?: DEFAULT_MODULE_STATUS,
            ecgModule = intent.getStringExtra(EXTRA_ECG_MODULE) ?: DEFAULT_ECG_MODULE_STATUS,
            temperature = intent.getStringExtra(EXTRA_TEMPERATURE) ?: DEFAULT_TEMPERATURE
        )
    }

    private fun setupUI() {
        // Set basic device info in UI
        findViewById<TextView>(R.id.tv_serial_number).text =
            getString(R.string.serial_number_format, deviceData.serialNumber)
        findViewById<TextView>(R.id.tv_firmware_version).text =
            getString(R.string.firmware_version_format, deviceData.firmwareVersion)
    }

    private fun setupClickListeners() {
        // Setup show details button
        findViewById<Button>(R.id.btn_show_details).setOnClickListener {
            showDeviceDetailsDialog()
        }

        // Setup continue button
        findViewById<Button>(R.id.btn_continue).setOnClickListener {
            navigateToTestSelection()
        }

        // Setup back button if exists
        findViewById<Button>(R.id.btn_back)?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showDeviceDetailsDialog() {
        // Create comprehensive device info message
        val deviceInfo = buildDeviceInfoString()

        // Create and show the dialog with better styling
        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle(getString(R.string.device_information_title))
            .setMessage(deviceInfo)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton(getString(R.string.copy_to_clipboard)) { _, _ ->
                copyToClipboard(deviceInfo)
            }
            .setCancelable(true)
            .create()
            .apply {
                // Make dialog scrollable for long content
                setOnShowListener {
                    val textView = findViewById<TextView>(android.R.id.message)
                    textView?.apply {
                        setTextIsSelectable(true)
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                }
            }
            .show()
    }

    private fun buildDeviceInfoString(): String {
        return buildString {
            appendLine("═══════════════════════════════════")
            appendLine("        DEVICE INTROSPECTION")
            appendLine("═══════════════════════════════════")
            appendLine()
            appendLine("HCDeviceData {")
            appendLine("  serialNumber: ${deviceData.serialNumber}")
            appendLine("  firmwareVersion: ${deviceData.firmwareVersion}")
            appendLine()
            appendLine("  --- MODULE STATUS ---")
            appendLine("  bloodPressureModule: ${deviceData.bloodPressureModule}")
            appendLine("  cholesterolUricAcidModule: ${deviceData.cholesterolUricAcidModule}")
            appendLine("  glucometerModule: ${deviceData.glucometerModule}")
            appendLine("  hemoglobinModule: ${deviceData.hemoglobinModule}")
            appendLine("  pulseOximetryModule: ${deviceData.pulseOximetryModule}")
            appendLine("  rdtModule: ${deviceData.rdtModule}")
            appendLine("  ecgModule: ${deviceData.ecgModule}")
            appendLine()
            appendLine("  --- SENSOR DATA ---")
            appendLine("  temperature: ${deviceData.temperature}°C")
            appendLine()
            appendLine("  --- DEVICE CAPABILITIES ---")
            appendLine("  Total Active Modules: ${getActiveModuleCount()}")
            appendLine("  Device Status: ${getDeviceStatus()}")
            appendLine("  Last Updated: ${getCurrentTimestamp()}")
            appendLine("}")
            appendLine()
            appendLine("═══════════════════════════════════")
        }
    }

    private fun getActiveModuleCount(): Int {
        return listOf(
            deviceData.bloodPressureModule,
            deviceData.cholesterolUricAcidModule,
            deviceData.glucometerModule,
            deviceData.hemoglobinModule,
            deviceData.pulseOximetryModule,
            deviceData.rdtModule,
            deviceData.ecgModule
        ).count { it.equals("ACTIVE", ignoreCase = true) }
    }

    private fun getDeviceStatus(): String {
        val activeModules = getActiveModuleCount()
        return when {
            activeModules >= 6 -> "FULLY OPERATIONAL"
            activeModules >= 4 -> "OPERATIONAL"
            activeModules >= 2 -> "LIMITED FUNCTIONALITY"
            else -> "MINIMAL FUNCTIONALITY"
        }
    }

    private fun getCurrentTimestamp(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Device Information", text)
        clipboard.setPrimaryClip(clip)

        // Show toast confirmation
        android.widget.Toast.makeText(this, getString(R.string.copied_to_clipboard), android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTestSelection() {
        val intent = Intent(this, TestSelectionActivity::class.java).apply {
            // Pass all device data using the constants
            putExtra(EXTRA_SERIAL_NUMBER, deviceData.serialNumber)
            putExtra(EXTRA_FIRMWARE_VERSION, deviceData.firmwareVersion)
            putExtra(EXTRA_BLOOD_PRESSURE_MODULE, deviceData.bloodPressureModule)
            putExtra(EXTRA_CHOLESTEROL_URIC_ACID_MODULE, deviceData.cholesterolUricAcidModule)
            putExtra(EXTRA_GLUCOMETER_MODULE, deviceData.glucometerModule)
            putExtra(EXTRA_HEMOGLOBIN_MODULE, deviceData.hemoglobinModule)
            putExtra(EXTRA_PULSE_OXIMETRY_MODULE, deviceData.pulseOximetryModule)
            putExtra(EXTRA_RDT_MODULE, deviceData.rdtModule)
            putExtra(EXTRA_ECG_MODULE, deviceData.ecgModule)
            putExtra(EXTRA_TEMPERATURE, deviceData.temperature)
        }

        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Add any cleanup if needed
    }
}