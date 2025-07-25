//package com.codixly.docbot
//
//import android.Manifest
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.ContentValues.TAG
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
//import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus
//import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
//import com.healthcubed.ezdxlib.model.EzdxData
//import com.healthcubed.ezdxlib.model.HCDeviceData
//import com.healthcubed.ezdxlib.model.Status
//import com.healthcubed.ezdxlib.model.TestName
//
//class BluetoothConnect: AppCompatActivity(),
//    BluetoothService.OnBluetoothScanCallback,
//    BluetoothService.OnBluetoothEventCallback {
//
//    private lateinit var btnScan: Button
//    private lateinit var deviceListView: ListView
//    private lateinit var progressBar: ProgressBar
//    private lateinit var statusTextView: TextView
//    private var isScanning = false
//
//    private val deviceList = ArrayList<BluetoothDevice>()
//    private val deviceNames = ArrayList<String>()
//    private lateinit var deviceAdapter: ArrayAdapter<String>
//    private lateinit var bluetoothService: BluetoothService
//
//    // Handler for delays
//    private val handler = Handler(Looper.getMainLooper())
//
//    companion object {
//        private const val PERMISSION_REQUEST_CODE = 123
//        private const val REQUEST_ENABLE_BT = 124
//        private const val CONNECTION_DELAY_MS = 2000L // 2 seconds delay
//        private const val NAVIGATION_DELAY_MS = 1500L // 1.5 seconds delay before navigation
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
//        setContentView(R.layout.activity_bluetooth_connect)
//
//        // Initialize EzdxBT
//        EzdxBT.initialize(applicationContext)
//        bluetoothService = BluetoothService.getDefaultInstance()
//
//        initializeViews()
//        setupListeners()
//        checkAndRequestPermissions()
//    }
//
//    private fun initializeViews() {
//        btnScan = findViewById(R.id.btn_scan)
//        deviceListView = findViewById(R.id.device_list)
//        progressBar = findViewById(R.id.progress_bar)
//        statusTextView = findViewById(R.id.status_text)
//
//        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
//        deviceListView.adapter = deviceAdapter
//
//        // Initially hide progress bar
//        progressBar.visibility = View.GONE
//        statusTextView.text = "Ready to scan"
//    }
//
//    private fun setupListeners() {
//        btnScan.setOnClickListener {
//            if (isScanning) {
//                stopScan()
//            } else {
//                startScan()
//            }
//        }
//
//        deviceListView.setOnItemClickListener { _, _, position, _ ->
//            if (position < deviceList.size) {
//                stopScan()
//                connectToDevice(deviceList[position])
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        bluetoothService.setOnScanCallback(this)
//        bluetoothService.setOnEventCallback(this)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (isScanning) {
//            stopScan()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (isScanning) {
//            stopScan()
//        }
//        // Clean up callbacks and handler
//        bluetoothService.setOnScanCallback(null)
//        bluetoothService.setOnEventCallback(null)
//        handler.removeCallbacksAndMessages(null)
//    }
//
//    private fun checkAndRequestPermissions() {
//        val permissions = mutableListOf<String>()
//
//        // Location permissions (required for BLE scanning)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
//        }
//
//        // Bluetooth permissions based on Android version
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
//            }
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
//            }
//        } else {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH)
//            }
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
//            }
//        }
//
//        if (permissions.isNotEmpty()) {
//            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            PERMISSION_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                    Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Some permissions were denied. Bluetooth functionality may be limited.", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    private fun startScan() {
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//        // Check if Bluetooth is supported
//        if (bluetoothAdapter == null) {
//            showError("Bluetooth not supported on this device")
//            return
//        }
//
//        // Check if Bluetooth is enabled
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                    != PackageManager.PERMISSION_GRANTED
//                ) {
//                    showError("Bluetooth permission not granted")
//                    return
//                }
//            }
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//            return
//        }
//
//        // Check scanning permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                showError("Bluetooth scan permission not granted")
//                return
//            }
//        }
//
//        // Start scanning
//        isScanning = true
//        deviceList.clear()
//        deviceNames.clear()
//        deviceAdapter.notifyDataSetChanged()
//
//        btnScan.text = "Stop Scan"
//        progressBar.visibility = View.VISIBLE
//        statusTextView.text = "Scanning for devices..."
//
//        try {
//            bluetoothService.startScan()
//        } catch (e: Exception) {
//            showError("Failed to start scan: ${e.message}")
//            resetScanUI()
//        }
//    }
//
//    private fun stopScan() {
//        isScanning = false
//        try {
//            bluetoothService.stopScan()
//        } catch (e: Exception) {
//            // Log error but continue with UI reset
//        }
//        resetScanUI()
//    }
//
//    private fun resetScanUI() {
//        btnScan.text = "Start Scan"
//        progressBar.visibility = View.GONE
//        statusTextView.text = if (deviceList.isEmpty()) "No devices found" else "Scan stopped"
//    }
//
//    private fun connectToDevice(device: BluetoothDevice) {
//        // Check connection permission
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                showError("Bluetooth connect permission not granted")
//                return
//            }
//        }
//
//        val deviceName = device.name ?: "Unknown Device"
//        statusTextView.text = "Connecting to $deviceName..."
//        progressBar.visibility = View.VISIBLE
//
//        try {
//            bluetoothService.connect(device)
//        } catch (e: Exception) {
//            showError("Failed to connect: ${e.message}")
//            progressBar.visibility = View.GONE
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            REQUEST_ENABLE_BT -> {
//                if (resultCode == RESULT_OK) {
//                    Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
//                } else {
//                    showError("Bluetooth is required for device scanning")
//                }
//            }
//        }
//    }
//
//    // Bluetooth scan callbacks
//    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                != PackageManager.PERMISSION_GRANTED
//            ) return
//        }
//
//        val deviceName = device.name
//        if (!deviceList.contains(device) && !deviceName.isNullOrEmpty()) {
//            deviceList.add(device)
//            deviceNames.add("$deviceName\n${device.address} (RSSI: $rssi)")
//            runOnUiThread {
//                deviceAdapter.notifyDataSetChanged()
//                if (deviceList.size == 1) {
//                    statusTextView.text = "Found ${deviceList.size} device"
//                } else {
//                    statusTextView.text = "Found ${deviceList.size} devices"
//                }
//            }
//        }
//    }
//
//    override fun onStartScan() {
//        runOnUiThread {
//            Toast.makeText(this, "Scan started", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onStopScan() {
//        runOnUiThread {
//            progressBar.visibility = View.GONE
//            val message = if (deviceList.isEmpty()) "No devices found" else "Scan completed"
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // Bluetooth connection callbacks
//    override fun onStatusChange(status: BluetoothStatus) {
//        runOnUiThread {
//            when (status) {
//                BluetoothStatus.NONE -> {
//                    statusTextView.text = "Not Connected"
//                    progressBar.visibility = View.GONE
//                }
//                BluetoothStatus.CONNECTING -> {
//                    statusTextView.text = "Connecting..."
//                    progressBar.visibility = View.VISIBLE
//                }
//                BluetoothStatus.CONNECTED -> {
//                    statusTextView.text = "Connected - Authenticating..."
//                    // Add delay before authentication
//                    handler.postDelayed({
//                        authenticateDevice()
//                    }, CONNECTION_DELAY_MS)
//                }
//            }
//        }
//    }
//
//    private fun authenticateDevice() {
//        try {
//            statusTextView.text = "Authenticating device..."
//            EzdxBT.authenticate("VmtaYVUyRnJNVVpPVlZaV1YwZG9VRmxYZEVkTk1WSldWV3RLYTAxRVJrVlVWV2h2VkRKV2MySkVVbFZOUmtwaFZHdFZOVkpXUmxsYVJUVlRVbFZaZWc9PQ==")
//        } catch (e: Exception) {
//            showError("Authentication failed: ${e.message}")
//        }
//    }
//
//    override fun onDeviceName(deviceName: String) {
//        runOnUiThread {
//            Toast.makeText(this, "Connected to $deviceName", Toast.LENGTH_SHORT).show()
//            statusTextView.text = "Connected to $deviceName"
//        }
//    }
//
//    override fun onToast(message: String) {
//        runOnUiThread {
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onEzdxData(ezdxData: EzdxData) {
//        runOnUiThread {
//            when (ezdxData.status) {
//                Status.STARTED -> {
//                    statusTextView.text = "Test started"
//                    progressBar.visibility = View.VISIBLE
//                }
//                Status.ANALYSING -> {
//                    statusTextView.text = "Analyzing..."
//                }
//                Status.TEST_COMPLETED -> {
//                    statusTextView.text = "Test completed successfully"
//                    progressBar.visibility = View.GONE
//                    displayTestResults(ezdxData)
//                    EzdxBT.stopCurrentTest()
//                }
//                Status.TEST_FAILED -> {
//                    statusTextView.text = "Test failed"
//                    progressBar.visibility = View.GONE
//                    showError("Test failed: ${ezdxData.failedMsg ?: "Unknown error"}")
//                    EzdxBT.stopCurrentTest()
//                }
//                else -> {
//                    statusTextView.text = "Status: ${ezdxData.status}"
//                }
//            }
//        }
//    }
//
//    private fun displayTestResults(ezdxData: EzdxData) {
//        val resultMessage = "Test Results:\n${ezdxData.resultData ?: "No data available"}"
//        Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show()
//
//        // You can add more detailed result display here
//        // For example, navigate to a results activity or show a dialog
//    }
//
//    override fun onHCDeviceInfo(hcDeviceData: HCDeviceData) {
//        runOnUiThread {
//            val serialNumber = hcDeviceData.serialNumber ?: "Unknown"
//            val firmwareVersion = hcDeviceData.firmwareVersion ?: "Unknown"
//
//            // Show device connected message with serial number
//            val connectionMessage = "Device Connected!\nSerial: $serialNumber"
//            Toast.makeText(this, connectionMessage, Toast.LENGTH_LONG).show()
//
//            // Update status text with slow animation effect
//            showSlowConnectionStatus(serialNumber, firmwareVersion)
//
//            // Navigate to device details after delay
//            handler.postDelayed({
//                navigateToDeviceDetails(hcDeviceData)
//            }, NAVIGATION_DELAY_MS)
//        }
//    }
//
//    private fun showSlowConnectionStatus(serialNumber: String, firmwareVersion: String) {
//        // Show connecting status with animation
//        val statusMessages = arrayOf(
//            "Device authenticating...",
//            "Retrieving device info...",
//            "Device connected successfully",
//            "Serial: $serialNumber",
//            "Firmware: $firmwareVersion",
//            "Preparing device details..."
//        )
//
//        var currentIndex = 0
//        val statusUpdateRunnable = object : Runnable {
//            override fun run() {
//                if (currentIndex < statusMessages.size) {
//                    statusTextView.text = statusMessages[currentIndex]
//                    currentIndex++
//                    handler.postDelayed(this, 500) // 500ms between each status update
//                }
//            }
//        }
//
//        handler.post(statusUpdateRunnable)
//    }
//
//    private fun navigateToDeviceDetails(hcDeviceData: HCDeviceData) {
//        try {
//            // Show final status before navigation
//            statusTextView.text = "Opening device details..."
//
//            val intent = Intent(this, DeviceDetailsActivity::class.java).apply {
//                putExtra("serial_number", hcDeviceData.serialNumber)
//                putExtra("serial_number", hcDeviceData.ecgModule)
//                putExtra("firmware_version", hcDeviceData.firmwareVersion)
//                putExtra("device_connected", true) // Flag to indicate device is connected
//                putExtra("connection_timestamp", System.currentTimeMillis())
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            }
//
//            // Final toast message
//            Toast.makeText(this, "Navigating to device details...", Toast.LENGTH_SHORT).show()
//
//            startActivity(intent)
//            finish()
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to navigate to device details", e)
//            showError("Failed to open device details: ${e.message}")
//        }
//    }
//
//    private fun showError(message: String) {
//        runOnUiThread {
//            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//            statusTextView.text = "Error: $message"
//            progressBar.visibility = View.GONE
//        }
//    }
//}

package com.codixly.docbot

import GenericResponse
import MachineTestStatusRequest
import SaveDeviceDetailsRequest
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codixly.docbot.activity.DeviceAddedSuccessfullyActivity
import com.codixly.docbot.adapter.BluetoothDeviceAdapter
import com.codixly.docbot.model.VerifyKeyRequest
import com.codixly.docbot.model.VerifyKeyResponse
import com.codixly.docbot.network.ApiClient
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
import com.healthcubed.ezdxlib.model.EzdxData
import com.healthcubed.ezdxlib.model.HCDeviceData
import com.healthcubed.ezdxlib.model.Status
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BluetoothConnect: AppCompatActivity(),
    BluetoothService.OnBluetoothScanCallback,
    BluetoothService.OnBluetoothEventCallback {

    private lateinit var btnScan: Button
    private lateinit var deviceListView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private var isScanning = false
    private lateinit var deviceRecyclerView: RecyclerView
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private lateinit var imageView: ImageView
    private lateinit var frameLayout: FrameLayout
    private val deviceList = ArrayList<BluetoothDevice>()
    private val deviceNames = ArrayList<String>()
//    private lateinit var deviceAdapter: ArrayAdapter<String>
    private lateinit var bluetoothService: BluetoothService
    private var pendingScanRequest = false

    // Handler for delays
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val REQUEST_ENABLE_BT = 124
        private const val CONNECTION_DELAY_MS = 2000L
        private const val NAVIGATION_DELAY_MS = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        setContentView(R.layout.activity_bluetooth_connect)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize EzdxBT
        EzdxBT.initialize(applicationContext)
        bluetoothService = BluetoothService.getDefaultInstance()

        initializeViews()
        setupListeners()
        checkAndRequestPermissions()
    }

    private fun initializeViews() {
        btnScan = findViewById(R.id.buttonscan)
        progressBar = findViewById(R.id.progress_bar)
        statusTextView = findViewById(R.id.status_text)
        deviceRecyclerView = findViewById(R.id.device_recycler_view)
        imageView = findViewById(R.id.myGifView)
        frameLayout = findViewById(R.id.bluetooth_frame)

        deviceAdapter = BluetoothDeviceAdapter(deviceList) { device ->
            stopScan()
            connectToDevice(device)
        }
        deviceRecyclerView.adapter = deviceAdapter
        deviceRecyclerView.layoutManager = LinearLayoutManager(this)

        progressBar.visibility = View.GONE
        statusTextView.text = "Ready to scan"

        // Hide GIF initially
        imageView.visibility = View.GONE
        frameLayout.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        btnScan.setOnClickListener {
            if (checkAndRequestPermissions()) {
                if (isScanning) {
                    stopScan()
                } else {
                    startScan()
                    showScanGif()
                }
            } else {
                pendingScanRequest = true // Remember user wanted to scan
            }
        }
//        btnScan.setOnClickListener {
//            if (isScanning) {
//                stopScan()
//            } else {
//                startScan()
//                // Show GIF and hide frame
//                frameLayout.visibility = View.GONE
//                imageView.visibility = View.VISIBLE
//
//                Glide.with(this)
//                    .asGif()
//                    .load(R.raw.scan_bluetooth_device)
//                    .into(imageView)
//            }
//        }

//        deviceListView.setOnItemClickListener { _, _, position, _ ->
//            if (position < deviceList.size) {
//                stopScan()
//                connectToDevice(deviceList[position])
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        bluetoothService.setOnScanCallback(this)
        bluetoothService.setOnEventCallback(this)
    }

    override fun onPause() {
        super.onPause()
        if (isScanning) stopScan()
    }
//    override fun onPause() {
//        super.onPause()
//        if (isScanning) {
//            stopScan()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        if (isScanning) {
            stopScan()
        }
        // Clean up callbacks and handler
        bluetoothService.setOnScanCallback(null)
        bluetoothService.setOnEventCallback(null)
        handler.removeCallbacksAndMessages(null)
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        // Add required permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        return if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            false
        } else {
            true
        }
    }

//    private fun checkAndRequestPermissions() {
//        val permissions = mutableListOf<String>()
//
//        // Location permissions (required for BLE scanning)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
//        }
//
//        // Bluetooth permissions based on Android version
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
//            }
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
//            }
//        } else {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH)
//            }
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
//            }
//        }
//
//        if (permissions.isNotEmpty()) {
//            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
                    if (pendingScanRequest) {
                        pendingScanRequest = false
                        startScan()
                        showScanGif()
                    }
                } else {
                    Toast.makeText(this, "Some permissions denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showScanGif() {
        frameLayout.visibility = View.GONE
        imageView.visibility = View.VISIBLE

        Glide.with(this)
            .asGif()
            .load(R.raw.scan_bluetooth_device)
            .into(imageView)
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            PERMISSION_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                    Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Some permissions were denied. Bluetooth functionality may be limited.", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

//    private fun startScan() {
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//        // Check if Bluetooth is supported
//        if (bluetoothAdapter == null) {
//            showError("Bluetooth not supported on this device")
//            return
//        }
//
//        // Check if Bluetooth is enabled
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                    != PackageManager.PERMISSION_GRANTED
//                ) {
//                    showError("Bluetooth permission not granted")
//                    return
//                }
//            }
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//            return
//        }
//
//        // Check scanning permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                showError("Bluetooth scan permission not granted")
//                return
//            }
//        }
//
//        // Start scanning
//        isScanning = true
//        deviceList.clear()
//        deviceNames.clear()
//        deviceAdapter.notifyDataSetChanged()
//
//        btnScan.text = "Stop Scan"
//        progressBar.visibility = View.VISIBLE
//        statusTextView.text = "Scanning for devices..."
//
//        try {
//            bluetoothService.startScan()
//        } catch (e: Exception) {
//            showError("Failed to start scan: ${e.message}")
//            resetScanUI()
//        }
//    }

    private fun startScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            showError("Bluetooth not supported on this device")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }

        // Required permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                showError("Bluetooth scan permission not granted")
                return
            }
        }

        // Start scanning
        isScanning = true
        deviceList.clear()
        deviceAdapter.notifyDataSetChanged()

        btnScan.text = "Stop Scan"
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Scanning for devices..."

        try {
            bluetoothService.startScan()
        } catch (e: Exception) {
            showError("Failed to start scan: ${e.message}")
            resetScanUI()
        }
    }

    private fun stopScan() {
        isScanning = false
        try {
            bluetoothService.stopScan()
        } catch (e: Exception) {
            // Log error but continue with UI reset
        }
        resetScanUI()
    }

    private fun resetScanUI() {
        btnScan.text = "Start Scan"
        progressBar.visibility = View.GONE
        statusTextView.text = if (deviceList.isEmpty()) "No devices found" else "Scan stopped"
        imageView.visibility = View.GONE
        frameLayout.visibility = View.VISIBLE
    }

    private fun connectToDevice(device: BluetoothDevice) {
        // Check connection permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                showError("Bluetooth connect permission not granted")
                return
            }
        }

        val deviceName = device.name ?: "Unknown Device"
        statusTextView.text = "Connecting to $deviceName..."
        progressBar.visibility = View.VISIBLE

        try {
            bluetoothService.connect(device)
        } catch (e: Exception) {
            showError("Failed to connect: ${e.message}")
            progressBar.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
                    startScan()
                } else {
                    showError("Bluetooth is required for device scanning")
                }
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            REQUEST_ENABLE_BT -> {
//                if (resultCode == RESULT_OK) {
//                    Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
//                } else {
//                    showError("Bluetooth is required for device scanning")
//                }
//            }
//        }
//    }

    // Bluetooth scan callbacks
    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val deviceName = device.name
        if (!deviceList.contains(device) && !deviceName.isNullOrEmpty()) {
            deviceList.add(device)
            deviceNames.add("$deviceName\n${device.address} (RSSI: $rssi)")
            runOnUiThread {
                deviceAdapter.notifyDataSetChanged()
                if (deviceList.size == 1) {
                    statusTextView.text = "Found ${deviceList.size} device"
                } else {
                    statusTextView.text = "Found ${deviceList.size} devices"
                }
            }
        }
    }

    override fun onStartScan() {
        runOnUiThread {
            Toast.makeText(this, "Scan started", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStopScan() {
        runOnUiThread {
            progressBar.visibility = View.GONE
            val message = if (deviceList.isEmpty()) "No devices found" else "Scan completed"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Bluetooth connection callbacks
    override fun onStatusChange(status: BluetoothStatus) {
        runOnUiThread {
            when (status) {
                BluetoothStatus.NONE -> {
                    statusTextView.text = "Not Connected"
                    progressBar.visibility = View.GONE
                }
                BluetoothStatus.CONNECTING -> {
                    statusTextView.text = "Connecting..."
                    progressBar.visibility = View.VISIBLE
                }
                BluetoothStatus.CONNECTED -> {
                    statusTextView.text = "Connected - Authenticating..."
                    // Add delay before authentication
                    handler.postDelayed({
                        authenticateDevice()
                    }, CONNECTION_DELAY_MS)
                }
            }
        }
    }
    private fun authenticateDevice() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val machineUniqueId = sharedPref.getString("machine_unique_id", null)

        if (!machineUniqueId.isNullOrEmpty()) {
            val request = VerifyKeyRequest(machine_unique_id = machineUniqueId)

            ApiClient.instance.getVerifyKey(request).enqueue(object : Callback<VerifyKeyResponse> {
                override fun onResponse(call: Call<VerifyKeyResponse>, response: Response<VerifyKeyResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val verifyKey = response.body()?.machine_verify_key

                        with(sharedPref.edit()) {
                            putString("machine_verify_key", verifyKey)
                            apply()
                        }

                        try {
                            statusTextView.text = "Authenticating device..."
                            verifyKey?.let {
                                EzdxBT.authenticate(it)
                            } ?: run {
                                showError("Verify key is null")
                            }
                        } catch (e: Exception) {
                            showError("Authentication failed: ${e.message}")
                        }
                    } else {
                        showError("Device authentication failed")
                    }
                }

                override fun onFailure(call: Call<VerifyKeyResponse>, t: Throwable) {
                    showError("API call failed: ${t.message}")
                }
            })
        } else {
            Toast.makeText(this, "Machine details is not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeviceName(deviceName: String) {
        runOnUiThread {
            Toast.makeText(this, "Connected to $deviceName", Toast.LENGTH_SHORT).show()
            statusTextView.text = "Connected to $deviceName"
        }
    }

    override fun onToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onEzdxData(ezdxData: EzdxData) {
        runOnUiThread {
            when (ezdxData.status) {
                Status.STARTED -> {
                    statusTextView.text = "Test started"
                    progressBar.visibility = View.VISIBLE
                }
                Status.ANALYSING -> {
                    statusTextView.text = "Analyzing..."
                }
                Status.TEST_COMPLETED -> {
                    statusTextView.text = "Test completed successfully"
                    progressBar.visibility = View.GONE
                    displayTestResults(ezdxData)
                    EzdxBT.stopCurrentTest()
                }
                Status.TEST_FAILED -> {
                    statusTextView.text = "Test failed"
                    progressBar.visibility = View.GONE
                    showError("Test failed: ${ezdxData.failedMsg ?: "Unknown error"}")
                    EzdxBT.stopCurrentTest()
                }
                else -> {
                    statusTextView.text = "Status: ${ezdxData.status}"
                }
            }
        }
    }

    private fun displayTestResults(ezdxData: EzdxData) {
        val resultMessage = "Test Results:\n${ezdxData.resultData ?: "No data available"}"
        Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show()

        // You can add more detailed result display here
        // For example, navigate to a results activity or show a dialog
    }

//        override fun onHCDeviceInfo(hcDeviceData: HCDeviceData) {
//            runOnUiThread {
//                val serialNumber = hcDeviceData.serialNumber ?: "Unknown"
//                val firmwareVersion = hcDeviceData.firmwareVersion ?: "Unknown"
//
//                // Show device connected message with serial number
//                val connectionMessage = "Device Connected!\nSerial: $serialNumber"
//                Toast.makeText(this, connectionMessage, Toast.LENGTH_LONG).show()
//
//                // Update status text with slow animation effect
//                showSlowConnectionStatus(serialNumber, firmwareVersion)
//
//                // Navigate to device details after delay
//                handler.postDelayed({
//                    navigateToDeviceDetails(hcDeviceData)
//                }, NAVIGATION_DELAY_MS)
//            }
//        }

    override fun onHCDeviceInfo(hcDeviceData: HCDeviceData) {
        runOnUiThread {
            val serialNumber = hcDeviceData.serialNumber ?: "Unknown"
            val firmwareVersion = hcDeviceData.firmwareVersion ?: "Unknown"

            val connectionMessage = "Device Connected!\nSerial: $serialNumber"
//            Toast.makeText(this, connectionMessage, Toast.LENGTH_LONG).show()

            showSlowConnectionStatus(serialNumber, firmwareVersion)

            // Start both API calls after device is connected
            callSaveDeviceDetailsAPI(serialNumber, firmwareVersion)
            callMachineTestStatusAPI(hcDeviceData)

            handler.postDelayed({
                navigateToDeviceDetails(hcDeviceData)
            }, NAVIGATION_DELAY_MS)
        }
    }

    private fun callMachineTestStatusAPI(hcDeviceData: HCDeviceData) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)

        if (!customerId.isNullOrEmpty()) {
            val request = MachineTestStatusRequest(
                customer_unique_id = customerId,
                bloodPressureModule = if (hcDeviceData.bloodPressureModule?.toString()?.equals("ACTIVE", ignoreCase = true) == true) "1" else "0",
                cholesterolUricAcidModule = if (hcDeviceData.cholestrolUricAcidModule?.toString()?.equals("ACTIVE", ignoreCase = true) == true) "1" else "0",
                glucometerModule = if (hcDeviceData.glucometerModule?.toString()?.equals("ACTIVE", ignoreCase = true) == true) "1" else "0",
                hemoglobinModule = if (hcDeviceData.hemoglobinModule?.toString()?.equals("ACTIVE", ignoreCase = true) == true) "1" else "0",
                pulseOximetryModule = if (hcDeviceData.pulseOximetryModule?.toString()?.equals("ACTIVE", ignoreCase = true) == true) "1" else "0",
                rdtModule = if (hcDeviceData.rdtModule?.toString()?.equals("ACTIVE", ignoreCase = true) == true) "1" else "0",
                ecgModule = if (hcDeviceData.ecgModule?.toString()?.equals("ACTIVE", ignoreCase = true) == true) "1" else "0"
            )

            ApiClient.instance.sendMachineTestStatus(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val resultMessage = response.body()?.message ?: "Test status updated successfully"
                        Toast.makeText(this@BluetoothConnect, resultMessage, Toast.LENGTH_SHORT).show()
                        Log.d("MachineTestStatus", resultMessage)
                    } else {
                        val errorMessage = response.body()?.message ?: "Failed to update test status"
                        Toast.makeText(this@BluetoothConnect, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("MachineTestStatus", errorMessage)
                    }
                }


                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("MachineTestStatus", "API call failed: ${t.message}")
                }
            })
        } else {
            Log.e("MachineTestStatus", "Customer ID not found")
        }
    }


    private fun callSaveDeviceDetailsAPI(serialNumber: String, firmwareVersion: String) {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)

        if (!customerId.isNullOrEmpty()) {
            val request = SaveDeviceDetailsRequest(
                customer_unique_id = customerId,
                serial_number = serialNumber,
                firmware_version = firmwareVersion
            )

            ApiClient.instance.saveDeviceDetails(request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == true) {
                            val successMessage = body.message ?: "Device details saved successfully"
                            Log.d("DeviceDetails", successMessage)
                            Toast.makeText(this@BluetoothConnect, successMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            val failureMessage = body?.message ?: "Failed to save device details"
                            Log.e("DeviceDetails", failureMessage)
                            Toast.makeText(this@BluetoothConnect, failureMessage, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("DeviceDetails", "HTTP error: ${response.code()} - $errorBody")
                        Toast.makeText(this@BluetoothConnect, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }


                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("DeviceDetails", "API call failed: ${t.message}")
                }
            })
        } else {
            Log.e("DeviceDetails", "Customer ID not found")
        }
    }

    private fun showSlowConnectionStatus(serialNumber: String, firmwareVersion: String) {
        // Show connecting status with animation
        val statusMessages = arrayOf(
            "Device authenticating...",
            "Retrieving device info...",
            "Device connected successfully",
            "Serial: $serialNumber",
            "Firmware: $firmwareVersion",
            "Preparing device details..."
        )

        var currentIndex = 0
        val statusUpdateRunnable = object : Runnable {
            override fun run() {
                if (currentIndex < statusMessages.size) {
                    statusTextView.text = statusMessages[currentIndex]
                    currentIndex++
                    handler.postDelayed(this, 500) // 500ms between each status update
                }
            }
        }

        handler.post(statusUpdateRunnable)
    }

    private fun navigateToDeviceDetails(hcDeviceData: HCDeviceData) {
        try {
            // Show final status before navigation
            statusTextView.text = "Opening device details..."
//            val intent = Intent(this@BluetoothScanScreenActivity, DeviceAddedSuccessfullyActivity::class.java)
            val intent = Intent(this, DeviceAddedSuccessfullyActivity::class.java).apply {
                putExtra("serial_number", hcDeviceData.serialNumber)
                putExtra("firmware_version", hcDeviceData.firmwareVersion)
                putExtra("blood_pressure_module", hcDeviceData.bloodPressureModule?.toString())
                putExtra("cholesterol_uric_acid_module", hcDeviceData.cholestrolUricAcidModule?.toString())
                putExtra("glucometer_module", hcDeviceData.glucometerModule?.toString())
                putExtra("hemoglobin_module", hcDeviceData.hemoglobinModule?.toString())
                putExtra("pulse_oximetry_module", hcDeviceData.pulseOximetryModule?.toString())
                putExtra("rdt_module", hcDeviceData.rdtModule?.toString())
                putExtra("ecg_module", hcDeviceData.ecgModule?.toString())
                putExtra("temperature", hcDeviceData.temperature?.toString() ?: "0.0")
                putExtra("device_connected", true)
                putExtra("connection_timestamp", System.currentTimeMillis())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            // Final toast message
            Toast.makeText(this, "Navigating to device details...", Toast.LENGTH_SHORT).show()

            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to device details", e)
            showError("Failed to open device details: ${e.message}")
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            statusTextView.text = "Error: $message"
            progressBar.visibility = View.GONE
        }
    }
}