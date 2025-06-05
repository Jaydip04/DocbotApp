//package com.docbot.docbotkt
//
//import android.Manifest
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothManager
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import android.widget.*
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
//import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus
//import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
//import com.healthcubed.ezdxlib.model.EzdxData
//import com.healthcubed.ezdxlib.model.HCDeviceData
//import com.healthcubed.ezdxlib.model.Status
//import kotlinx.coroutines.launch
//
//class BluetoothConnect : AppCompatActivity(),
//    BluetoothService.OnBluetoothScanCallback,
//    BluetoothService.OnBluetoothEventCallback {
//
//    private lateinit var btnScan: Button
//    private lateinit var deviceListView: ListView
//    private lateinit var progressBar: ProgressBar
//    private lateinit var statusTextView: TextView
//    private lateinit var btnBack: Button
//
//    private var isScanning = false
//    private var isConnected = false
//    private var isAuthenticated = false
//    private var bluetoothAdapter: BluetoothAdapter? = null
//
//    private val deviceList = mutableListOf<BluetoothDevice>()
//    private val deviceNames = mutableListOf<String>()
//    private lateinit var deviceAdapter: ArrayAdapter<String>
//
//    private val handler = Handler(Looper.getMainLooper())
//    private var scanTimeoutRunnable: Runnable? = null
//
//    companion object {
//        private const val TAG = "BluetoothConnect"
//        private const val PERMISSION_REQUEST_CODE = 123
//        private const val SCAN_TIMEOUT_MS = 30000L
//        private const val REQUIRED_PERMISSIONS = "bluetooth_permissions"
//    }
//
//    // Activity result launcher for Bluetooth enable request
//    private val bluetoothEnableLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == RESULT_OK) {
//            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Bluetooth is required for device connection", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        try {
//            window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
//            setContentView(R.layout.activity_bluetooth_connect)
//
//            // Initialize Bluetooth adapter
//            initializeBluetoothAdapter()
//
//            // Initialize EzdxBT
//            EzdxBT.initialize(applicationContext)
//
//            initializeViews()
//            setupListeners()
//            checkAndRequestPermissions()
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in onCreate", e)
//            showErrorDialog("Initialization failed: ${e.message}")
//        }
//    }
//
//    private fun initializeBluetoothAdapter() {
//        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothAdapter = bluetoothManager.adapter
//
//        if (bluetoothAdapter == null) {
//            showErrorDialog("Bluetooth not supported on this device")
//            return
//        }
//    }
//
//    private fun initializeViews() {
//        try {
//            btnScan = findViewById(R.id.btn_scan)
//            deviceListView = findViewById(R.id.device_list)
//            progressBar = findViewById(R.id.progress_bar)
//            statusTextView = findViewById(R.id.status_text)
//
//            deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
//            deviceListView.adapter = deviceAdapter
//
//            // Initial UI state
//            updateUI()
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error initializing views", e)
//            showErrorDialog("Failed to initialize UI components")
//        }
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
//            if (position < deviceList.size && position >= 0) {
//                stopScan()
//                connectToDevice(deviceList[position])
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        try {
//            BluetoothService.getDefaultInstance().apply {
//                setOnScanCallback(this@BluetoothConnect)
//                setOnEventCallback(this@BluetoothConnect)
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up Bluetooth service callbacks", e)
//        }
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
//        cleanupResources()
//    }
//
//    private fun cleanupResources() {
//        try {
//            if (isScanning) {
//                stopScan()
//            }
//
//            scanTimeoutRunnable?.let { handler.removeCallbacks(it) }
//
//            lifecycleScope.launch {
//                try {
//                    BluetoothService.getDefaultInstance().disconnect()
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error disconnecting Bluetooth service", e)
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error cleaning up resources", e)
//        }
//    }
//
//    private fun checkAndRequestPermissions() {
//        val permissions = getRequiredPermissions()
//
//        if (permissions.isNotEmpty()) {
//            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
//        } else {
//            updateUI()
//        }
//    }
//
//    private fun getRequiredPermissions(): List<String> {
//        val permissions = mutableListOf<String>()
//
//        // Bluetooth permissions based on Android version
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
//                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
//            }
//            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
//            }
//            if (!hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
//                permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
//            }
//        } else {
//            if (!hasPermission(Manifest.permission.BLUETOOTH)) {
//                permissions.add(Manifest.permission.BLUETOOTH)
//            }
//            if (!hasPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
//                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
//            }
//        }
//
//        // Location permissions (required for Bluetooth scanning on older Android versions)
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
//            }
//            if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
//            }
//        }
//
//        return permissions
//    }
//
//    private fun hasPermission(permission: String): Boolean {
//        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            val deniedPermissions = permissions.filterIndexed { index, _ ->
//                grantResults.getOrNull(index) != PackageManager.PERMISSION_GRANTED
//            }
//
//            if (deniedPermissions.isNotEmpty()) {
//                showPermissionDeniedDialog(deniedPermissions)
//            } else {
//                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
//                updateUI()
//            }
//        }
//    }
//
//    private fun showPermissionDeniedDialog(deniedPermissions: List<String>) {
//        AlertDialog.Builder(this)
//            .setTitle("Permissions Required")
//            .setMessage("The following permissions are required for Bluetooth functionality:\n${deniedPermissions.joinToString("\n")}")
//            .setPositiveButton("Retry") { _, _ ->
//                checkAndRequestPermissions()
//            }
//            .setNegativeButton("Cancel") { _, _ ->
//                finish()
//            }
//            .setCancelable(false)
//            .show()
//    }
//
//    private fun startScan() {
//        if (bluetoothAdapter == null) {
//            showErrorDialog("Bluetooth not available")
//            return
//        }
//
//        if (!bluetoothAdapter!!.isEnabled) {
//            requestBluetoothEnable()
//            return
//        }
//
//        if (!hasRequiredPermissions()) {
//            Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_SHORT).show()
//            checkAndRequestPermissions()
//            return
//        }
//
//        if (isScanning) {
//            return
//        }
//
//        try {
//            isScanning = true
//            deviceList.clear()
//            deviceNames.clear()
//            deviceAdapter.notifyDataSetChanged()
//
//            updateUI()
//
//            // Set scan timeout
//            setupScanTimeout()
//
//            BluetoothService.getDefaultInstance().startScan()
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to start scan", e)
//            Toast.makeText(this, "Failed to start scan: ${e.message}", Toast.LENGTH_SHORT).show()
//            resetScanState()
//        }
//    }
//
//    private fun requestBluetoothEnable() {
//        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//                bluetoothEnableLauncher.launch(enableBtIntent)
//            } else {
//                Toast.makeText(this, "Bluetooth connect permission required", Toast.LENGTH_LONG).show()
//            }
//        } else {
//            bluetoothEnableLauncher.launch(enableBtIntent)
//        }
//    }
//
//    private fun setupScanTimeout() {
//        scanTimeoutRunnable?.let { handler.removeCallbacks(it) }
//
//        scanTimeoutRunnable = Runnable {
//            if (isScanning) {
//                stopScan()
//                Toast.makeText(this, "Scan timeout - no devices found", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        handler.postDelayed(scanTimeoutRunnable!!, SCAN_TIMEOUT_MS)
//    }
//
//    private fun stopScan() {
//        if (!isScanning) return
//
//        try {
//            BluetoothService.getDefaultInstance().stopScan()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error stopping scan", e)
//        }
//
//        resetScanState()
//    }
//
//    private fun resetScanState() {
//        isScanning = false
//        scanTimeoutRunnable?.let { handler.removeCallbacks(it) }
//        updateUI()
//    }
//
//    private fun hasRequiredPermissions(): Boolean {
//        return getRequiredPermissions().isEmpty()
//    }
//
//    private fun updateUI() {
//        runOnUiThread {
//            btnScan.text = if (isScanning) "Stop Scan" else "Start Scan"
//            btnScan.isEnabled = hasRequiredPermissions() && bluetoothAdapter?.isEnabled == true
//
//            progressBar.visibility = if (isScanning || isConnected) View.VISIBLE else View.GONE
//
//            statusTextView.text = when {
//                !hasRequiredPermissions() -> "Permissions required"
//                bluetoothAdapter?.isEnabled != true -> "Bluetooth disabled"
//                isScanning -> "Scanning for devices..."
//                deviceList.isEmpty() && !isScanning -> "No devices found"
//                isConnected -> "Connected"
//                else -> "Ready to scan"
//            }
//        }
//    }
//
//    private fun connectToDevice(device: BluetoothDevice) {
//        if (!hasRequiredPermissions()) {
//            Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val deviceName = try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//                    device.name ?: "Unknown Device"
//                } else {
//                    "Unknown Device"
//                }
//            } else {
//                device.name ?: "Unknown Device"
//            }
//        } catch (e: SecurityException) {
//            "Unknown Device"
//        }
//
//        statusTextView.text = "Connecting to $deviceName..."
//        progressBar.visibility = View.VISIBLE
//
//        lifecycleScope.launch {
//            try {
//                BluetoothService.getDefaultInstance().connect(device)
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to connect to device", e)
//                runOnUiThread {
//                    Toast.makeText(this@BluetoothConnect, "Failed to connect: ${e.message}", Toast.LENGTH_SHORT).show()
//                    progressBar.visibility = View.GONE
//                    statusTextView.text = "Connection failed"
//                }
//            }
//        }
//    }
//
//    // Bluetooth scan callbacks
//    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
//        try {
//            val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//                    device.name
//                } else null
//            } else {
//                device.name
//            }
//
//            if (!deviceList.contains(device) && !deviceName.isNullOrBlank()) {
//                deviceList.add(device)
//                val displayName = "$deviceName (${device.address}) - RSSI: $rssi dBm"
//                deviceNames.add(displayName)
//
//                runOnUiThread {
//                    deviceAdapter.notifyDataSetChanged()
//                    statusTextView.text = "Found ${deviceList.size} device(s)"
//                }
//            }
//        } catch (e: SecurityException) {
//            Log.w(TAG, "Permission denied while accessing device info", e)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error processing discovered device", e)
//        }
//    }
//
//    override fun onStartScan() {
//        runOnUiThread {
//            Log.d(TAG, "Scan started")
//            statusTextView.text = "Scanning for devices..."
//        }
//    }
//
//    override fun onStopScan() {
//        runOnUiThread {
//            Log.d(TAG, "Scan stopped")
//            resetScanState()
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
//                    isConnected = false
//                    isAuthenticated = false
//                }
//                BluetoothStatus.CONNECTING -> {
//                    statusTextView.text = "Connecting..."
//                    progressBar.visibility = View.VISIBLE
//                    isConnected = false
//                    isAuthenticated = false
//                }
//                BluetoothStatus.CONNECTED -> {
//                    statusTextView.text = "Connected - Authenticating..."
//                    progressBar.visibility = View.VISIBLE
//                    isConnected = true
//                    authenticateDevice()
//                }
//            }
//        }
//    }
//
//    private fun authenticateDevice() {
//        lifecycleScope.launch {
//            try {
//                val authKey = getString(R.string.auth_key)
//                EzdxBT.authenticate(authKey)
//            } catch (e: Exception) {
//                Log.e(TAG, "Authentication failed", e)
//                runOnUiThread {
//                    Toast.makeText(this@BluetoothConnect, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    progressBar.visibility = View.GONE
//                    statusTextView.text = "Authentication failed"
//                }
//            }
//        }
//    }
//
//    override fun onDeviceName(deviceName: String) {
//        runOnUiThread {
//            Log.d(TAG, "Connected to device: $deviceName")
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
//                    progressBar.visibility = View.VISIBLE
//                }
//                Status.TEST_COMPLETED -> {
//                    statusTextView.text = "Test completed successfully"
//                    progressBar.visibility = View.GONE
//                    displayTestResults(ezdxData)
//                    safeStopCurrentTest()
//                }
//                Status.TEST_FAILED -> {
//                    statusTextView.text = "Test failed"
//                    progressBar.visibility = View.GONE
//                    val errorMessage = ezdxData.failedMsg ?: "Unknown error"
//                    Toast.makeText(this, "Test failed: $errorMessage", Toast.LENGTH_LONG).show()
//                    safeStopCurrentTest()
//                }
//                else -> {
//                    statusTextView.text = "Status: ${ezdxData.status}"
//                }
//            }
//        }
//    }
//
//    private fun safeStopCurrentTest() {
//        lifecycleScope.launch {
//            try {
//                EzdxBT.stopCurrentTest()
//            } catch (e: Exception) {
//                Log.e(TAG, "Error stopping current test", e)
//            }
//        }
//    }
//
//    private fun displayTestResults(ezdxData: EzdxData) {
//        val resultMessage = ezdxData.resultData?.toString() ?: "No result data available"
//        Toast.makeText(this, "Test Result: $resultMessage", Toast.LENGTH_LONG).show()
//
//        showResultsDialog(resultMessage)
//    }
//
//    private fun showResultsDialog(results: String) {
//        AlertDialog.Builder(this)
//            .setTitle("Test Results")
//            .setMessage(results)
//            .setPositiveButton("OK", null)
//            .setNeutralButton("Save") { _, _ ->
//                // Implement save functionality
//                Toast.makeText(this, "Results saved", Toast.LENGTH_SHORT).show()
//            }
//            .show()
//    }
//
//    override fun onHCDeviceInfo(hcDeviceData: HCDeviceData) {
//        runOnUiThread {
//            isAuthenticated = true
//            progressBar.visibility = View.GONE
//            statusTextView.text = "Device authenticated successfully"
//
//            val serialNumber = hcDeviceData.serialNumber ?: "Unknown"
//            Toast.makeText(this, "Device authenticated: $serialNumber", Toast.LENGTH_SHORT).show()
//
//            navigateToDeviceDetails(hcDeviceData)
//        }
//    }
//
//    private fun navigateToDeviceDetails(hcDeviceData: HCDeviceData) {
//        try {
//            val intent = Intent(this, DeviceDetailsActivity::class.java).apply {
//                putExtra("serial_number", hcDeviceData.serialNumber)
//                putExtra("firmware_version", hcDeviceData.firmwareVersion)
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            }
//            startActivity(intent)
//            finish()
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to navigate to device details", e)
//            Toast.makeText(this, "Failed to open device details: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun showErrorDialog(message: String) {
//        AlertDialog.Builder(this)
//            .setTitle("Error")
//            .setMessage(message)
//            .setPositiveButton("OK") { _, _ ->
//                finish()
//            }
//            .setCancelable(false)
//            .show()
//    }
//
//}


//
//package com.docbot.docbotkt
//
//import android.Manifest
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.view.View
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.docbot.docbotkt.R
//import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothService
//import com.healthcubed.ezdxlib.bluetoothHandler.BluetoothStatus
//import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT
//import com.healthcubed.ezdxlib.model.EzdxData
//import com.healthcubed.ezdxlib.model.HCDeviceData
//import com.healthcubed.ezdxlib.model.Status  // Ensure this import exists
//import com.healthcubed.ezdxlib.model.TestName  // If used
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
//
//    companion object {
//        private const val PERMISSION_REQUEST_CODE = 123
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
//        setContentView(R.layout.activity_bluetooth_connect)
//
//        EzdxBT.initialize(applicationContext)
//
//        btnScan = findViewById(R.id.btn_scan)
//        deviceListView = findViewById(R.id.device_list)
//        progressBar = findViewById(R.id.progress_bar)
//        statusTextView = findViewById(R.id.status_text)
//
//        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
//        deviceListView.adapter = deviceAdapter
//
//        btnScan.setOnClickListener {
//            val service = BluetoothService.getDefaultInstance()
//            if (isScanning) {
//                stopScan()
//            } else {
//                startScan()
//            }
//        }
//
//        deviceListView.setOnItemClickListener { _, _, position, _ ->
//            stopScan()
//            connectToDevice(deviceList[position])
//        }
//
//        checkAndRequestPermissions()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val service = BluetoothService.getDefaultInstance()
//        service.setOnScanCallback(this)
//        service.setOnEventCallback(this)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        val service = BluetoothService.getDefaultInstance()
//        if (isScanning) {
//            stopScan()
//        }
//    }
//
//    private fun checkAndRequestPermissions() {
//        val permissions = mutableListOf<String>()
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
//            }
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
//            }
//        } else {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.BLUETOOTH)
//            }
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
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
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
//            Toast.makeText(this, "Some permissions were not granted", Toast.LENGTH_SHORT).show()
//        }
//    }
//
////    private fun startScan() {
////        isScanning = true
////        deviceList.clear()
////        deviceNames.clear()
////        deviceAdapter.notifyDataSetChanged()
////
////        btnScan.text = "Stop Scan"
////        progressBar.visibility = View.VISIBLE
////        statusTextView.text = "Scanning..."
////
////        BluetoothService.getDefaultInstance().startScan()
////    }
//
//    private fun startScan() {
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        if (bluetoothAdapter == null) {
//            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if (!bluetoothAdapter.isEnabled) {
//            Toast.makeText(this, "Please turn on Bluetooth to scan devices", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        isScanning = true
//        deviceList.clear()
//        deviceNames.clear()
//        deviceAdapter.notifyDataSetChanged()
//
//        btnScan.text = "Stop Scan"
//        progressBar.visibility = View.VISIBLE
//        statusTextView.text = "Scanning..."
//
//        BluetoothService.getDefaultInstance().startScan()
//    }
//
//    private fun stopScan() {
//        isScanning = false
//        BluetoothService.getDefaultInstance().stopScan()
//        btnScan.text = "Start Scan"
//        progressBar.visibility = View.GONE
//        statusTextView.text = "Scan stopped"
//    }
//
//    private fun connectToDevice(device: BluetoothDevice) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        statusTextView.text = "Connecting to ${device.name}..."
//        progressBar.visibility = View.VISIBLE
//
//        BluetoothService.getDefaultInstance().connect(device)
//    }
//
//    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//            != PackageManager.PERMISSION_GRANTED
//        ) return
//
//        if (!deviceList.contains(device) && !device.name.isNullOrEmpty()) {
//            deviceList.add(device)
//            deviceNames.add("${device.name} - ${device.address}")
//            runOnUiThread { deviceAdapter.notifyDataSetChanged() }
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
//            Toast.makeText(this, "Scan stopped", Toast.LENGTH_SHORT).show()
//        }
//    }
//
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
//                    statusTextView.text = "Connected"
//                    progressBar.visibility = View.GONE
//                    authenticateDevice()
//                }
//            }
//        }
//    }
//
//    private fun authenticateDevice() {
//        EzdxBT.authenticate("VmtaYVUyRnJNVVpPVlZaV1YwZG9VRmxYZEVkTk1WSldWV3RLYTAxRVJrVlVWV2h2VkRKV2MySkVVbFZOUmtwaFZHdFZOVkpXUmxsYVJUVlRVbFZaZWc9PQ==")
//    }
//
//    override fun onDeviceName(deviceName: String) {
//        runOnUiThread {
//            Toast.makeText(this, "Connected to $deviceName", Toast.LENGTH_SHORT).show()
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
//                }
//                Status.ANALYSING -> {
//                    statusTextView.text = "Analyzing..."
//                }
//                Status.TEST_COMPLETED -> {
//                    statusTextView.text = "Test completed"
//                    displayTestResults(ezdxData)
//                    EzdxBT.stopCurrentTest()
//                }
//                Status.TEST_FAILED -> {
//                    statusTextView.text = "Test failed: ${ezdxData.failedMsg}"
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
//        // Show result based on test type and values
//        Toast.makeText(this, "Result: ${ezdxData.resultData}", Toast.LENGTH_LONG).show()
//    }
//
//    override fun onHCDeviceInfo(hcDeviceData: HCDeviceData) {
//        runOnUiThread {
//            Toast.makeText(this,
//                "Device authenticated: ${hcDeviceData.serialNumber}",
//                Toast.LENGTH_SHORT).show()
//            // Navigate to test screen or show next options
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
                putExtra("device_connected", true) // Flag to indicate device is connected
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