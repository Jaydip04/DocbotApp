package com.codixly.docbot.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.BluetoothConnect
import com.codixly.docbot.databinding.ActivitySplashScreenBinding
import com.codixly.docbot.network.ApiClient
import com.codixly.docbot.model.VerifyTokenRequest
import com.codixly.docbot.model.VerifyTokenResponse
import com.codixly.docbot.model.LogoutRequest
import com.codixly.docbot.model.LogoutResponse
import com.codixly.docbot.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        apiService = ApiClient.instance

        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token.isNullOrEmpty()) {
            navigateToOnboarding()
        } else {
            verifyToken(token)
        }
    }

    private fun verifyToken(token: String) {
        val request = VerifyTokenRequest(token)

        apiService.verifyToken(request).enqueue(object : Callback<VerifyTokenResponse> {
            override fun onResponse(call: Call<VerifyTokenResponse>, response: Response<VerifyTokenResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val verifyResponse = response.body()!!

                    if (verifyResponse.status == true) {
                        // Token is valid, user is logged in
                        Log.d("SplashScreen", "Token verified successfully")
                        Log.d("SplashScreen", "verifyResponse: ${verifyResponse.message}")

                        // Update shared preferences with latest user data
                        updateUserData(verifyResponse)

                        navigateToBluetoothScreen()
                    } else {
                        Log.d("SplashScreen", "Token verification failed: ${verifyResponse.message}")
                        handleInvalidToken()
                    }
                } else {
                    Log.e("SplashScreen", "Token verification API failed: ${response.code()}")
                    handleInvalidToken()
                }
            }

            override fun onFailure(call: Call<VerifyTokenResponse>, t: Throwable) {
                Log.e("SplashScreen", "Token verification failed", t)
                handleInvalidToken()
            }
        })
    }

    private fun updateUserData(verifyResponse: VerifyTokenResponse) {
        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        with(sharedPref.edit()) {
            verifyResponse.customer?.let { customer ->
                putString("token", customer.token)
                putString("name", customer.name)
                putString("username", customer.username)
                putString("email", customer.email)
                putString("mobile", customer.mobile)
                putString("machine_id", customer.machine_id)
                putString("customer_unique_id", customer.customer_unique_id)
            }
            apply()
        }
    }

    private fun handleInvalidToken() {
        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        val customerUniqueId = sharedPref.getString("customer_unique_id", null)

        if (!customerUniqueId.isNullOrEmpty()) {
            logoutUser(customerUniqueId)
        } else {
            clearUserData()
            navigateToOnboarding()
        }
    }

    private fun logoutUser(customerUniqueId: String) {
        val logoutRequest = LogoutRequest(customerUniqueId)

        apiService.logout(logoutRequest).enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val logoutResponse = response.body()!!
                    Log.d("SplashScreen", "Logout response: ${logoutResponse.message}")
                } else {
                    Log.e("SplashScreen", "Logout API failed: ${response.code()}")
                }

                // Clear data and navigate to onboarding regardless of logout API response
                clearUserData()
                navigateToOnboarding()
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                Log.e("SplashScreen", "Logout API failed", t)

                // Clear data and navigate to onboarding even if logout API fails
                clearUserData()
                navigateToOnboarding()
            }
        })
    }

    private fun clearUserData() {
        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }

    private fun navigateToOnboarding() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnboardingScreenOneActivity::class.java))
            finish()
        }, 3000)
    }

    private fun navigateToBluetoothScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, BluetoothScanScreenActivity::class.java))
            finish()
        }, 3000)

    }
}