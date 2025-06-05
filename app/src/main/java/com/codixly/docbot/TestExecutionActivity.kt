package com.codixly.docbot

import android.app.AlertDialog
import android.app.Application
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT.stopCurrentTest
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxDataListener
import com.healthcubed.ezdxlib.model.EzdxData
import com.healthcubed.ezdxlib.model.HCDeviceData
import com.healthcubed.ezdxlib.model.Status

class TestExecutionActivity : AppCompatActivity(),
    BluetoothService.OnBluetoothEventCallback,
    EzdxDataListener {

    private lateinit var testType: String
    private lateinit var authKey: String

    private lateinit var tvTestTitle: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var tvResults: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnStartTest: Button
    private lateinit var btnStopTest: Button
    private lateinit var btnBack: Button
    private lateinit var cardResults: CardView
    private var isTestRunning = false
    private val testHandler = Handler(Looper.getMainLooper())
//    private var isDeviceAuthenticated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        setContentView(R.layout.activity_test_execution)

        testType = intent.getStringExtra("test_type") ?: "UNKNOWN"
        authKey = intent.getStringExtra("auth_key") ?: "default-key"

        initializeViews()
        setupTestUI()
        setupButtonListeners()

        BluetoothService.getDefaultInstance().setOnEventCallback(this)
        authenticateDevice()
    }

    private fun initializeViews() {
        tvTestTitle = findViewById(R.id.tv_test_title)
        tvStatus = findViewById(R.id.tv_status)
        tvInstructions = findViewById(R.id.tv_instructions)
        tvResults = findViewById(R.id.tv_results)
        progressBar = findViewById(R.id.progress_bar)
        btnStartTest = findViewById(R.id.btn_start_test)
        btnStopTest = findViewById(R.id.btn_stop_test)
        btnBack = findViewById(R.id.btn_back)
        cardResults = findViewById(R.id.card_results)
    }

    private fun setupTestUI() {
        tvTestTitle.text = when (testType) {
            "PULSE_OXIMETRY" -> "Pulse Oximetry Test"
            "WEIGHT_TEMPERATURE" -> "Weight / Temperature Test"
            "BLOOD_PRESSURE" -> "Blood Pressure Test"
            "BCA" -> "Body Composition Analysis"
            "HEIGHT" -> "Height Test"
            "GLUCOSE_TEST" -> "Blood Glucose Test"
            else -> "Unknown Test"
        }

        tvInstructions.text = when (testType) {
            "PULSE_OXIMETRY" -> "1. Connect sensor probe\n2. Place finger\n3. Remain still"
            "WEIGHT_TEMPERATURE" -> "1. Power on device\n2. Pair with machine\n3. Take reading"
            "BLOOD_PRESSURE" -> "1. Attach cuff\n2. Relax arm\n3. Remain calm"
            "BCA" -> "1. Stand barefoot\n2. Hold handles\n3. Stay steady"
            "HEIGHT" -> "1. Step on scale\n2. Press measure\n3. Wait for result"
            "GLUCOSE_TEST" -> "1. Insert test strip\n2. Apply blood sample\n3. Wait for result"
            else -> "No instructions available"
        }

        tvStatus.text = "Authenticating device..."
        tvResults.isVisible = false
        btnStartTest.isEnabled = false
        btnStopTest.isEnabled = false
    }

    private fun authenticateDevice() {
        tvStatus.text = "Authenticating device..."
        when (EzdxBT.authenticate(authKey)) {
            Status.SUCCESS -> {
//                isDeviceAuthenticated = true
                tvStatus.text = "Authenticated. Ready to test."
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                btnStartTest.isEnabled = true
            }
            Status.BLUETOOTH_NOT_CONNECTED -> showError("Bluetooth not connected")
            Status.INVALID_KEY -> showError("Invalid authentication key")
            else -> showError("Authentication failed")
        }
    }

    private fun setupButtonListeners() {
        btnStartTest.setOnClickListener { startTest() }
        btnStopTest.setOnClickListener { stopTest() }
        btnBack.setOnClickListener {
            if (isTestRunning) showExitConfirmation() else finish()
        }
    }

    private fun startTest() {
        isTestRunning = true
        btnStartTest.isEnabled = false
        btnStopTest.isEnabled = true
        progressBar.isVisible = true
        tvResults.isVisible = false

        when (testType) {
            "PULSE_OXIMETRY" -> handleStartResult(EzdxBT.startPulseOximetry().toString(), "Pulse Oximetry")
            "BLOOD_PRESSURE" -> handleStartResult(EzdxBT.startAdultBloodPressure().toString(), "Blood Pressure")
            "GLUCOSE_TEST" -> handleStartResult(EzdxBT.startBloodGlucose().toString(), "Blood Glucose")
            "WEIGHT_TEMPERATURE" -> {
                tvStatus.text = "Pairing thermometer..."
                if (EzdxBT.startTemperaturePairing().toString() == "SUCCESS") {
                    testHandler.postDelayed({
                        handleStartResult(EzdxBT.startTemperature().toString(), "Temperature")
                    }, 4000)
                } else {
                    handleStartResult("FAILED", "Thermometer Pairing")
                }
            }
            "BCA" -> {
                val age = intent.getIntExtra("age", 25).toFloat()
                val height = intent.getIntExtra("height", 170).toFloat()
                val isFemale = intent.getBooleanExtra("is_female", false)
                handleStartResult(
                    EzdxBT.startBCA(authKey, this, applicationContext as Application?, isFemale, age, height).toString(),
                    "Body Composition"
                )
            }
            "HEIGHT" -> handleStartResult(
                EzdxBT.startDigitalHeightScale(
                    authKey,
                    this,
                    this,
                    this::class.java,
                    applicationContext as Application?
                ).toString(),
                "Height"
            )
            else -> handleStartResult("FAILED", "Unknown Test")
        }
    }

    private fun handleStartResult(result: String, testName: String) {
        if (result == "SUCCESS") {
            tvStatus.text = "$testName started"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        } else {
            tvStatus.text = "$testName failed"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            resetTestState()
            showErrorDialog("Failed to start $testName test.")
        }
    }

    private fun stopTest() {
        isTestRunning = false
        btnStartTest.isEnabled = true
        btnStopTest.isEnabled = false
        progressBar.isVisible = false

        when (testType) {
            "HEIGHT" -> EzdxBT.stopDigitalHeightScale()
            "BCA" -> EzdxBT.stopBCA()
            else -> EzdxBT.stopCurrentTest()
        }

        tvStatus.text = "Test stopped"
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
    }

    private fun resetTestState() {
        isTestRunning = false
        btnStartTest.isEnabled = true
        btnStopTest.isEnabled = false
        progressBar.isVisible = false
    }

//    private fun showError(message: String) {
//        tvStatus.text = message
//        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
//        btnStartTest.isEnabled = false
//        showErrorDialog(message)
//    }
//
//    private fun showErrorDialog(message: String) {
//        AlertDialog.Builder(this)
//            .setTitle("Error")
//            .setMessage(message)
//            .setPositiveButton("OK", null)
//            .show()
//    }
//
//    private fun showExitConfirmation() {
//        AlertDialog.Builder(this)
//            .setTitle("Exit Test")
//            .setMessage("Test is running. Stop and exit?")
//            .setPositiveButton("Yes") { _, _ ->
//                stopTest()
//                finish()
//            }
//            .setNegativeButton("No", null)
//            .show()
//    }

//    override fun onEzdxData(ezdxData: EzdxData) {
//        runOnUiThread {
//            val status = ezdxData.status?.name ?: return@runOnUiThread
//            when (status) {
//                "TEST_COMPLETED" -> {
//                    val result = when (testType) {
//                        "PULSE_OXIMETRY" -> "SpO2: ${ezdxData.result1}%\nBPM: ${ezdxData.result2}"
//                        "BLOOD_PRESSURE" -> "Systolic: ${ezdxData.result1}\nDiastolic: ${ezdxData.result2}"
//                        "WEIGHT_TEMPERATURE" -> "Temperature: ${ezdxData.result1} °C"
//                        "BCA" -> "BCA Result: ${ezdxData.result1}"
//                        "HEIGHT" -> "Height: ${ezdxData.result1} cm"
//                        "GLUCOSE_TEST" -> "Glucose: ${ezdxData.result1} mg/dL"
//                        else -> "Result: ${ezdxData.result1}"
//                    }
//
//                    tvStatus.text = "Test Completed"
//                    tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
//                    tvResults.text = result
//                    tvResults.isVisible = true
//                    AlertDialog.Builder(this)
//                        .setTitle("Test Result")
//                        .setMessage(result)
//                        .setPositiveButton("OK", null)
//                        .show()
//                    resetTestState()
//                    when (testType) {
//                        "HEIGHT" -> EzdxBT.stopDigitalHeightScale()
//                        "BCA" -> EzdxBT.stopBCA()
//                        else -> EzdxBT.stopCurrentTest()
//                    }
//
//
//                }
//
//                "TEST_FAILED" -> {
//                    tvStatus.text = "Test failed: ${ezdxData.failedMsg ?: "Unknown error"}"
//                    tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
//                    resetTestState()
//                    EzdxBT.stopCurrentTest()
//                }
//
//                else -> {
//                    val display = status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
//                    tvStatus.text = display
//                    tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
//                }
//            }
//        }
//    }
    override fun onEzdxData(ezdxData: EzdxData) {
        Log.d(TAG, "Received EzdxData: ${ezdxData.status?.name}, Result1: ${ezdxData.result1}, Result2: ${ezdxData.result2}")

        runOnUiThread {
            val status = ezdxData.status?.name ?: return@runOnUiThread

            when (status) {
                "TEST_COMPLETED" -> {
                    Log.d(TAG, "Test completed successfully")

                    val result = formatTestResult(ezdxData)
                    updateStatus("Test completed successfully", StatusType.SUCCESS)
                    showResults(result)
                    resetTestState()
                    stopCurrentTest()
                }

                "TEST_FAILED" -> {
                    val errorMsg = ezdxData.failedMsg ?: "Unknown error occurred"
                    Log.e(TAG, "Test failed: $errorMsg")

                    updateStatus("Test failed: $errorMsg", StatusType.ERROR)
                    resetTestState()
                    stopCurrentTest()
                    showErrorDialog("Test failed: $errorMsg")
                }

                "TEST_IN_PROGRESS", "MEASURING", "PROCESSING" -> {
                    val displayStatus = status.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() }
                    updateStatus(displayStatus, StatusType.INFO)
                }

                else -> {
                    val displayStatus = status.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() }
                    updateStatus(displayStatus, StatusType.INFO)
                    Log.d(TAG, "Status update: $displayStatus")
                }
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    private fun copyResultToClipboard(result: String) {
        try {
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Test Result", result)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(this, "Result copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard", e)
        }
    }
    private fun formatTestResult(ezdxData: EzdxData): String {
        return when (testType) {
            "PULSE_OXIMETRY" -> buildString {
                appendLine("=== Pulse Oximetry Results ===")
                appendLine("SpO2: ${ezdxData.result1 ?: "N/A"}%")
                appendLine("Heart Rate: ${ezdxData.result2 ?: "N/A"} BPM")
                appendLine("Timestamp: ${getCurrentTimestamp()}")
            }

            "BLOOD_PRESSURE" -> buildString {
                appendLine("=== Blood Pressure Results ===")
                appendLine("Systolic: ${ezdxData.result1 ?: "N/A"} mmHg")
                appendLine("Diastolic: ${ezdxData.result2 ?: "N/A"} mmHg")
                appendLine("Pulse: ${ezdxData.result3 ?: "N/A"} BPM")
                appendLine("Timestamp: ${getCurrentTimestamp()}")
            }

            "WEIGHT_TEMPERATURE" -> buildString {
                appendLine("=== Temperature Results ===")
                appendLine("Temperature: ${ezdxData.result1 ?: "N/A"}°C")
                appendLine("Weight: ${ezdxData.result2 ?: "N/A"} kg")
                appendLine("Timestamp: ${getCurrentTimestamp()}")
            }

            "BCA" -> buildString {
                appendLine("=== Body Composition Analysis ===")
                appendLine("Weight: ${ezdxData.result1 ?: "N/A"} kg")
                appendLine("Body Fat: ${ezdxData.result2 ?: "N/A"}%")
                appendLine("Muscle Mass: ${ezdxData.result3 ?: "N/A"} kg")
                appendLine("Timestamp: ${getCurrentTimestamp()}")
            }

            "HEIGHT" -> buildString {
                appendLine("=== Height Measurement ===")
                appendLine("Height: ${ezdxData.result1 ?: "N/A"} cm")
                appendLine("Timestamp: ${getCurrentTimestamp()}")
            }

            "GLUCOSE_TEST" -> buildString {
                appendLine("=== Blood Glucose Results ===")
                appendLine("Glucose Level: ${ezdxData.result1 ?: "N/A"} mg/dL")
                appendLine("Timestamp: ${getCurrentTimestamp()}")
            }

            else -> buildString {
                appendLine("=== Test Results ===")
                appendLine("Result: ${ezdxData.result1 ?: "N/A"}")
                if (ezdxData.result2 != null) appendLine("Additional: ${ezdxData.result2}")
                appendLine("Timestamp: ${getCurrentTimestamp()}")
            }
        }
    }

    private fun showResults(result: String) {
        Log.d(TAG, "Showing results: $result")

        runOnUiThread {
            tvResults.text = result
            cardResults.isVisible = true
            tvResults.isVisible = true

            // Also show result dialog
            AlertDialog.Builder(this)
                .setTitle("Test Results")
                .setMessage(result)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton("Copy") { _, _ ->
                    copyResultToClipboard(result)
                }
                .show()
        }
    }

    enum class StatusType { SUCCESS, ERROR, WARNING, INFO }

    private fun updateStatus(message: String, type: StatusType) {
        Log.d(TAG, "Status update: $message ($type)")

        runOnUiThread {
            tvStatus.text = message
            tvStatus.setTextColor(ContextCompat.getColor(this, when (type) {
                StatusType.SUCCESS -> android.R.color.holo_green_dark
                StatusType.ERROR -> android.R.color.holo_red_dark
                StatusType.WARNING -> android.R.color.holo_orange_dark
                StatusType.INFO -> R.color.text_primary
            }))
        }
    }

    private fun showError(message: String) {
        updateStatus(message, StatusType.ERROR)
        showErrorDialog(message)
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Retry") { _, _ ->
//                if (!isDeviceAuthenticated) {
//                    authenticateDevice()
//                }
            }
            .show()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Exit Test")
            .setMessage("Test is currently running. Do you want to stop the test and exit?")
            .setPositiveButton("Yes") { _, _ ->
                stopTest()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onHCDeviceInfo(hcDeviceData: HCDeviceData?) {
        runOnUiThread {
            hcDeviceData?.let {
                tvStatus.text = "Device info received"
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                btnStartTest.isEnabled = true
            }
        }
    }

    override fun onStatusChange(status: com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus) {
        runOnUiThread {
            when (status) {
                com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus.CONNECTED -> {
                    // Optional UI feedback
                }
                com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus.NONE -> {
                    if (isTestRunning) {
                        tvStatus.text = "Bluetooth disconnected"
                        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                        resetTestState()
                        showErrorDialog("Device disconnected. Please reconnect.")
                    }
                }
                else -> Unit
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isTestRunning) showExitConfirmation()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        testHandler.removeCallbacksAndMessages(null)
        when (testType) {
            "HEIGHT" -> EzdxBT.stopDigitalHeightScale()
            "BCA" -> EzdxBT.stopBCA()
            else -> EzdxBT.stopCurrentTest()
        }
    }
}



//package com.codixly.docbot
//
//import android.app.AlertDialog
//import android.app.Application
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.widget.Button
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.cardview.widget.CardView
//import androidx.core.content.ContextCompat
//import androidx.core.view.isVisible
//import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
//import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
//import com.healthcubed.ezdxlib.bluetoothHandler.EzdxDataListener
//import com.healthcubed.ezdxlib.model.EzdxData
//import com.healthcubed.ezdxlib.model.HCDeviceData
//import com.healthcubed.ezdxlib.model.Status
//
//class TestExecutionActivity : AppCompatActivity(),
//    BluetoothService.OnBluetoothEventCallback,
//    EzdxDataListener {
//
//    companion object {
//        private const val TAG = "TestExecutionActivity"
//        private const val TEMPERATURE_PAIRING_DELAY = 4000L
//
//        // Intent extras
//        const val EXTRA_TEST_TYPE = "test_type"
//        const val EXTRA_AUTH_KEY = "auth_key"
//        const val EXTRA_AGE = "age"
//        const val EXTRA_HEIGHT = "height"
//        const val EXTRA_IS_FEMALE = "is_female"
//    }
//
//    // Test configuration
//    private lateinit var testType: String
//    private lateinit var authKey: String
//
//    // UI components
//    private lateinit var tvTestTitle: TextView
//    private lateinit var tvStatus: TextView
//    private lateinit var tvInstructions: TextView
//    private lateinit var tvResults: TextView
//    private lateinit var progressBar: ProgressBar
//    private lateinit var btnStartTest: Button
//    private lateinit var btnStopTest: Button
//    private lateinit var btnBack: Button
//    private lateinit var cardResults: CardView
//
//    // Test state
//    private var isTestRunning = false
//    private var isDeviceAuthenticated = false
//    private val testHandler = Handler(Looper.getMainLooper())
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
//        setContentView(R.layout.activity_test_execution)
//
//
//        testType = intent.getStringExtra("test_type") ?: "UNKNOWN"
//        authKey = intent.getStringExtra("auth_key") ?: "default-key"
//
////        testType = intent.getStringExtra(EXTRA_TEST_TYPE) ?: run {
////            Log.e(TAG, "Test type not provided in intent")
////            showErrorAndFinish("Test type not specified")
////            return
////        }
////
////        authKey = intent.getStringExtra(EXTRA_AUTH_KEY) ?: run {
////            Log.w(TAG, "Auth key not provided, using default")
////            "default-key"
////        }
//
////        Log.d(TAG, "Starting test execution for type: $testType")
//
//        initializeViews()
//        setupTestUI()
//        setupButtonListeners()
//        initializeBluetoothService()
//    }
//
//    private fun initializeViews() {
//        try {
//            tvTestTitle = findViewById(R.id.tv_test_title)
//            tvStatus = findViewById(R.id.tv_status)
//            tvInstructions = findViewById(R.id.tv_instructions)
//            tvResults = findViewById(R.id.tv_results)
//            progressBar = findViewById(R.id.progress_bar)
//            btnStartTest = findViewById(R.id.btn_start_test)
//            btnStopTest = findViewById(R.id.btn_stop_test)
//            btnBack = findViewById(R.id.btn_back)
//            cardResults = findViewById(R.id.card_results)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error initializing views", e)
//            showErrorAndFinish("UI initialization failed")
//        }
//    }
//
//    private fun setupTestUI() {
//        // Set test title
//        tvTestTitle.text = getTestDisplayName(testType)
//
//        // Set instructions
//        tvInstructions.text = getTestInstructions(testType)
//
//        // Initialize UI state
//        updateStatus("Initializing...", StatusType.INFO)
//        hideResults()
//        setButtonsEnabled(start = false, stop = false)
//    }
//
//    private fun getTestDisplayName(testType: String): String = when (testType) {
//        "PULSE_OXIMETRY" -> "Pulse Oximetry Test"
//        "WEIGHT_TEMPERATURE" -> "Weight / Temperature Test"
//        "BLOOD_PRESSURE" -> "Blood Pressure Test"
//        "BCA" -> "Body Composition Analysis"
//        "HEIGHT" -> "Height Test"
//        "GLUCOSE_TEST" -> "Blood Glucose Test"
//        else -> "Medical Test"
//    }
//
//    private fun getTestInstructions(testType: String): String = when (testType) {
//        "PULSE_OXIMETRY" -> "1. Connect sensor probe\n2. Place finger firmly\n3. Remain still during measurement\n4. Wait for results"
//        "WEIGHT_TEMPERATURE" -> "1. Power on device\n2. Ensure proper pairing\n3. Take measurement\n4. Wait for reading"
//        "BLOOD_PRESSURE" -> "1. Attach cuff to upper arm\n2. Relax arm at heart level\n3. Remain calm and still\n4. Wait for completion"
//        "BCA" -> "1. Stand barefoot on scale\n2. Hold handles firmly\n3. Stay steady during scan\n4. Wait for analysis"
//        "HEIGHT" -> "1. Step on scale platform\n2. Stand straight and tall\n3. Press measure button\n4. Wait for result"
//        "GLUCOSE_TEST" -> "1. Insert test strip\n2. Apply blood sample to strip\n3. Wait for analysis\n4. Read result"
//        else -> "Follow the on-screen instructions for this test"
//    }
//
//    private fun initializeBluetoothService() {
//        try {
//            BluetoothService.getDefaultInstance().setOnEventCallback(this)
//            authenticateDevice()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error initializing Bluetooth service", e)
//            showError("Failed to initialize Bluetooth service")
//        }
//    }
//
//    private fun authenticateDevice() {
//        updateStatus("Authenticating device...", StatusType.INFO)
//
//        try {
//            val authResult = EzdxBT.authenticate(authKey)
//            Log.d(TAG, "Authentication result: $authResult")
//
//            when (authResult) {
//                Status.SUCCESS -> {
//                    isDeviceAuthenticated = true
//                    updateStatus("Device authenticated successfully", StatusType.SUCCESS)
//                    setButtonsEnabled(start = true, stop = false)
//                }
//                Status.BLUETOOTH_NOT_CONNECTED -> {
//                    showError("Bluetooth not connected. Please check device connection.")
//                }
//                Status.INVALID_KEY -> {
//                    showError("Invalid authentication key. Please check configuration.")
//                }
//                else -> {
//                    showError("Authentication failed: ${authResult.name}")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception during authentication", e)
//            showError("Authentication error: ${e.message}")
//        }
//    }
//
//    private fun setupButtonListeners() {
//        btnStartTest.setOnClickListener {
//            Log.d(TAG, "Start test button clicked")
//            startTest()
//        }
//
//        btnStopTest.setOnClickListener {
//            Log.d(TAG, "Stop test button clicked")
//            stopTest()
//        }
//
//        btnBack.setOnClickListener {
//            if (isTestRunning) {
//                showExitConfirmation()
//            } else {
//                finish()
//            }
//        }
//    }
//
//    private fun startTest() {
//        if (!isDeviceAuthenticated) {
//            showError("Device not authenticated. Please wait for authentication to complete.")
//            return
//        }
//
//        Log.d(TAG, "Starting test: $testType")
//
//        isTestRunning = true
//        setButtonsEnabled(start = false, stop = true)
//        showProgress(true)
//        hideResults()
//
//        try {
//            when (testType) {
//                "PULSE_OXIMETRY" -> startPulseOximetryTest()
//                "BLOOD_PRESSURE" -> startBloodPressureTest()
//                "GLUCOSE_TEST" -> startGlucoseTest()
//                "WEIGHT_TEMPERATURE" -> startTemperatureTest()
//                "BCA" -> startBCATest()
//                "HEIGHT" -> startHeightTest()
//                else -> {
//                    Log.e(TAG, "Unknown test type: $testType")
//                    handleTestStartFailure("Unknown test type")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception starting test", e)
//            handleTestStartFailure("Error starting test: ${e.message}")
//        }
//    }
//
//    private fun startPulseOximetryTest() {
//        updateStatus("Starting pulse oximetry measurement...", StatusType.INFO)
//        val result = EzdxBT.startPulseOximetry()
//        handleTestStartResult(result, "Pulse Oximetry")
//    }
//
//    private fun startBloodPressureTest() {
//        updateStatus("Starting blood pressure measurement...", StatusType.INFO)
//        val result = EzdxBT.startAdultBloodPressure()
//        handleTestStartResult(result, "Blood Pressure")
//    }
//
//    private fun startGlucoseTest() {
//        updateStatus("Starting blood glucose measurement...", StatusType.INFO)
//        val result = EzdxBT.startBloodGlucose()
//        handleTestStartResult(result, "Blood Glucose")
//    }
//
//    private fun startTemperatureTest() {
//        updateStatus("Pairing with thermometer...", StatusType.INFO)
//        val pairingResult = EzdxBT.startTemperaturePairing()
//
//        if (pairingResult == Status.SUCCESS) {
//            updateStatus("Thermometer paired. Starting temperature measurement...", StatusType.INFO)
//            testHandler.postDelayed({
//                val tempResult = EzdxBT.startTemperature()
//                handleTestStartResult(tempResult, "Temperature")
//            }, TEMPERATURE_PAIRING_DELAY)
//        } else {
//            handleTestStartFailure("Failed to pair thermometer")
//        }
//    }
//
//    private fun startBCATest() {
//        val age = intent.getIntExtra(EXTRA_AGE, 25).toFloat()
//        val height = intent.getIntExtra(EXTRA_HEIGHT, 170).toFloat()
//        val isFemale = intent.getBooleanExtra(EXTRA_IS_FEMALE, false)
//
//        updateStatus("Starting body composition analysis...", StatusType.INFO)
//        Log.d(TAG, "BCA parameters - Age: $age, Height: $height, Female: $isFemale")
//
//        val result = EzdxBT.startBCA(authKey, this, applicationContext as Application?, isFemale, age, height)
//        handleTestStartResult(result, "Body Composition Analysis")
//    }
//
//    private fun startHeightTest() {
//        updateStatus("Starting height measurement...", StatusType.INFO)
//        val result = EzdxBT.startDigitalHeightScale(
//            authKey,
//            this,
//            this,
//            this::class.java,
//            applicationContext as Application?
//        )
//        handleTestStartResult(result, "Height Measurement")
//    }
//
//    private fun handleTestStartResult(result: Status, testName: String) {
//        Log.d(TAG, "$testName start result: $result")
//
//        if (result == Status.SUCCESS) {
//            updateStatus("$testName test in progress...", StatusType.INFO)
//        } else {
//            handleTestStartFailure("Failed to start $testName test: ${result.name}")
//        }
//    }
//
//    private fun handleTestStartFailure(message: String) {
//        Log.e(TAG, "Test start failure: $message")
//        updateStatus(message, StatusType.ERROR)
//        resetTestState()
//        showErrorDialog(message)
//    }
//
//    private fun stopTest() {
//        Log.d(TAG, "Stopping test: $testType")
//
//        isTestRunning = false
//        setButtonsEnabled(start = true, stop = false)
//        showProgress(false)
//
//        try {
//            when (testType) {
//                "HEIGHT" -> EzdxBT.stopDigitalHeightScale()
//                "BCA" -> EzdxBT.stopBCA()
//                else -> EzdxBT.stopCurrentTest()
//            }
//            updateStatus("Test stopped by user", StatusType.WARNING)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error stopping test", e)
//            updateStatus("Error stopping test", StatusType.ERROR)
//        }
//    }
//
//    private fun resetTestState() {
//        isTestRunning = false
//        setButtonsEnabled(start = isDeviceAuthenticated, stop = false)
//        showProgress(false)
//    }
//
//    // Enhanced result display methods
//    private fun showResults(result: String) {
//        Log.d(TAG, "Showing results: $result")
//
//        runOnUiThread {
//            tvResults.text = result
//            cardResults.isVisible = true
//            tvResults.isVisible = true
//
//            // Also show result dialog
//            AlertDialog.Builder(this)
//                .setTitle("Test Results")
//                .setMessage(result)
//                .setPositiveButton("OK") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .setNeutralButton("Copy") { _, _ ->
//                    copyResultToClipboard(result)
//                }
//                .show()
//        }
//    }
//
//    private fun hideResults() {
//        cardResults.isVisible = false
//        tvResults.isVisible = false
//    }
//
//    private fun copyResultToClipboard(result: String) {
//        try {
//            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
//            val clip = android.content.ClipData.newPlainText("Test Result", result)
//            clipboard.setPrimaryClip(clip)
//            android.widget.Toast.makeText(this, "Result copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error copying to clipboard", e)
//        }
//    }
//
//    // Enhanced status management
//    enum class StatusType { SUCCESS, ERROR, WARNING, INFO }
//
//    private fun updateStatus(message: String, type: StatusType) {
//        Log.d(TAG, "Status update: $message ($type)")
//
//        runOnUiThread {
//            tvStatus.text = message
//            tvStatus.setTextColor(ContextCompat.getColor(this, when (type) {
//                StatusType.SUCCESS -> android.R.color.holo_green_dark
//                StatusType.ERROR -> android.R.color.holo_red_dark
//                StatusType.WARNING -> android.R.color.holo_orange_dark
//                StatusType.INFO -> R.color.text_primary
//            }))
//        }
//    }
//
//    private fun setButtonsEnabled(start: Boolean, stop: Boolean) {
//        runOnUiThread {
//            btnStartTest.isEnabled = start
//            btnStopTest.isEnabled = stop
//        }
//    }
//
//    private fun showProgress(show: Boolean) {
//        runOnUiThread {
//            progressBar.isVisible = show
//        }
//    }
//
//    private fun showError(message: String) {
//        updateStatus(message, StatusType.ERROR)
//        showErrorDialog(message)
//    }
//
//    private fun showErrorDialog(message: String) {
//        AlertDialog.Builder(this)
//            .setTitle("Error")
//            .setMessage(message)
//            .setPositiveButton("OK", null)
//            .setNeutralButton("Retry") { _, _ ->
//                if (!isDeviceAuthenticated) {
//                    authenticateDevice()
//                }
//            }
//            .show()
//    }
//
//    private fun showErrorAndFinish(message: String) {
//        AlertDialog.Builder(this)
//            .setTitle("Error")
//            .setMessage(message)
//            .setPositiveButton("OK") { _, _ -> finish() }
//            .setCancelable(false)
//            .show()
//    }
//
//    private fun showExitConfirmation() {
//        AlertDialog.Builder(this)
//            .setTitle("Exit Test")
//            .setMessage("Test is currently running. Do you want to stop the test and exit?")
//            .setPositiveButton("Yes") { _, _ ->
//                stopTest()
//                finish()
//            }
//            .setNegativeButton("No", null)
//            .show()
//    }
//
//    // Enhanced data callback with better result formatting
//    override fun onEzdxData(ezdxData: EzdxData) {
//        Log.d(TAG, "Received EzdxData: ${ezdxData.status?.name}, Result1: ${ezdxData.result1}, Result2: ${ezdxData.result2}")
//
//        runOnUiThread {
//            val status = ezdxData.status?.name ?: return@runOnUiThread
//
//            when (status) {
//                "TEST_COMPLETED" -> {
//                    Log.d(TAG, "Test completed successfully")
//
//                    val result = formatTestResult(ezdxData)
//                    updateStatus("Test completed successfully", StatusType.SUCCESS)
//                    showResults(result)
//                    resetTestState()
//                    stopCurrentTest()
//                }
//
//                "TEST_FAILED" -> {
//                    val errorMsg = ezdxData.failedMsg ?: "Unknown error occurred"
//                    Log.e(TAG, "Test failed: $errorMsg")
//
//                    updateStatus("Test failed: $errorMsg", StatusType.ERROR)
//                    resetTestState()
//                    stopCurrentTest()
//                    showErrorDialog("Test failed: $errorMsg")
//                }
//
//                "TEST_IN_PROGRESS", "MEASURING", "PROCESSING" -> {
//                    val displayStatus = status.replace("_", " ").lowercase()
//                        .replaceFirstChar { it.uppercase() }
//                    updateStatus(displayStatus, StatusType.INFO)
//                }
//
//                else -> {
//                    val displayStatus = status.replace("_", " ").lowercase()
//                        .replaceFirstChar { it.uppercase() }
//                    updateStatus(displayStatus, StatusType.INFO)
//                    Log.d(TAG, "Status update: $displayStatus")
//                }
//            }
//        }
//    }
//
//    private fun formatTestResult(ezdxData: EzdxData): String {
//        return when (testType) {
//            "PULSE_OXIMETRY" -> buildString {
//                appendLine("=== Pulse Oximetry Results ===")
//                appendLine("SpO2: ${ezdxData.result1 ?: "N/A"}%")
//                appendLine("Heart Rate: ${ezdxData.result2 ?: "N/A"} BPM")
//                appendLine("Timestamp: ${getCurrentTimestamp()}")
//            }
//
//            "BLOOD_PRESSURE" -> buildString {
//                appendLine("=== Blood Pressure Results ===")
//                appendLine("Systolic: ${ezdxData.result1 ?: "N/A"} mmHg")
//                appendLine("Diastolic: ${ezdxData.result2 ?: "N/A"} mmHg")
//                appendLine("Pulse: ${ezdxData.result3 ?: "N/A"} BPM")
//                appendLine("Timestamp: ${getCurrentTimestamp()}")
//            }
//
//            "WEIGHT_TEMPERATURE" -> buildString {
//                appendLine("=== Temperature Results ===")
//                appendLine("Temperature: ${ezdxData.result1 ?: "N/A"}°C")
//                appendLine("Weight: ${ezdxData.result2 ?: "N/A"} kg")
//                appendLine("Timestamp: ${getCurrentTimestamp()}")
//            }
//
//            "BCA" -> buildString {
//                appendLine("=== Body Composition Analysis ===")
//                appendLine("Weight: ${ezdxData.result1 ?: "N/A"} kg")
//                appendLine("Body Fat: ${ezdxData.result2 ?: "N/A"}%")
//                appendLine("Muscle Mass: ${ezdxData.result3 ?: "N/A"} kg")
//                appendLine("Timestamp: ${getCurrentTimestamp()}")
//            }
//
//            "HEIGHT" -> buildString {
//                appendLine("=== Height Measurement ===")
//                appendLine("Height: ${ezdxData.result1 ?: "N/A"} cm")
//                appendLine("Timestamp: ${getCurrentTimestamp()}")
//            }
//
//            "GLUCOSE_TEST" -> buildString {
//                appendLine("=== Blood Glucose Results ===")
//                appendLine("Glucose Level: ${ezdxData.result1 ?: "N/A"} mg/dL")
//                appendLine("Timestamp: ${getCurrentTimestamp()}")
//            }
//
//            else -> buildString {
//                appendLine("=== Test Results ===")
//                appendLine("Result: ${ezdxData.result1 ?: "N/A"}")
//                if (ezdxData.result2 != null) appendLine("Additional: ${ezdxData.result2}")
//                appendLine("Timestamp: ${getCurrentTimestamp()}")
//            }
//        }
//    }
//
//    private fun getCurrentTimestamp(): String {
//        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
//            .format(java.util.Date())
//    }
//
//    private fun stopCurrentTest() {
//        try {
//            when (testType) {
//                "HEIGHT" -> EzdxBT.stopDigitalHeightScale()
//                "BCA" -> EzdxBT.stopBCA()
//                else -> EzdxBT.stopCurrentTest()
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error stopping test", e)
//        }
//    }
//
//    override fun onHCDeviceInfo(hcDeviceData: HCDeviceData?) {
//        Log.d(TAG, "Device info received: ${hcDeviceData?.serialNumber}")
//
//        runOnUiThread {
//            hcDeviceData?.let {
//                updateStatus("Device information received", StatusType.SUCCESS)
//                if (!isDeviceAuthenticated) {
//                    setButtonsEnabled(start = true, stop = false)
//                }
//            }
//        }
//    }
//
//    override fun onStatusChange(status: com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus) {
//        Log.d(TAG, "Bluetooth status changed: ${status.name}")
//
//        runOnUiThread {
//            when (status) {
//                com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus.CONNECTED -> {
//                    if (!isDeviceAuthenticated) {
//                        authenticateDevice()
//                    }
//                }
//                com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus.NONE -> {
//                    isDeviceAuthenticated = false
//                    if (isTestRunning) {
//                        updateStatus("Device disconnected during test", StatusType.ERROR)
//                        resetTestState()
//                        showErrorDialog("Device disconnected. Please reconnect and try again.")
//                    } else {
//                        updateStatus("Device disconnected", StatusType.WARNING)
//                        setButtonsEnabled(start = false, stop = false)
//                    }
//                }
//                else -> Unit
//            }
//        }
//    }
//
//    override fun onBackPressed() {
//        if (isTestRunning) {
//            showExitConfirmation()
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG, "Activity destroyed")
//
//        testHandler.removeCallbacksAndMessages(null)
//
//        try {
//            stopCurrentTest()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in onDestroy", e)
//        }
//    }
//}