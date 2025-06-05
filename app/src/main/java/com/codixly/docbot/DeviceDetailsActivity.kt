package com.codixly.docbot

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DeviceDetailsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        setContentView(R.layout.activity_device_details)

        val serialNumber = intent.getStringExtra("serial_number") ?: "N/A"
        val firmwareVersion = intent.getStringExtra("firmware_version") ?: "N/A"

        findViewById<TextView>(R.id.tv_serial_number).text = "Serial Number: $serialNumber"
        findViewById<TextView>(R.id.tv_firmware_version).text = "Firmware: $firmwareVersion"
    }
}
