package com.codixly.docbot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            // All permissions granted, proceed to next activity
            openNextActivity()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartScan = findViewById<Button>(R.id.btn_start)
        btnStartScan.setOnClickListener {
            checkBluetoothPermissions()
        }
    }

    private fun checkBluetoothPermissions() {
        val requiredPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        requiredPermissions.add(Manifest.permission.BLUETOOTH)
        requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isEmpty()) {
            openNextActivity()
        } else {
            bluetoothPermissionLauncher.launch(missingPermissions)
        }
    }

    private fun openNextActivity() {
        try {
            val intent = Intent(this, BluetoothConnect::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}