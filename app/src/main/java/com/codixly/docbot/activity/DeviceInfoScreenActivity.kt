package com.codixly.docbot.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.databinding.ActivityDeviceInfoScreenBinding

class DeviceInfoScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceInfoScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize view binding
        binding = ActivityDeviceInfoScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(binding.deviceinfo) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button click handler
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
