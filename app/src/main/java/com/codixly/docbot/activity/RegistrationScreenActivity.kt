package com.codixly.docbot.activity

import android.content.SharedPreferences
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
import com.codixly.docbot.model.PatientRegistrationRequest
import com.codixly.docbot.model.PatientRegistrationResponse
import com.codixly.docbot.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationScreenBinding
    private lateinit var sharedPref: SharedPreferences
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

//        binding.btnContainer.setOnClickListener {
//            if (areAllFieldsFilled()) {
//                showSuccessDialog()
//            } else {
//                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
//            }
//        }
        binding.btnContainer.setOnClickListener {
            if (areAllFieldsFilled()) {
                // Retrieve customer_unique_id from SharedPreferences
                val customerUniqueId = sharedPref.getString("customer_unique_id", null)
                val request = PatientRegistrationRequest(
                    paitent_name = binding.etName.text.toString(),
                    paitent_mobile = binding.etMobile.text.toString(),
                    paitent_email = binding.etEmail.text.toString(),
                    gender = binding.etGender.text.toString(),
                    dob = binding.etDob.text.toString(),
                    address = binding.etAdress.text.toString(),
                    customer_unique_id = customerUniqueId ?: "" // Use empty string if null// or get it dynamically
                )

//                ApiClient.instance.registerPatient(request).enqueue(object : Callback<YourResponseType> {
//                    override fun onResponse(call: Call<YourResponseType>, response: Response<YourResponseType>) {
//                        if (response.isSuccessful) {
//                            showSuccessDialog()
//                        } else {
//                            Toast.makeText(this@RegistrationScreenActivity, "Registration failed", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<YourResponseType>, t: Throwable) {
//                        Toast.makeText(this@RegistrationScreenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//                    }
//                })
                ApiClient.instance.registerPatient(request).enqueue(object :
                    Callback<PatientRegistrationResponse> {
                    override fun onResponse(call: Call<PatientRegistrationResponse>, response: Response<PatientRegistrationResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val registrationResponse = response.body()!!
                            if (registrationResponse.status) {
                                // Registration was successful
                                Toast.makeText(this@RegistrationScreenActivity, registrationResponse.message, Toast.LENGTH_SHORT).show()

                                // You can access patient details like this:
                                val patient = registrationResponse.paitent
                                // For example, you can save the token for future use
                                val token = patient.token

                                // Optionally, navigate to another screen or perform other actions
                                showSuccessDialog()
                            } else {
                                // Handle the case where the registration was not successful
                                Toast.makeText(this@RegistrationScreenActivity, registrationResponse.message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Handle the case where the response was not successful
                            Toast.makeText(this@RegistrationScreenActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<PatientRegistrationResponse>, t: Throwable) {
                        Toast.makeText(this@RegistrationScreenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

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
