package com.codixly.docbot.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.BluetoothConnect
import com.codixly.docbot.databinding.ActivityBluetoothScanScreenBinding

class BluetoothScanScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothScanScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate binding and set content view
        binding = ActivityBluetoothScanScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle scan button click
        binding.scanButton.setOnClickListener {
//            val intent = Intent(this@BluetoothScanScreenActivity, DeviceAddedSuccessfullyActivity::class.java)
            val intent = Intent(this@BluetoothScanScreenActivity, BluetoothConnect::class.java)
            startActivity(intent)
            finish() // Optional: close this activity
        }
    }
}
