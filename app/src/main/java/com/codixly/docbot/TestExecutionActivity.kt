package com.codixly.docbot

import android.app.AlertDialog
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT.stopCurrentTest
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxDataListener
import com.healthcubed.ezdxlib.model.EzdxData
import com.healthcubed.ezdxlib.model.HCDeviceData
import com.healthcubed.ezdxlib.model.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private var testStartTime: Long = 0
    private var temperaturePairingJob: Runnable? = null

    companion object {
        private const val TAG = "TestExecutionActivity"
        private const val TEMPERATURE_PAIRING_DELAY = 4000L
        private const val TEST_TIMEOUT = 60000L // 60 seconds timeout

        // Test type constants
        const val TEST_PULSE_OXIMETRY = "PULSE_OXIMETRY"
        const val TEST_TEMPERATURE = "TEMPERATURE"
        const val TEST_BLOOD_PRESSURE = "BLOOD_PRESSURE"
        const val TEST_BCA = "BCA"
        const val TEST_HEIGHT = "HEIGHT"
        const val TEST_GLUCOSE = "GLUCOSE_TEST"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        setContentView(R.layout.activity_test_execution)

        enableEdgeToEdge()

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
        tvTestTitle.text = getTestTitle(testType)
        tvInstructions.text = getTestInstructions(testType)

        tvStatus.text = "Authenticating device..."
        tvResults.isVisible = false
        cardResults.isVisible = false
        btnStartTest.isEnabled = false
        btnStopTest.isEnabled = false
    }

    private fun getTestTitle(testType: String): String = when (testType) {
        TEST_PULSE_OXIMETRY -> "Pulse Oximetry Test"
        TEST_TEMPERATURE -> "Temperature Test"
        TEST_BLOOD_PRESSURE -> "Blood Pressure Test"
        TEST_BCA -> "Body Composition Analysis"
        TEST_HEIGHT -> "Height Test"
        TEST_GLUCOSE -> "Blood Glucose Test"
        else -> "Unknown Test"
    }

    private fun getTestInstructions(testType: String): String = when (testType) {
        TEST_PULSE_OXIMETRY -> "1. Ensure sensor is connected\n2. Place finger firmly on sensor\n3. Keep hand still during measurement\n4. Breathe normally"
        TEST_TEMPERATURE -> "1. Power on the device\n2. Wait for pairing confirmation\n3. Step on scale for weight\n4. Use thermometer for temperature"
        TEST_BLOOD_PRESSURE -> "1. Attach cuff to upper arm\n2. Sit comfortably, relax arm\n3. Keep arm at heart level\n4. Remain still and quiet"
        TEST_BCA -> "1. Remove shoes and socks\n2. Stand barefoot on scale\n3. Hold handles firmly\n4. Remain still during analysis"
        TEST_HEIGHT -> "1. Step onto the scale platform\n2. Stand straight and still\n3. Press measure button\n4. Wait for stable reading"
        TEST_GLUCOSE -> "1. Insert test strip into meter\n2. Clean finger with alcohol\n3. Apply blood drop to strip\n4. Wait for result display"
        else -> "No instructions available"
    }

    private fun authenticateDevice() {
        updateStatus("Authenticating device...", StatusType.INFO)

        try {
            val authResult = EzdxBT.authenticate(authKey)
            handleAuthenticationResult(authResult)
        } catch (e: Exception) {
            Toast.makeText(this, "Authentication error: ${e.message}", Toast.LENGTH_LONG).show()
            showError("Authentication error: ${e.message}")
        }
    }

    private fun handleAuthenticationResult(status: Status) {
        when (status) {
            Status.SUCCESS -> {
                updateStatus("Device authenticated successfully", StatusType.SUCCESS)
                btnStartTest.isEnabled = true
                Toast.makeText(this, "Device authentication successful", Toast.LENGTH_SHORT).show()
            }

            Status.BLUETOOTH_NOT_CONNECTED -> {
                showError("Bluetooth not connected. Please check connection.")
            }

            Status.INVALID_KEY -> {
                showError("Invalid authentication key. Please verify credentials.")
            }

            else -> {
                showError("Authentication failed. Please try again.")
            }
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
        Toast.makeText(this, "Starting test: $testType", Toast.LENGTH_SHORT).show()

        if (!validateTestPreconditions()) {
            return
        }

        initializeTestState()
        scheduleTestTimeout()

        try {
            executeTestByType()
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting test: $testType - ${e.message}", Toast.LENGTH_LONG)
                .show()
            handleStartResult("FAILED", "Test Initialization")
//            showErrorDialog("Failed to start test: ${e.message}")
        }
    }

    private fun validateTestPreconditions(): Boolean {
        if (isTestRunning) {
            Toast.makeText(this, "Test already running", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!btnStartTest.isEnabled) {
            Toast.makeText(this, "Test button not enabled", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun initializeTestState() {
        testStartTime = System.currentTimeMillis()
        isTestRunning = true
        btnStartTest.isEnabled = false
        btnStopTest.isEnabled = true
        progressBar.isVisible = true
        tvResults.isVisible = false
        cardResults.isVisible = false
    }

    private fun scheduleTestTimeout() {
        testHandler.postDelayed({
            if (isTestRunning) {
                Toast.makeText(this, "Test timeout reached", Toast.LENGTH_LONG).show()
                handleTestTimeout()
            }
        }, TEST_TIMEOUT)
    }

    private fun executeTestByType() {
        when (testType) {
            TEST_PULSE_OXIMETRY -> startPulseOximetryTest()
            TEST_BLOOD_PRESSURE -> startBloodPressureTest()
            TEST_GLUCOSE -> startGlucoseTest()
            TEST_TEMPERATURE -> startTemperatureTest()
            TEST_BCA -> startBCATest()
            TEST_HEIGHT -> startHeightTest()
            else -> {
                Toast.makeText(this, "Unknown test type: $testType", Toast.LENGTH_LONG).show()
                handleStartResult("FAILED", "Unknown Test Type")
            }
        }
    }

    private fun startPulseOximetryTest() {
        updateStatus("Starting pulse oximetry measurement...", StatusType.INFO)
        val result = EzdxBT.startPulseOximetry()
        handleStartResult(result.toString(), "Pulse Oximetry")
    }

    private fun startBloodPressureTest() {
        updateStatus("Starting blood pressure measurement...", StatusType.INFO)
        val result = EzdxBT.startAdultBloodPressure()
        handleStartResult(result.toString(), "Blood Pressure")
    }

    private fun startGlucoseTest() {
        updateStatus("Starting blood glucose test...", StatusType.INFO)
        val result = EzdxBT.startBloodGlucose()
        handleStartResult(result.toString(), "Blood Glucose")
        Toast.makeText(this, "Glucose test initiated: $result", Toast.LENGTH_SHORT).show()
    }

    private fun startTemperatureTest() {
        updateStatus("Pairing with thermometer device...", StatusType.INFO)

        try {
            val pairingResult = EzdxBT.startTemperaturePairing()

            if (pairingResult.toString() == "SUCCESS") {
                updateStatus("Pairing successful. Starting measurements...", StatusType.SUCCESS)
                scheduleTemperatureStart()
            } else {
                handleStartResult("FAILED", "Thermometer Pairing")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Temperature pairing error: ${e.message}", Toast.LENGTH_LONG)
                .show()
            handleStartResult("FAILED", "Temperature Setup")
        }
    }

    private fun scheduleTemperatureStart() {
        temperaturePairingJob = Runnable {
            if (isTestRunning) {
                try {
                    val tempResult = EzdxBT.startTemperature()
                    handleStartResult(tempResult.toString(), "Weight/Temperature")
                } catch (e: Exception) {
                    Toast.makeText(this, "Temperature start error: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    handleStartResult("FAILED", "Temperature Start")
                }
            }
        }
        testHandler.postDelayed(temperaturePairingJob!!, TEMPERATURE_PAIRING_DELAY)
    }

//    private fun startBCATest() {
//        updateStatus("Starting body composition analysis...", StatusType.INFO)
//
//        val age = intent.getIntExtra("age", 25)
//        val height = intent.getIntExtra("height", 170)
//        val isFemale = intent.getBooleanExtra("is_female", false)
//
//        Toast.makeText(this, "Please stand barefoot on metal plates", Toast.LENGTH_LONG).show()
//
//        try {
//            val result = EzdxBT.startBCA(
//                authKey,
//                this,  // EzdxDataListener
//                applicationContext as Application?,
//                isFemale,
//                age.toFloat(),
//                height.toFloat()
//            )
//            handleStartResult(result.toString(), "Body Composition Analysis")
//        } catch (e: Exception) {
//            Toast.makeText(this, "Error starting BCA test: ${e.message}", Toast.LENGTH_LONG).show()
//            handleStartResult("FAILED", "BCA Initialization")
//            showErrorDialog("Error starting BCA: ${e.message}")
//        }
//    }

//    private fun startBCATest() {
//        updateStatus("Waiting for user to step on BCA machine...", StatusType.INFO)
//        Toast.makeText(this, "Please step barefoot on BCA machine first", Toast.LENGTH_LONG).show()
//
//        // Add a 3-second delay before starting the test
//        testHandler.postDelayed({
//            updateStatus("Starting body composition analysis...", StatusType.INFO)
//
//            val age = intent.getIntExtra("age", 25)
//            val height = intent.getIntExtra("height", 170)
//            val isFemale = intent.getBooleanExtra("is_female", false)
//
//            try {
//                val result = EzdxBT.startBCA(
//                    authKey,
//                    this,
//                    applicationContext as Application?,
//                    isFemale,
//                    age.toFloat(),
//                    height.toFloat()
//                )
//                handleStartResult(result.toString(), "Body Composition Analysis")
//            } catch (e: Exception) {
//                Toast.makeText(this, "Error starting BCA test: ${e.message}", Toast.LENGTH_LONG).show()
//                handleStartResult("FAILED", "BCA Initialization")
//                showErrorDialog("Error starting BCA: ${e.message}")
//            }
//        }, 3000)
//    }

    private fun startBCATest() {
        updateStatus("Preparing Body Composition Analysis...", StatusType.INFO)
        Toast.makeText(this, "Please step barefoot on the BCA machine.", Toast.LENGTH_LONG).show()

        testHandler.postDelayed({
            updateStatus("Starting Body Composition Analysis...", StatusType.INFO)

            val age = intent.getIntExtra("age", 25).toFloat()
            val height = intent.getIntExtra("height", 170).toFloat()
            val isFemale = intent.getBooleanExtra("is_female", false)

            try {
                val result = EzdxBT.startBCA(
                    authKey,
                    this, // EzdxDataListener
                    applicationContext as Application?,
                    isFemale,
                    age,
                    height
                )
                handleStartResult(result.toString(), "Body Composition Analysis")
            } catch (e: Exception) {
                val errorMessage = "Failed to start BCA test: ${e.message}"
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                handleStartResult("FAILED", "BCA Initialization")
//                showErrorDialog(errorMessage)
            }
        }, 5000)
    }

    //    private fun startHeightTest() {
//        updateStatus("Preparing Height Measurement...", StatusType.INFO)
//        Toast.makeText(this, "Please step onto the height scale and remain still.", Toast.LENGTH_LONG).show()
//
//        testHandler.postDelayed({
//            try {
//                val application = applicationContext as? Application
//                if (application == null) {
//                    val error = "Application context is null"
//                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
//                    handleStartResult("FAILED", "Height Initialization")
//                    showErrorDialog(error)
//                    return@postDelayed
//                }
//
//                val result = EzdxBT.startDigitalHeightScale(
//                    authKey,
//                    this,
//                    this,
//                    TestExecutionActivity::class.java,
//                    application
//                )
//
//                if (result.toString() == "SUCCESS") {
//                    updateStatus("Height measurement started. Please stand still.", StatusType.SUCCESS)
//                    handleStartResult(result.toString(), "Height Measurement")
//                } else {
//                    val errorMessage = "Failed to start Height test: $result"
//                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
//                    handleStartResult("FAILED", "Height Initialization")
//                    showErrorDialog(errorMessage)
//                }
//            } catch (e: Exception) {
//                val errorMessage = "Exception during height test start: ${e.message}"
//                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
//                handleStartResult("FAILED", "Height Exception")
//                showErrorDialog(errorMessage)
//            }
//        }, 4000) // let the UI and machine settle
//    }
    // above code of jaydeep

// this code is worked
//    private fun startHeightTest() {
//        val tag = "HeightTest"
//        val preparingMsg = "Preparing Height Measurement..."
//        Log.d(tag, preparingMsg)
//        updateStatus(preparingMsg, StatusType.INFO)
//        Toast.makeText(this, "Preparing Height Measurement...", Toast.LENGTH_SHORT).show()
//        Toast.makeText(
//            this,
//            "Please step onto the height scale and remain still.",
//            Toast.LENGTH_LONG
//        ).show()
//
//        if (authKey.isEmpty()) {
//            Toast.makeText(this, "Authentication key is invalid.", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        testHandler.postDelayed({
//            try {
//                val application = this.application
//                if (application == null) {
//                    val error = "Application context is null"
//                    Log.e(tag, error)
//                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
//                    handleStartResult("FAILED", "Height Initialization")
//                    showErrorDialog(error)
//                    return@postDelayed
//                }
//
//                Log.d(tag, "Starting digital height scale...")
//                Toast.makeText(this, "Starting digital height scale...", Toast.LENGTH_SHORT).show()
//                val result = EzdxBT.startDigitalHeightScale(
//                    authKey,
//                    this, // EzdxDataListener
//                    this,
//                    TestExecutionActivity::class.java,
//                    application
//                )
//
//                if (result.toString() == "SUCCESS") {
//                    val successMsg = "Height measurement started. Please stand still."
//                    Log.d(tag, successMsg)
//                    updateStatus(successMsg, StatusType.SUCCESS)
//                    Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show()
//                    handleStartResult(result.toString(), "Height Measurement")
//                } else {
//                    val errorMessage = "Failed to start Height test: $result"
//                    Log.e(tag, errorMessage)
//                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
//                    handleStartResult("FAILED", "Height Initialization")
//                    showErrorDialog(errorMessage)
//                }
//            } catch (e: Exception) {
//                val errorMessage = "Exception during height test start: ${e.message}"
//                Log.e(tag, errorMessage)
//                Log.e(tag, Log.getStackTraceString(e))
//                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
//                handleStartResult("FAILED", "Height Exception")
//                showErrorDialog(errorMessage)
//            }
//        }, 4000) // let the UI and machine settle
//    }

    // this is send for the testing
    private fun startHeightTest() {
        val tag = "HeightTest"
        Log.d(tag, "Preparing Height Measurement...")

        updateStatus("Preparing Height Measurement...", StatusType.INFO)
        Toast.makeText(this, "Preparing Height Measurement...", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Please step onto the height scale and remain still.", Toast.LENGTH_LONG).show()

        if (authKey.isEmpty()) {
            Toast.makeText(this, "Authentication key is invalid.", Toast.LENGTH_LONG).show()
            return
        }


        // Delay start for better device readiness
        testHandler.postDelayed({
            try {
                val application = this.application ?: run {
                    val error = "Application context is null"
                    Log.e(tag, error)
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    handleStartResult("FAILED", "Height Initialization")
//                    showErrorDialog(error)
                    return@postDelayed
                }

                Log.d(tag, "Starting digital height scale...")
                Toast.makeText(this, "Starting digital height scale...", Toast.LENGTH_SHORT).show()

                val result = EzdxBT.startDigitalHeightScale(
                    authKey,
                    this, // EzdxDataListener
                    this,
                    TestExecutionActivity::class.java,
                    application
                )

                if (result.toString().equals("SUCCESS", ignoreCase = true)) {
                    val msg = "Height measurement started. Please stand still."
                    Log.d(tag, msg)
                    updateStatus(msg, StatusType.SUCCESS)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    handleStartResult(result.toString(), "Height Measurement")
                } else {
                    val error = "Failed to start Height test: $result"
                    Log.e(tag, error)
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    handleStartResult("FAILED", "Height Initialization")
//                    showErrorDialog("Please ensure the device is ready and try again.")
                }
            } catch (e: Exception) {
                val error = "Exception during height test start: ${e.message}"
                Log.e(tag, error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                handleStartResult("FAILED", "Height Exception")
//                showErrorDialog(error)
            }
        }, 6000) // Increased from 4000 to 6000 for more stability
    }
//
//
//    private fun formatHeightResult(ezdxData: EzdxData): String = buildString {
//        appendLine("=== Height Measurement ===")
//        appendLine("Height: ${ezdxData.result1 ?: "N/A"} cm")
//        if (ezdxData.result2 != null) appendLine("Height (ft): ${ezdxData.result2}")
//        appendLine("Test Duration: ${getTestDuration()}")
//        appendLine("Timestamp: ${getCurrentTimestamp()}")
//    }
//    private fun startHeightTest() {
//        val tag = "HeightTest"
//        val preparingMsg = "Preparing Height Measurement..."
//        Log.d(tag, preparingMsg)
//        updateStatus(preparingMsg, StatusType.INFO)
//        Toast.makeText(this, "Preparing Height Measurement...", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, "Please step onto the height scale and remain still.", Toast.LENGTH_LONG).show()
//
//        if (authKey.isEmpty()) {
//            Toast.makeText(this, "Authentication key is invalid.", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        testHandler.postDelayed({
//            try {
//                val application = this.application
//                if (application == null) {
//                    val error = "Application context is null"
//                    Log.e(tag, error)
//                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
//                    handleStartResult("FAILED", "Height Initialization")
//                    showErrorDialog(error)
//                    return@postDelayed
//                }
//
//                Log.d(tag, "Starting digital height scale...")
//                Toast.makeText(this, "Starting digital height scale...", Toast.LENGTH_SHORT).show()
//                val result = EzdxBT.startDigitalHeightScale(
//                    authKey,
//                    this, // EzdxDataListener
//                    this,
//                    TestExecutionActivity::class.java,
//                    application
//                )
//
//                if (result.toString() == "SUCCESS") {
//                    val successMsg = "Height measurement started. Please stand still."
//                    Log.d(tag, successMsg)
//                    updateStatus(successMsg, StatusType.SUCCESS)
//                    Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show()
//                    handleStartResult(result.toString(), "Height Measurement")
//                } else {
//                    handleStartFailure(result.toString())
//                }
//            } catch (e: Exception) {
//                val errorMessage = "Exception during height test start: ${e.message}"
//                Log.e(tag, errorMessage)
//                Log.e(tag, Log.getStackTraceString(e))
//                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
//                handleStartResult("FAILED", "Height Exception")
//                showErrorDialog(errorMessage)
//            }
//        }, 6000) // let the UI and machine settle
//    }

//    private fun handleStartFailure(result: String) {
//        val tag = "HeightTest"
//        val errorMessage = "Failed to start Height test: $result"
//        Log.e(tag, errorMessage)
//        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
//
//        // Retry logic
//        val retryCount = 1 // Number of retries
//        for (i in 1..retryCount) {
//            Log.d(tag, "Retrying to start height test... Attempt: $i")
//            val retryResult = EzdxBT.startDigitalHeightScale(
//                authKey,
//                this, // EzdxDataListener
//                this,
//                TestExecutionActivity::class.java,
//                this.application
//            )
//
//            if (retryResult.toString() == "SUCCESS") {
//                val successMsg = "Height measurement started on retry. Please stand still."
//                Log.d(tag, successMsg)
//                updateStatus(successMsg, StatusType.SUCCESS)
//                Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show()
//                handleStartResult(retryResult.toString(), "Height Measurement")
//                return
//            }
//        }
//
//        // If all retries fail
//        handleStartResult("FAILED", "Height Initialization")
//        showErrorDialog(errorMessage)
//    }

    private fun formatHeightResult(ezdxData: EzdxData): String = buildString {
        appendLine("Height Measurement")
        appendLine("Height: ${ezdxData.result1 ?: "N/A"} cm")
        if (ezdxData.result2 != null) appendLine("Height (ft): ${ezdxData.result2}")
        appendLine("Test Duration: ${getTestDuration()}")
        appendLine("Timestamp: ${getCurrentTimestamp()}")
    }


    private fun handleStartResult(result: String, testName: String) {
        Toast.makeText(this, "Test start result for $testName: $result", Toast.LENGTH_SHORT).show()

        runOnUiThread {
            if (result == "SUCCESS") {
                updateStatus("$testName measurement in progress...", StatusType.SUCCESS)
                Toast.makeText(this, "$testName started successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "$testName failed to start: $result", Toast.LENGTH_LONG).show()
                updateStatus("Failed to start $testName", StatusType.ERROR)
                resetTestState()
//                showErrorDialog("Failed to start $testName test. Please check device connection and try again.")
            }
        }
    }

    private fun handleTestTimeout() {
        Toast.makeText(this, "Test timeout for: $testType", Toast.LENGTH_LONG).show()
        runOnUiThread {
            updateStatus("Test timeout - Please try again", StatusType.WARNING)
            resetTestState()
            stopCurrentTest()
//            showErrorDialog("Test took too long to complete. Please check device and try again.")
        }
    }

    private fun stopTest() {
        Toast.makeText(this, "Stopping test: $testType", Toast.LENGTH_SHORT).show()
        cleanupTestResources()
        resetTestState()

        try {
            stopCurrentTest()
            updateStatus("Test stopped by user", StatusType.WARNING)
        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping test: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun cleanupTestResources() {
        testHandler.removeCallbacksAndMessages(null)
        temperaturePairingJob = null
    }

    private fun stopCurrentTest() {
        try {
            when (testType) {
                TEST_HEIGHT -> EzdxBT.stopDigitalHeightScale()
                TEST_BCA -> EzdxBT.stopBCA()
                else -> EzdxBT.stopCurrentTest()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping current test: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun resetTestState() {
        cleanupTestResources()
        isTestRunning = false
        btnStartTest.isEnabled = true
        btnStopTest.isEnabled = false
        progressBar.isVisible = false
    }

    //    override fun onEzdxData(ezdxData: EzdxData) {
//        val statusName = ezdxData.status?.name ?: "UNKNOWN"
//        Toast.makeText(
//            this,
//            "Received EzdxData - Status: $statusName, Result1: ${ezdxData.result1}, Result2: ${ezdxData.result2}, Result3: ${ezdxData.result3}",
//            Toast.LENGTH_SHORT
//        ).show()
//
//        runOnUiThread {
//            handleEzdxDataStatus(statusName, ezdxData)
//        }
//    }
    override fun onEzdxData(ezdxData: EzdxData?) {
        if (ezdxData == null) {
            Toast.makeText(this, "EzdxData is null", Toast.LENGTH_SHORT).show()
            Log.e("Ezdx", "EzdxData is null")
            return
        }

        val statusName = ezdxData.status?.name ?: "UNKNOWN"
        val result1 = ezdxData.result1 ?: "N/A"
        val result2 = ezdxData.result2 ?: "N/A"
        val result3 = ezdxData.result3 ?: "N/A"
        val note = ezdxData.note ?: "No note"

        // Show toast with data
        Toast.makeText(
            this,
            "EzdxData ➤ Status: $statusName\nResult1: $result1\nResult2: $result2\nResult3: $result3",
            Toast.LENGTH_LONG
        ).show()

        // Log details
        Log.d("Ezdx", "Status: $statusName")
        Log.d("Ezdx", "Result1: $result1")
        Log.d("Ezdx", "Result2: $result2")
        Log.d("Ezdx", "Result3: $result3")
        Log.d("Ezdx", "Note: $note")

        // Update UI or handle logic
        runOnUiThread {
            handleEzdxDataStatus(statusName, ezdxData)
        }
    }


    private fun handleEzdxDataStatus(statusName: String, ezdxData: EzdxData) {
        when (statusName) {
            "TEST_COMPLETED" -> {
                Toast.makeText(
                    this,
                    "Test completed successfully for $testType",
                    Toast.LENGTH_SHORT
                ).show()
                val result = formatTestResult(ezdxData)
                updateStatus("Test completed successfully", StatusType.SUCCESS)
                showResults(result)
                resetTestState()
                stopCurrentTest()
            }

            "TEST_FAILED" -> {
                val errorMsg = ezdxData.failedMsg ?: "Unknown error occurred"
                Toast.makeText(this, "Test failed for $testType: $errorMsg", Toast.LENGTH_LONG)
                    .show()
                updateStatus("Test failed: $errorMsg", StatusType.ERROR)
                resetTestState()
                stopCurrentTest()
//                showErrorDialog("Test failed: $errorMsg")
            }

            "TEST_IN_PROGRESS", "MEASURING", "PROCESSING", "CALIBRATING", "READING" -> {
                val displayStatus = formatStatusMessage(statusName)
                updateStatus(displayStatus, StatusType.INFO)
                Toast.makeText(this, "Test status update: $displayStatus", Toast.LENGTH_SHORT)
                    .show()
            }

            "DEVICE_READY", "SENSOR_CONNECTED" -> {
                updateStatus("Device ready for measurement", StatusType.SUCCESS)
            }

            "SENSOR_ERROR", "CONNECTION_ERROR" -> {
                updateStatus("Sensor error - check connection", StatusType.ERROR)
                resetTestState()
            }

            else -> {
                val displayStatus = formatStatusMessage(statusName)
                updateStatus(displayStatus, StatusType.INFO)
                Toast.makeText(this, "Status update: $displayStatus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatStatusMessage(statusName: String): String {
        return statusName.replace("_", " ").lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
    }

    private fun copyResultToClipboard(result: String) {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Test Result", result)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Result copied to clipboard", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error copying to clipboard: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun formatTestResult(ezdxData: EzdxData): String {
        return when (testType) {
            TEST_PULSE_OXIMETRY -> formatPulseOximetryResult(ezdxData)
            TEST_BLOOD_PRESSURE -> formatBloodPressureResult(ezdxData)
            TEST_TEMPERATURE -> formatTemperatureResult(ezdxData)
            TEST_BCA -> formatBCAResult(ezdxData)
            TEST_HEIGHT -> formatHeightResult(ezdxData)
            TEST_GLUCOSE -> formatGlucoseResult(ezdxData)
            else -> formatGenericResult(ezdxData)
        }
    }

    private fun formatPulseOximetryResult(ezdxData: EzdxData): String = buildString {
        appendLine("Pulse Oximetry Results")
        appendLine("SpO2: ${ezdxData.result1 ?: "N/A"}%")
        appendLine("Heart Rate: ${ezdxData.result2 ?: "N/A"} BPM")
        if (ezdxData.result3 != null) appendLine("PI: ${ezdxData.result3}")
        appendLine("Test Duration: ${getTestDuration()}")
        appendLine("Timestamp: ${getCurrentTimestamp()}")
        appendLine("Status: Normal range SpO2 (95-100%), HR (60-100 BPM)")
    }

    private fun formatBloodPressureResult(ezdxData: EzdxData): String = buildString {
        appendLine("Blood Pressure Results")
        appendLine("Systolic: ${ezdxData.result1 ?: "N/A"} mmHg")
        appendLine("Diastolic: ${ezdxData.result2 ?: "N/A"} mmHg")
        appendLine("Pulse: ${ezdxData.result3 ?: "N/A"} BPM")
        appendLine("Test Duration: ${getTestDuration()}")
        appendLine("Timestamp: ${getCurrentTimestamp()}")
        appendLine("Note: Normal BP <120/80 mmHg")
    }

    private fun formatTemperatureResult(ezdxData: EzdxData): String = buildString {
        appendLine("Weight/Temperature Results")
        appendLine("Temperature: ${ezdxData.result1 ?: "N/A"}°C")
        appendLine("Weight: ${ezdxData.result2 ?: "N/A"} kg")
        if (ezdxData.result3 != null) appendLine("BMI: ${ezdxData.result3}")
        appendLine("Test Duration: ${getTestDuration()}")
        appendLine("Timestamp: ${getCurrentTimestamp()}")
        appendLine("Note: Normal body temp 36.1-37.2°C")
    }

    private fun formatBCAResult(ezdxData: EzdxData): String = buildString {
        appendLine("Body Composition Analysis")
        appendLine("Weight: ${ezdxData.result1 ?: "N/A"} kg")
        appendLine("Body Fat: ${ezdxData.result2 ?: "N/A"}%")
        appendLine("Muscle Mass: ${ezdxData.result3 ?: "N/A"} kg")
        if (ezdxData.result4 != null) appendLine("Water %: ${ezdxData.result4}%")
        if (ezdxData.result5 != null) appendLine("Bone Mass: ${ezdxData.result5} kg")
        appendLine("Test Duration: ${getTestDuration()}")
        appendLine("Timestamp: ${getCurrentTimestamp()}")
    }


    private fun formatGlucoseResult(ezdxData: EzdxData): String = buildString {
        appendLine("Blood Glucose Results")
        appendLine("Glucose Level: ${ezdxData.result1 ?: "N/A"} mg/dL")
        if (ezdxData.result2 != null) appendLine("Alternative Unit: ${ezdxData.result2} mmol/L")
        appendLine("Test Duration: ${getTestDuration()}")
        appendLine("Timestamp: ${getCurrentTimestamp()}")
        appendLine("Note: Normal range 70-99 mg/dL (fasting)")
    }

    private fun formatGenericResult(ezdxData: EzdxData): String = buildString {
        appendLine("Test Results")
        appendLine("Primary Result: ${ezdxData.result1 ?: "N/A"}")
        if (ezdxData.result2 != null) appendLine("Secondary Result: ${ezdxData.result2}")
        if (ezdxData.result3 != null) appendLine("Additional Data: ${ezdxData.result3}")
        appendLine("Test Duration: ${getTestDuration()}")
        appendLine("Timestamp: ${getCurrentTimestamp()}")
    }

    private fun getTestDuration(): String {
        val duration = if (testStartTime > 0) {
            (System.currentTimeMillis() - testStartTime) / 1000
        } else 0
        return "${duration}s"
    }

    private fun showResults(result: String) {
        Toast.makeText(this, "Displaying test results", Toast.LENGTH_SHORT).show()

        runOnUiThread {
            tvResults.text = result
            cardResults.isVisible = true
            tvResults.isVisible = true

//            showResultDialog(result)
        }
    }

    private fun showResultDialog(result: String) {
        AlertDialog.Builder(this)
            .setTitle("Test Results - ${getTestTitle(testType)}")
            .setMessage(result)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Copy") { _, _ ->
                copyResultToClipboard(result)
            }
            .setNegativeButton("Save") { _, _ ->
                // Future implementation for saving results
                Toast.makeText(this, "Save feature coming soon", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    enum class StatusType { SUCCESS, ERROR, WARNING, INFO }

    private fun updateStatus(message: String, type: StatusType) {
        Toast.makeText(this, "Status update: $message ($type)", Toast.LENGTH_SHORT).show()

        runOnUiThread {
            tvStatus.text = message
            tvStatus.setTextColor(ContextCompat.getColor(this, getStatusColor(type)))
        }
    }

    private fun getStatusColor(type: StatusType): Int = when (type) {
        StatusType.SUCCESS -> android.R.color.holo_green_dark
        StatusType.ERROR -> android.R.color.holo_red_dark
        StatusType.WARNING -> android.R.color.holo_orange_dark
        StatusType.INFO -> R.color.text_primary
    }

    private fun showError(message: String) {
        updateStatus(message, StatusType.ERROR)
//        showErrorDialog(message)
    }

//    private fun showErrorDialog(message: String) {
//        AlertDialog.Builder(this)
//            .setTitle("Error - ${getTestTitle(testType)}")
//            .setMessage(message)
//            .setPositiveButton("OK") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .setNeutralButton("Retry") { _, _ ->
//                authenticateDevice()
//            }
//            .setNegativeButton("Help") { _, _ ->
//                showTroubleshootingDialog()
//            }
//            .show()
//    }

    private fun showTroubleshootingDialog() {
        val troubleshootingText = getTroubleshootingText(testType)

        AlertDialog.Builder(this)
            .setTitle("Troubleshooting - ${getTestTitle(testType)}")
            .setMessage(troubleshootingText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getTroubleshootingText(testType: String): String = when (testType) {
        TEST_TEMPERATURE -> "1. Ensure device is powered on\n2. Check Bluetooth pairing\n3. Wait for pairing confirmation\n4. Restart the device if needed"
        TEST_GLUCOSE -> "1. Insert test strip properly\n2. Use fresh blood sample\n3. Check strip expiry date\n4. Ensure meter is calibrated"
        else -> "1. Check device connection\n2. Ensure device is powered on\n3. Restart Bluetooth\n4. Try re-authentication"
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
                Toast.makeText(this, "Device info received: $it", Toast.LENGTH_SHORT).show()
                updateStatus("Device information received", StatusType.SUCCESS)
                btnStartTest.isEnabled = true
            }
        }
    }

    override fun onStatusChange(status: com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus) {
        runOnUiThread {
            handleBluetoothStatusChange(status)
        }
    }

    private fun handleBluetoothStatusChange(status: com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus) {
        when (status) {
            com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus.CONNECTED -> {
                Toast.makeText(this, "Bluetooth connected", Toast.LENGTH_SHORT).show()
                updateStatus("Device connected", StatusType.SUCCESS)
            }

            com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus.CONNECTING -> {
                updateStatus("Connecting to device...", StatusType.INFO)
            }

            com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus.NONE -> {
                Toast.makeText(this, "Bluetooth disconnected", Toast.LENGTH_LONG).show()
                handleBluetoothDisconnection()
            }

            else -> {
                Toast.makeText(this, "Bluetooth status: $status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleBluetoothDisconnection() {
        if (isTestRunning) {
            updateStatus("Device disconnected during test", StatusType.ERROR)
            resetTestState()
//            showErrorDialog("Device disconnected. Please reconnect and try again.")
        } else {
            updateStatus("Device disconnected", StatusType.WARNING)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isTestRunning) {
            showExitConfirmation()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Activity destroyed", Toast.LENGTH_SHORT).show()
        cleanupTestResources()

        try {
            stopCurrentTest()
        } catch (e: Exception) {
            Toast.makeText(this, "Error during cleanup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(this, "Activity paused", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(this, "Activity resumed", Toast.LENGTH_SHORT).show()
    }
}
