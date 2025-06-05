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

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
import com.healthcubed.ezdxlib.model.EzdxData
import com.healthcubed.ezdxlib.model.HCDeviceData
import com.healthcubed.ezdxlib.model.Status
import com.healthcubed.ezdxlib.model.TestName

class BluetoothConnect: AppCompatActivity(),
    BluetoothService.OnBluetoothScanCallback,
    BluetoothService.OnBluetoothEventCallback {

    private lateinit var btnScan: Button
    private lateinit var deviceListView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private var isScanning = false

    private val deviceList = ArrayList<BluetoothDevice>()
    private val deviceNames = ArrayList<String>()
    private lateinit var deviceAdapter: ArrayAdapter<String>
    private lateinit var bluetoothService: BluetoothService

    // Handler for delays
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val REQUEST_ENABLE_BT = 124
        private const val CONNECTION_DELAY_MS = 2000L // 2 seconds delay
        private const val NAVIGATION_DELAY_MS = 1500L // 1.5 seconds delay before navigation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        setContentView(R.layout.activity_bluetooth_connect)

        // Initialize EzdxBT
        EzdxBT.initialize(applicationContext)
        bluetoothService = BluetoothService.getDefaultInstance()

        initializeViews()
        setupListeners()
        checkAndRequestPermissions()
    }

    private fun initializeViews() {
        btnScan = findViewById(R.id.btn_scan)
        deviceListView = findViewById(R.id.device_list)
        progressBar = findViewById(R.id.progress_bar)
        statusTextView = findViewById(R.id.status_text)

        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
        deviceListView.adapter = deviceAdapter

        // Initially hide progress bar
        progressBar.visibility = View.GONE
        statusTextView.text = "Ready to scan"
    }

    private fun setupListeners() {
        btnScan.setOnClickListener {
            if (isScanning) {
                stopScan()
            } else {
                startScan()
            }
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            if (position < deviceList.size) {
                stopScan()
                connectToDevice(deviceList[position])
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bluetoothService.setOnScanCallback(this)
        bluetoothService.setOnEventCallback(this)
    }

    override fun onPause() {
        super.onPause()
        if (isScanning) {
            stopScan()
        }
    }

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

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        // Location permissions (required for BLE scanning)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Bluetooth permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

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
                } else {
                    Toast.makeText(this, "Some permissions were denied. Bluetooth functionality may be limited.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            showError("Bluetooth not supported on this device")
            return
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    showError("Bluetooth permission not granted")
                    return
                }
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }

        // Check scanning permissions
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
        deviceNames.clear()
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
                } else {
                    showError("Bluetooth is required for device scanning")
                }
            }
        }
    }

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
        try {
            statusTextView.text = "Authenticating device..."
            EzdxBT.authenticate("VmtaYVUyRnJNVVpPVlZaV1YwZG9VRmxYZEVkTk1WSldWV3RLYTAxRVJrVlVWV2h2VkRKV2MySkVVbFZOUmtwaFZHdFZOVkpXUmxsYVJUVlRVbFZaZWc9PQ==")
        } catch (e: Exception) {
            showError("Authentication failed: ${e.message}")
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

    override fun onHCDeviceInfo(hcDeviceData: HCDeviceData) {
        runOnUiThread {
            val serialNumber = hcDeviceData.serialNumber ?: "Unknown"
            val firmwareVersion = hcDeviceData.firmwareVersion ?: "Unknown"

            // Show device connected message with serial number
            val connectionMessage = "Device Connected!\nSerial: $serialNumber"
            Toast.makeText(this, connectionMessage, Toast.LENGTH_LONG).show()

            // Update status text with slow animation effect
            showSlowConnectionStatus(serialNumber, firmwareVersion)

            // Navigate to device details after delay
            handler.postDelayed({
                navigateToDeviceDetails(hcDeviceData)
            }, NAVIGATION_DELAY_MS)
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

            val intent = Intent(this, DeviceDetailsActivity::class.java).apply {
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