package com.codixly.docbot.activity

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.databinding.ActivityRegistrationScreenBinding
import com.codixly.docbot.databinding.DialogAuthLoginPatientsSuccessBinding
import com.codixly.docbot.databinding.DialogAuthRegPatientsSuccessBinding

class RegistrationScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegistrationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val receivedMobile = intent.getStringExtra("mobile") ?: ""
        val receivedEmail = intent.getStringExtra("email") ?: ""


        binding.etMobile.setText(receivedMobile)
        binding.etMobile.isEnabled = false
        binding.etMobile.isFocusable = false
        binding.etMobile.isCursorVisible = false

        binding.etEmail.setText(receivedEmail)
        binding.etEmail.isEnabled = true
        binding.etEmail.isFocusable = true
        binding.etEmail.isCursorVisible = true

        binding.btnContainer.setOnClickListener {
            if (areAllFieldsFilled()) {
                showSuccessDialog()
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun areAllFieldsFilled(): Boolean {
        return binding.etName.text.toString().trim().isNotEmpty() &&
                binding.etDob.text.toString().trim().isNotEmpty() &&
                binding.etGender.text.toString().trim().isNotEmpty() &&
                binding.etEmail.text.toString().trim().isNotEmpty() &&
                binding.etMobile.text.toString().trim().isNotEmpty() &&
                binding.etAdress.text.toString().trim().isNotEmpty()
    }

    private fun showSuccessDialog() {
        val dialogBinding = DialogAuthRegPatientsSuccessBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            dialog.window?.let { window ->
                val params = window.attributes
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.horizontalMargin = 0.05f
                window.attributes = params
            }
        }

        dialog.show()

        Handler().postDelayed({
            dialog.dismiss()
        }, 2000)
    }
}
