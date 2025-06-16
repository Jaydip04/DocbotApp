//package com.codixly.docbot.activity
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.codixly.docbot.databinding.ActivityAuthenticateScreenBinding
//
//class AuthenticateScreenActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityAuthenticateScreenBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAuthenticateScreenBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        enableEdgeToEdge()
//
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
////        val emailEditText = binding.etEmail
//        val mobileEditText = binding.etMobile
//
//        // Toggle fields
////        emailEditText.addTextChangedListener(object : TextWatcher {
////            override fun afterTextChanged(s: Editable?) {
////                mobileEditText.isEnabled = s.isNullOrEmpty()
////            }
////            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
////            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
////        })
//
//        mobileEditText.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
////                emailEditText.isEnabled = s.isNullOrEmpty()
//            }
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//
//        // Back button
//        binding.ivBack.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//
//        // Submit button
//        binding.btnContainer.setOnClickListener {
////            val email = emailEditText.text.toString().trim()
//            val mobile = mobileEditText.text.toString().trim()
//
//            if (
////                email.isEmpty() &&
//                mobile.isEmpty()) {
//                Toast.makeText(this, "Please enter either email or mobile", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            val fullPhoneNumber = binding.ccp.fullNumberWithPlus
//            val intent = Intent(this, OtpVerifyScreenActivity::class.java)
////            if (email.isNotEmpty()) {
////                intent.putExtra("email", email)
////            } else
//            if (mobile.isNotEmpty()) {
//                intent.putExtra("mobile", fullPhoneNumber )
//            }
//            startActivity(intent)
//        }
//    }
//}
package com.codixly.docbot.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.databinding.ActivityAuthenticateScreenBinding

class AuthenticateScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticateScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mobileEditText = binding.etMobile

        // Optional: React to text change if needed
        mobileEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Back button click
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Submit button click
        binding.btnContainer.setOnClickListener {
            val mobile = mobileEditText.text.toString().trim()

            if (mobile.isEmpty()) {
                Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.ccp.registerCarrierNumberEditText(binding.etMobile)

            val fullPhoneNumber = binding.ccp.fullNumberWithPlus
            Log.d("AuthenticateScreenActivity","Full Phone NUmber: $fullPhoneNumber")
            val intent = Intent(this, OtpVerifyScreenActivity::class.java)
            intent.putExtra("mobile", fullPhoneNumber)
            startActivity(intent)
        }
    }
}
