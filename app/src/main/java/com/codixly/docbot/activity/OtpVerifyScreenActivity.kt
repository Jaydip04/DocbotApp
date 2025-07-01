//package com.codixly.docbot.activity
//
//import android.app.AlertDialog
//import android.content.Context
//import android.content.Intent
//import android.os.*
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.codixly.docbot.R
//import com.codixly.docbot.databinding.ActivityOtpVerifyScreenBinding
//import com.codixly.docbot.model.*
//import com.codixly.docbot.network.ApiClient
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class OtpVerifyScreenActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityOtpVerifyScreenBinding
//    private var existingPatient: Boolean = false
//    private lateinit var mobile: String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityOtpVerifyScreenBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
////        val mobile = intent.getStringExtra("mobile") ?: ""
//        val mobile = intent.getStringExtra("mobile") ?: ""
////        val email = intent.getStringExtra("email") ?: ""
//
////        if (email.isNotEmpty()) {
////            sendOtpToEmail(email)
////        } else
//            if (mobile.isNotEmpty()) {
//            sendOtpAndSetFields(mobile)
//        }
//
//        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
//        val customerId = sharedPref.getString("customer_unique_id", null)
//
//        binding.ivBack.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//
//        binding.btnContainer.setOnClickListener {
//            val otp = getEnteredOtp()
//            if (otp.length == 4) {
//                verifyOtp(mobile, otp.toInt(), existingPatient)
//            } else {
//                Toast.makeText(this, "Please enter 4-digit OTP", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        setupOtpInputs()
//    }
//
//    private fun sendOtpAndSetFields(mobile: String) {
//        val request = SendOtpRequest(mobile)
//        ApiClient.instance.sendOtp(request).enqueue(object : Callback<SendOtpResponse> {
//            override fun onResponse(call: Call<SendOtpResponse>, response: Response<SendOtpResponse>) {
//                if (response.isSuccessful && response.body()?.status == true) {
//                    val otp = response.body()?.otp.toString()
//                    existingPatient = response.body()?.existingPaitent == true
//                    Log.d("API", "OTP: $otp, existingPatient: $existingPatient")
//                    setOtpFields(otp)
//                } else {
//                    Toast.makeText(this@OtpVerifyScreenActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<SendOtpResponse>, t: Throwable) {
//                Toast.makeText(this@OtpVerifyScreenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun setOtpFields(otp: String) {
//        val digits = otp.padStart(4, '0')
//        if (digits.length >= 4) {
//            binding.etOtp1.setText(digits[0].toString())
//            binding.etOtp2.setText(digits[1].toString())
//            binding.etOtp3.setText(digits[2].toString())
//            binding.etOtp4.setText(digits[3].toString())
//        }
//    }
//
//    private fun getEnteredOtp(): String {
//        return listOf(
//            binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4
//        ).joinToString("") { it.text.toString().trim() }
//    }
//
//    private fun setupOtpInputs() {
//        val editTexts = listOf(
//            binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4
//        )
//
//        for (i in editTexts.indices) {
//            editTexts[i].addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                    if (s?.length == 1 && i < editTexts.lastIndex) {
//                        editTexts[i + 1].requestFocus()
//                    }
//                }
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            })
//        }
//    }
//
////    private fun verifyOtp(mobile: String, otp: Int, existingPatient: Boolean) {
////        showProgress(true)
////        val request = VerifyOtpRequest(mobile, otp, existingPatient)
////        ApiClient.instance.verifyOtp(request).enqueue(object : Callback<VerifyOtpResponse> {
////            override fun onResponse(call: Call<VerifyOtpResponse>, response: Response<VerifyOtpResponse>) {
////                showProgress(false)
////                if (response.isSuccessful && response.body()?.status == true) {
////                    val responseBody = response.body()
////                    if (existingPatient) {
//////                        val patient = responseBody?.paitent
//////                        if (patient == null) {
////                            showSuccessDialog {
////                                val intent = Intent(this@OtpVerifyScreenActivity, MainActivity::class.java)
////                                startActivity(intent)
////                                finish()
////                            }
//////                        } else {
//////                            Toast.makeText(this@OtpVerifyScreenActivity, "Patient data missing", Toast.LENGTH_SHORT).show()
//////                        }
////                    } else {
////
////                        showSuccessDialog {
////                            val intent = Intent(this@OtpVerifyScreenActivity, RegistrationScreenActivity::class.java)
////                            intent.putExtra("mobile", mobile)
////                            startActivity(intent)
////                            finish()
////                        }
////
////                    }
////
////                } else {
////                    Toast.makeText(this@OtpVerifyScreenActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
////                }
////            }
////
////            override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
////                showProgress(false)
////                Toast.makeText(this@OtpVerifyScreenActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
////            }
////        })
////    }
//
//    private fun verifyOtp(mobile: String, otp: Int, existingPatient: Boolean) {
//        Log.d("OTP_VERIFY", "Starting OTP verification...")
//        Log.d("OTP_VERIFY", "Mobile: $mobile, OTP: $otp, Existing Patient: $existingPatient")
//
//        showProgress(true)
//
//        val request = VerifyOtpRequest(mobile, otp, existingPatient)
//        Log.d("OTP_VERIFY", "VerifyOtpRequest: $request")
//
//        ApiClient.instance.verifyOtp(request).enqueue(object : Callback<VerifyOtpResponse> {
//            override fun onResponse(call: Call<VerifyOtpResponse>, response: Response<VerifyOtpResponse>) {
//                showProgress(false)
//                Log.d("OTP_VERIFY", "API Response received. Success: ${response.isSuccessful}")
//
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    Log.d("OTP_VERIFY", "Response Body: $responseBody")
//
//                    if (responseBody?.status == true) {
//                        if (existingPatient) {
//                            Log.d("OTP_VERIFY", "Existing patient flow")
//
//                            showSuccessDialog {
//                                val intent = Intent(this@OtpVerifyScreenActivity, MainActivity::class.java)
//                                Log.d("OTP_VERIFY", "Navigating to MainActivity")
//                                startActivity(intent)
//                                finish()
//                            }
//
//                        } else {
//                            Log.d("OTP_VERIFY", "New patient flow")
//
//                            showSuccessDialog {
//                                val intent = Intent(this@OtpVerifyScreenActivity, RegistrationScreenActivity::class.java)
//                                intent.putExtra("mobile", mobile)
//                                Log.d("OTP_VERIFY", "Navigating to RegistrationScreenActivity with mobile: $mobile")
//                                startActivity(intent)
//                                finish()
//                            }
//                        }
//
//                    } else {
//                        Log.d("OTP_VERIFY", "OTP verification failed - status = false")
//                        Toast.makeText(this@OtpVerifyScreenActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Log.d("OTP_VERIFY", "API response unsuccessful: ${response.errorBody()?.string()}")
//                    Toast.makeText(this@OtpVerifyScreenActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
//                showProgress(false)
//                Log.e("OTP_VERIFY", "Network error: ${t.message}", t)
//                Toast.makeText(this@OtpVerifyScreenActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun showProgress(show: Boolean) {
//        binding.btnLoader.visibility = if (show) View.VISIBLE else View.GONE
//        binding.btnSignInText.visibility = if (show) View.GONE else View.VISIBLE
//        binding.btnContainer.isEnabled = !show
//    }
//
//    private fun showSuccessDialog(onDismiss: () -> Unit) {
//        try {
//            if (isFinishing || isDestroyed) {
//                onDismiss()
//                return
//            }
//
//            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_auth_login_patients_success, null)
//            val dialog = AlertDialog.Builder(this)
//                .setView(dialogView)
//                .setCancelable(false)
//                .create()
//
//            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//            dialog.setOnShowListener {
//                val window = dialog.window
//                if (window != null) {
//                    val params = window.attributes
//                    params.width = WindowManager.LayoutParams.MATCH_PARENT
//                    params.horizontalMargin = 0.05f
//                    window.attributes = params
//                }
//            }
//
//            dialog.show()
//
//            Handler(Looper.getMainLooper()).postDelayed({
//                if (dialog.isShowing && !isFinishing && !isDestroyed) {
//                    dialog.dismiss()
//                }
//                onDismiss()
//            }, 2000)
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            onDismiss()
//        }
//    }
//}
//package com.codixly.docbot.activity
//
//import android.app.AlertDialog
//import android.content.Context
//import android.content.Intent
//import android.os.*
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.codixly.docbot.R
//import com.codixly.docbot.databinding.ActivityOtpVerifyScreenBinding
//import com.codixly.docbot.model.*
//import com.codixly.docbot.network.ApiClient
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class OtpVerifyScreenActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityOtpVerifyScreenBinding
//    private var existingPatient: Boolean = false
//    private lateinit var mobile: String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityOtpVerifyScreenBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        mobile = intent.getStringExtra("mobile") ?: ""
//
//        if (mobile.isNotEmpty()) {
//            sendOtpAndSetFields(mobile)
//        }
//
//        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
//        val customerId = sharedPref.getString("customer_unique_id", null)
//        Log.d("USER_DATA", "customer_unique_id: $customerId")
//
//        binding.ivBack.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//
//        binding.btnContainer.setOnClickListener {
//            val otp = getEnteredOtp()
//            if (otp.length == 4) {
//                if (customerId != null) {
//                    verifyOtp(mobile, otp.toInt(), existingPatient,customerId)
//                }
//            } else {
//                Toast.makeText(this, "Please enter 4-digit OTP", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        setupOtpInputs()
//    }
//
//    private fun sendOtpAndSetFields(mobile: String) {
//        val request = SendOtpRequest(mobile)
//        ApiClient.instance.sendOtp(request).enqueue(object : Callback<SendOtpResponse> {
//            override fun onResponse(call: Call<SendOtpResponse>, response: Response<SendOtpResponse>) {
//                if (response.isSuccessful && response.body()?.status == true) {
//                    val otp = response.body()?.otp.toString()
//                    existingPatient = response.body()?.existingPaitent == true
//                    Log.d("API", "OTP: $otp, existingPatient: $existingPatient")
//                    setOtpFields(otp)
//                } else {
//                    Toast.makeText(this@OtpVerifyScreenActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<SendOtpResponse>, t: Throwable) {
//                Toast.makeText(this@OtpVerifyScreenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun setOtpFields(otp: String) {
//        val digits = otp.padStart(4, '0')
//        if (digits.length >= 4) {
//            binding.etOtp1.setText(digits[0].toString())
//            binding.etOtp2.setText(digits[1].toString())
//            binding.etOtp3.setText(digits[2].toString())
//            binding.etOtp4.setText(digits[3].toString())
//        }
//    }
//
//    private fun getEnteredOtp(): String {
//        return listOf(
//            binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4
//        ).joinToString("") { it.text.toString().trim() }
//    }
//
//    private fun setupOtpInputs() {
//        val editTexts = listOf(
//            binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4
//        )
//
//        for (i in editTexts.indices) {
//            editTexts[i].addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                    if (s?.length == 1 && i < editTexts.lastIndex) {
//                        editTexts[i + 1].requestFocus()
//                    }
//                }
//
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            })
//        }
//    }
//
//    private fun verifyOtp(mobile: String, otp: Int, existingPatient: Boolean,customerID: String) {
//        Log.d("OTP_VERIFY", "Starting OTP verification...")
//        Log.d("OTP_VERIFY", "Mobile: $mobile, OTP: $otp, Existing Patient: $existingPatient")
//
//        showProgress(true)
//
//        val request = VerifyOtpRequest(mobile, otp, existingPatient,customerID)
//        Log.d("OTP_VERIFY", "VerifyOtpRequest: $request")
//
//        ApiClient.instance.verifyOtp(request).enqueue(object : Callback<VerifyOtpResponse> {
//            override fun onResponse(call: Call<VerifyOtpResponse>, response: Response<VerifyOtpResponse>) {
//                showProgress(false)
//                Log.d("OTP_VERIFY", "API Response received. Success: ${response.isSuccessful}")
//
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    Log.d("OTP_VERIFY", "Response Body: $responseBody")
//
//                    if (responseBody?.status == true) {
//                        if (existingPatient) {
//                            Log.d("OTP_VERIFY", "Existing patient flow")
//
//                            showSuccessDialog {
//                                val intent = Intent(this@OtpVerifyScreenActivity, MainActivity::class.java)
//                                Log.d("OTP_VERIFY", "Navigating to MainActivity")
//                                startActivity(intent)
//                                finish()
//                            }
//
//                        } else {
//                            Log.d("OTP_VERIFY", "New patient flow")
//
//                            showSuccessDialog {
//                                val intent = Intent(this@OtpVerifyScreenActivity, RegistrationScreenActivity::class.java)
//                                intent.putExtra("mobile", mobile)
//                                Log.d("OTP_VERIFY", "Navigating to RegistrationScreenActivity with mobile: $mobile")
//                                startActivity(intent)
//                                finish()
//                            }
//                        }
//
//                    } else {
//                        Log.d("OTP_VERIFY", "OTP verification failed - status = false")
//                        Toast.makeText(this@OtpVerifyScreenActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Log.d("OTP_VERIFY", "API response unsuccessful: ${response.errorBody()?.string()}")
//                    Toast.makeText(this@OtpVerifyScreenActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
//                showProgress(false)
//                Log.e("OTP_VERIFY", "Network error: ${t.message}", t)
//                Toast.makeText(this@OtpVerifyScreenActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun showProgress(show: Boolean) {
//        binding.btnLoader.visibility = if (show) View.VISIBLE else View.GONE
//        binding.btnSignInText.visibility = if (show) View.GONE else View.VISIBLE
//        binding.btnContainer.isEnabled = !show
//    }
//
//    private fun showSuccessDialog(onDismiss: () -> Unit) {
//        try {
//            if (isFinishing || isDestroyed) {
//                onDismiss()
//                return
//            }
//
//            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_auth_login_patients_success, null)
//            val dialog = AlertDialog.Builder(this)
//                .setView(dialogView)
//                .setCancelable(false)
//                .create()
//
//            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//            dialog.setOnShowListener {
//                val window = dialog.window
//                if (window != null) {
//                    val params = window.attributes
//                    params.width = WindowManager.LayoutParams.MATCH_PARENT
//                    params.horizontalMargin = 0.05f
//                    window.attributes = params
//                }
//            }
//
//            dialog.show()
//
//            Handler(Looper.getMainLooper()).postDelayed({
//                if (dialog.isShowing && !isFinishing && !isDestroyed) {
//                    dialog.dismiss()
//                }
//                onDismiss()
//            }, 2000)
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            onDismiss()
//        }
//    }
//}
//


package com.codixly.docbot.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.R
import com.codixly.docbot.databinding.ActivityOtpVerifyScreenBinding
import com.codixly.docbot.model.*
import com.codixly.docbot.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpVerifyScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerifyScreenBinding
    private var existingPatient: Boolean = false
    private lateinit var mobile: String
    private var resendTimer: CountDownTimer? = null
    private var isResendEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mobile = intent.getStringExtra("mobile") ?: ""
        if (mobile.isNotEmpty()) {
            sendOtpAndSetFields(mobile)
        }

        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContainer.setOnClickListener {
            val otp = getEnteredOtp()
            if (otp.length == 4) {
                if (customerId != null) {
                    verifyOtp(mobile, otp.toInt(), existingPatient, customerId)
                }
            } else {
                Toast.makeText(this, "Please enter 4-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resendOtp.setOnClickListener {
            if (isResendEnabled) {
                sendOtpAndSetFields(mobile)
                startResendOtpTimer()
            }
        }

        setupOtpInputs()
        startResendOtpTimer()
    }

    private fun sendOtpAndSetFields(mobile: String) {
        val request = SendOtpRequest(mobile)
        ApiClient.instance.sendOtp(request).enqueue(object : Callback<SendOtpResponse> {
            override fun onResponse(call: Call<SendOtpResponse>, response: Response<SendOtpResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    val otp = response.body()?.otp.toString()
                    existingPatient = response.body()?.existingPaitent == true
                    Log.d("API", "OTP: $otp, existingPatient: $existingPatient")
                    setOtpFields(otp)
                } else {
                    Toast.makeText(this@OtpVerifyScreenActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SendOtpResponse>, t: Throwable) {
                Toast.makeText(this@OtpVerifyScreenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setOtpFields(otp: String) {
        val digits = otp.padStart(4, '0')
        if (digits.length >= 4) {
            binding.etOtp1.setText(digits[0].toString())
            binding.etOtp2.setText(digits[1].toString())
            binding.etOtp3.setText(digits[2].toString())
            binding.etOtp4.setText(digits[3].toString())
        }
    }

    private fun getEnteredOtp(): String {
        return listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4
        ).joinToString("") { it.text.toString().trim() }
    }

    private fun setupOtpInputs() {
        val editTexts = listOf(binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4)
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < editTexts.lastIndex) {
                        editTexts[i + 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun verifyOtp(mobile: String, otp: Int, existingPatient: Boolean, customerID: String) {
        Log.d("OTP_VERIFY", "Starting OTP verification...")
        showProgress(true)

        val request = VerifyOtpRequest(mobile, otp, existingPatient, customerID)

        ApiClient.instance.verifyOtp(request).enqueue(object : Callback<VerifyOtpResponse> {
            override fun onResponse(call: Call<VerifyOtpResponse>, response: Response<VerifyOtpResponse>) {
                showProgress(false)
                if (response.isSuccessful && response.body()?.status == true) {
                    if (existingPatient) {
                        showSuccessDialog {
//                            startActivity(Intent(this@OtpVerifyScreenActivity, MainActivity::class.java))
                            val intent = Intent(this@OtpVerifyScreenActivity, RegistrationScreenActivity::class.java)
                            intent.putExtra("mobile", mobile)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        showSuccessDialog {
                            val intent = Intent(this@OtpVerifyScreenActivity, RegistrationScreenActivity::class.java)
                            intent.putExtra("mobile", mobile)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this@OtpVerifyScreenActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                showProgress(false)
                Toast.makeText(this@OtpVerifyScreenActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showProgress(show: Boolean) {
        binding.btnLoader.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSignInText.visibility = if (show) View.GONE else View.VISIBLE
        binding.btnContainer.isEnabled = !show
    }

    private fun showSuccessDialog(onDismiss: () -> Unit) {
        try {
            if (isFinishing || isDestroyed) {
                onDismiss()
                return
            }

            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_auth_login_patients_success, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setOnShowListener {
                val window = dialog.window
                window?.let {
                    val params = it.attributes
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.horizontalMargin = 0.05f
                    it.attributes = params
                }
            }

            dialog.show()

            Handler(Looper.getMainLooper()).postDelayed({
                if (dialog.isShowing && !isFinishing && !isDestroyed) {
                    dialog.dismiss()
                }
                onDismiss()
            }, 2000)

        } catch (e: Exception) {
            e.printStackTrace()
            onDismiss()
        }
    }

    private fun startResendOtpTimer() {
        isResendEnabled = false
        binding.resendOtp.setTextColor(resources.getColor(R.color.black))
        binding.second.visibility = View.VISIBLE

        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(90000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.second.text = "(${seconds}s)"
            }

            override fun onFinish() {
                isResendEnabled = true
                binding.resendOtp.setTextColor(resources.getColor(R.color.teal_200))
                binding.second.text = ""
                binding.second.visibility = View.GONE
            }
        }.start()
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        super.onDestroy()
    }
}
