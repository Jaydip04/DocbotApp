package com.codixly.docbot.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.R
import com.codixly.docbot.databinding.ActivityBluetoothScanScreenBinding

class BluetoothScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothScanScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBluetoothScanScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContentView(R.layout.activity_bluetooth_scan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.scanButton.setOnClickListener {
            val intent = Intent(this, BluetoothScanActivity::class.java)
            startActivity(intent)
            finish() // Optional: close this activity
        }

    }
}