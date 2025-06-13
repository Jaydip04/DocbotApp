package com.codixly.docbot.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.R
import com.codixly.docbot.databinding.ActivitySignUpBinding
import com.codixly.docbot.model.LoginRequest
import com.codixly.docbot.model.LoginResponse
import com.codixly.docbot.network.ApiClient

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Toggle password visibility
        binding.passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_on) // Replace with visible eye icon
            } else {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off) // Replace with hidden eye icon
            }

            // Move cursor to end after toggling
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

//        binding.btnSignIn.setOnClickListener {
//            val username = binding.etClientId.text.toString().trim()
//            val password = binding.etPassword.text.toString().trim()
//
//            if (username.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//
//
//            val request = LoginRequest(username, password)
//
//            ApiClient.instance.loginUser(request).enqueue(object : retrofit2.Callback<LoginResponse> {
//                override fun onResponse(
//                    call: retrofit2.Call<LoginResponse>,
//                    response: retrofit2.Response<LoginResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        val res = response.body()
//                        if (res?.status == true) {
//                            Toast.makeText(this@SignUpActivity, "Login Successful", Toast.LENGTH_SHORT).show()
//                            // Save token or user data if needed
//                            val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
//                            with(sharedPref.edit()) {
//                                putString("token", res.customer?.token)
//                                putString("name", res.customer?.name)
//                                putString("username", res.customer?.username)
//                                putString("email", res.customer?.email)
//                                putString("mobile", res.customer?.mobile)
//                                putString("machine_id", res.customer?.machine_id)
//                                putString("bluetooth_id", res.machineData?.blutooth_id)
//                                apply()
//                            }
////                            Get Data from SharedPreferences
////                            val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
//                            val token = sharedPref.getString("token", null)
//                            val name = sharedPref.getString("name", null)
////                            val username = sharedPref.getString("username", null)
//                            val email = sharedPref.getString("email", null)
////                            val mobile = sharedPref.getString("mobile", null)
////                            val machineId = sharedPref.getString("machine_id", null)
////                            val bluetoothId = sharedPref.getString("bluetooth_id", null)
//
//                            Log.d("StoredData", "Token: $token")
//                            Log.d("StoredData", "Name: $name")
//                            Log.d("StoredData", "Email: $email")
//
////                            Clear Data on Logout
////                            val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
////                            sharedPref.edit().clear().apply()
//
//
//                            Log.d("LoginResponse", "Data saved to SharedPreferences")
//                            Log.d("LoginResponse", "Result: ${res}")
//                            Log.d("LoginResponse", "Customer Name: ${res.customer?.name}")
//                            Log.d("LoginResponse", "Username: ${res.customer?.username}")
//                            Log.d("LoginResponse", "Email: ${res.customer?.email}")
//                            Log.d("LoginResponse", "Mobile: ${res.customer?.mobile}")
//                            Log.d("LoginResponse", "Token: ${res.customer?.token}")
//                            Log.d("LoginResponse", "Machine ID: ${res.customer?.machine_id}")
//                            Log.d("LoginResponse", "Bluetooth ID: ${res.machineData?.blutooth_id}")
//                            val intent = Intent(this@SignUpActivity, BluetoothScanScreenActivity::class.java)
////            val intent = Intent(this@SignUpActivity, BluetoothConnect::class.java)
//                            startActivity(intent)
//                            finish()
////                            val token = res.customer?.token
//                        } else {
//                            Toast.makeText(this@SignUpActivity, res?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Toast.makeText(this@SignUpActivity, "Server error", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
//                    Log.d("Error", "Error: ${t.message}")
//                    Toast.makeText(this@SignUpActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }

        binding.btnSignInText.setOnClickListener {
            val username = binding.etClientId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Show loader and disable button
            binding.btnSignInText.isEnabled = false
            binding.btnSignInText.text = ""  // Clear text
            binding.btnLoader.visibility = View.VISIBLE


            val request = LoginRequest(username, password)

            ApiClient.instance.loginUser(request)
                .enqueue(object : retrofit2.Callback<LoginResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<LoginResponse>,
                        response: retrofit2.Response<LoginResponse>
                    ) {
                        // ✅ Hide loader and enable button
                        binding.btnLoader.visibility = View.GONE
                        binding.btnSignInText.text = "Sign In"
                        binding.btnSignInText.isEnabled = true


                        if (response.isSuccessful) {
                            val res = response.body()
                            if (res?.status == true) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("customer_unique_id", res.customer?.customer_unique_id)
                                    putString("name", res.customer?.name)
                                    putString("username", res.customer?.username)
                                    putString("email", res.customer?.email)
                                    putString("mobile", res.customer?.mobile)
                                    putString("token", res.customer?.token)
                                    putString("machine_id", res.customer?.machine_id)
                                    putString("inserted_date", res.customer?.inserted_date)
                                    putString("inserted_time", res.customer?.inserted_time)

                                    putString("bluetooth_id", res.machineData?.blutooth_id)
                                    putString("machine_unique_id", res.machineData?.machine_unique_id)
                                    apply()
                                }

//                                val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
//                                with(sharedPref.edit()) {
//                                    putString("token", res.customer?.token)
//                                    putString("name", res.customer?.name)
//                                    putString("username", res.customer?.username)
//                                    putString("email", res.customer?.email)
//                                    putString("mobile", res.customer?.mobile)
//                                    putString("machine_id", res.customer?.machine_id)
//                                    putString("bluetooth_id", res.machineData?.blutooth_id)
//                                    apply()
//                                }

//                                val intent =
//                                    Intent(
//                                        this@SignUpActivity,
//                                        BluetoothScanScreenActivity::class.java
//                                    )
                                val intent =
                                    Intent(
                                        this@SignUpActivity,
                                        MainActivity::class.java
                                    )
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    res?.message ?: "Login failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(this@SignUpActivity, "Server error", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                        // ✅ Hide loader and enable button

                        binding.btnLoader.visibility = View.GONE
                        binding.btnSignInText.text = "Sign In"
                        binding.btnSignInText.isEnabled = true


                        Log.d("Error", "Error: ${t.message}")
                        Toast.makeText(
                            this@SignUpActivity,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}

