//package com.codixly.docbot.activity
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.os.Bundle
//import android.os.Handler
//import android.view.LayoutInflater
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.codixly.docbot.databinding.ActivityRegistrationScreenBinding
//import com.codixly.docbot.databinding.DialogAuthLoginPatientsSuccessBinding
//import com.codixly.docbot.databinding.DialogAuthRegPatientsSuccessBinding
//import com.codixly.docbot.model.PatientRegistrationRequest
//import com.codixly.docbot.model.PatientRegistrationResponse
//import com.codixly.docbot.network.ApiClient
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class RegistrationScreenActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityRegistrationScreenBinding
////    private lateinit var sharedPref: SharedPreferences
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
////ok?
//        binding = ActivityRegistrationScreenBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        binding.ivBack.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
//
//        val receivedMobile = intent.getStringExtra("mobile") ?: ""
//        val receivedEmail = intent.getStringExtra("email") ?: ""
//
//
//        binding.etMobile.setText(receivedMobile)
//        binding.etMobile.isEnabled = false
//        binding.etMobile.isFocusable = false
//        binding.etMobile.isCursorVisible = false
//
//        binding.etEmail.setText(receivedEmail)
//        binding.etEmail.isEnabled = true
//        binding.etEmail.isFocusable = true
//        binding.etEmail.isCursorVisible = true
//
////        binding.btnContainer.setOnClickListener {
////            if (areAllFieldsFilled()) {
////                showSuccessDialog()
////            } else {
////                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
////            }
////        }
//        binding.btnContainer.setOnClickListener {
//            if (areAllFieldsFilled()) {
//                // Retrieve customer_unique_id from SharedPreferences
//                val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
//                val customerId = sharedPref.getString("customer_unique_id", null)
////                val customerUniqueId = sharedPref.getString("customer_unique_id", null)
//                val request = PatientRegistrationRequest(
//                    paitent_name = binding.etName.text.toString(),
//                    paitent_mobile = binding.etMobile.text.toString(),
//                    paitent_email = binding.etEmail.text.toString(),
//                    gender = binding.etGender.text.toString(),
//                    dob = binding.etDob.text.toString(),
//                    address = binding.etAdress.text.toString(),
//                    customer_unique_id = customerId ?: ""
//                )
//
////                ApiClient.instance.registerPatient(request).enqueue(object : Callback<YourResponseType> {
////                    override fun onResponse(call: Call<YourResponseType>, response: Response<YourResponseType>) {
////                        if (response.isSuccessful) {
////                            showSuccessDialog()
////                        } else {
////                            Toast.makeText(this@RegistrationScreenActivity, "Registration failed", Toast.LENGTH_SHORT).show()
////                        }
////                    }
////
////                    override fun onFailure(call: Call<YourResponseType>, t: Throwable) {
////                        Toast.makeText(this@RegistrationScreenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
////                    }
////                })
//                ApiClient.instance.registerPatient(request).enqueue(object :
//                    Callback<PatientRegistrationResponse> {
//                    override fun onResponse(call: Call<PatientRegistrationResponse>, response: Response<PatientRegistrationResponse>) {
//                        if (response.isSuccessful && response.body() != null) {
//                            val registrationResponse = response.body()!!
//                            if (registrationResponse.status) {
//                                // Registration was successful
//                                Toast.makeText(this@RegistrationScreenActivity, registrationResponse.message, Toast.LENGTH_SHORT).show()
//
//                                // You can access patient details like this:
//                                val patient = registrationResponse.paitent
//                                // For example, you can save the token for future use
//                                val token = patient.token
//
//                                // Optionally, navigate to another screen or perform other actions
//                                showSuccessDialog()
//                            } else {
//                                // Handle the case where the registration was not successful
//                                Toast.makeText(this@RegistrationScreenActivity, registrationResponse.message, Toast.LENGTH_SHORT).show()
//                            }
//                        } else {
//                            // Handle the case where the response was not successful
//                            Toast.makeText(this@RegistrationScreenActivity, "Registration failed", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<PatientRegistrationResponse>, t: Throwable) {
//                        Toast.makeText(this@RegistrationScreenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//                    }
//                })
//
//            } else {
//                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//    }
//
//    private fun areAllFieldsFilled(): Boolean {
//        return binding.etName.text.toString().trim().isNotEmpty() &&
//                binding.etDob.text.toString().trim().isNotEmpty() &&
//                binding.etGender.text.toString().trim().isNotEmpty() &&
//                binding.etEmail.text.toString().trim().isNotEmpty() &&
//                binding.etMobile.text.toString().trim().isNotEmpty() &&
//                binding.etAdress.text.toString().trim().isNotEmpty()
//    }
//
//    private fun showSuccessDialog() {
//        val dialogBinding = DialogAuthRegPatientsSuccessBinding.inflate(LayoutInflater.from(this))
//
//        val dialog = AlertDialog.Builder(this)
//            .setView(dialogBinding.root)
//            .setCancelable(false)
//            .create()
//
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        dialog.setOnShowListener {
//            dialog.window?.let { window ->
//                val params = window.attributes
//                params.width = WindowManager.LayoutParams.MATCH_PARENT
//                params.horizontalMargin = 0.05f
//                window.attributes = params
//            }
//        }
//
//        dialog.show()
//
//        Handler().postDelayed({
//            dialog.dismiss()
//        }, 2000)
//    }
//}

package com.codixly.docbot.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.databinding.ActivityRegistrationScreenBinding
import com.codixly.docbot.databinding.DialogAuthRegPatientsSuccessBinding
import com.codixly.docbot.model.PatientRegistrationRequest
import com.codixly.docbot.model.PatientRegistrationResponse
import com.codixly.docbot.network.ApiClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationScreenBinding
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPref = getSharedPreferences("DocBotPrefs", MODE_PRIVATE)

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val receivedMobile = intent.getStringExtra("mobile") ?: ""
        val receivedEmail = intent.getStringExtra("email") ?: ""

        binding.etMobile.setText(receivedMobile)
        binding.etMobile.isEnabled = false

        binding.etEmail.setText(receivedEmail)

//        binding.btnContainer.setOnClickListener {
//            if (areAllFieldsFilled()) {
//                val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
//                val customerId = sharedPref.getString("customer_unique_id", null)
//
//                if (customerId.isNullOrEmpty()) {
//                    Toast.makeText(this, "Customer ID is missing", Toast.LENGTH_SHORT).show()
//                    Log.d("RegistrationDebug", "Missing customer_unique_id in SharedPreferences")
//                    return@setOnClickListener
//                }
//
//                Log.d("RegistrationDebug", "Customer ID: $customerId")
//
//                val request = PatientRegistrationRequest(
//                    paitent_name = binding.etName.text.toString(),
//                    paitent_mobile = binding.etMobile.text.toString(),
//                    paitent_email = binding.etEmail.text.toString(),
//                    gender = binding.etGender.text.toString(),
//                    dob = binding.etDob.text.toString(),
//                    address = binding.etAdress.text.toString(),
//                    customer_unique_id = customerId
//                )
//
//                Log.d("RegistrationDebug", "Sending request: $request")
//
//                ApiClient.instance.registerPatient(request).enqueue(object :
//                    Callback<PatientRegistrationResponse> {
//                    override fun onResponse(
//                        call: Call<PatientRegistrationResponse>,
//                        response: Response<PatientRegistrationResponse>
//                    ) {
//                        if (response.isSuccessful && response.body()?.status == true) {
//                            val registrationResponse = response.body()!!
//                            Log.d("RegistrationDebug", "API Success: $registrationResponse")
//
//                            if (registrationResponse.status) {
//                                val patient = registrationResponse.paitent
//                                val sharedPref = getSharedPreferences("paitent_data", MODE_PRIVATE)
//                                with(sharedPref.edit()) {
//                                    putString("paitent_id", patient.paitent_id.toString())
//                                    putString("paitent_unique_id", patient.paitent_unique_id)
//                                    putString("paitent_name", patient.paitent_name)
//                                    putString("paitent_email", patient.paitent_email)
//                                    putString("paitent_mobile", patient.paitent_mobile)
//                                    putString("gender", patient.gender)
//                                    putString("dob", patient.dob)
//                                    putString("age", patient.age)
//                                    putString("address", patient.address)
//                                    putString("token", patient.token)
//                                    apply()
//                                }
//
//                                Log.d("paitent_id","${sharedPref.getString("paitent_id ", "")}");
//                                Log.d("token","${sharedPref.getString("token", "")}");
//                                Log.d("paitent_unique_id","${sharedPref.getString("paitent_unique_id", "")}");
//                                Toast.makeText(
//                                    this@RegistrationScreenActivity,
//                                    registrationResponse.message,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//
//                                Log.d("RegistrationDebug", "Patient data saved to SharedPreferences")
//                                showSuccessDialog()
//                            } else {
//                                Log.d("RegistrationDebug", "API response status false: ${registrationResponse.message}")
//                                Toast.makeText(
//                                    this@RegistrationScreenActivity,
//                                    registrationResponse.message,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        } else {
//                            val registrationResponse = response.body()!!
//                            Toast.makeText(
//                                this@RegistrationScreenActivity,
//                                registrationResponse.message,
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            Log.d("RegistrationDebug", "API Response Error: ${response.errorBody()?.string()}")
//                        }
//                    }
//
//                    override fun onFailure(call: Call<PatientRegistrationResponse>, t: Throwable) {
//                        Log.e("RegistrationDebug", "API call failed", t)
//                        Toast.makeText(
//                            this@RegistrationScreenActivity,
//                            "Error: ${t.message}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                })
//            } else {
//                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
//                Log.d("RegistrationDebug", "Validation failed: not all fields are filled")
//            }
//        }

        binding.btnContainer.setOnClickListener {
            if (areAllFieldsFilled()) {
                val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                val customerId = sharedPref.getString("customer_unique_id", null)

                if (customerId.isNullOrEmpty()) {
                    Toast.makeText(this, "Customer ID is missing", Toast.LENGTH_SHORT).show()
                    Log.d("RegistrationDebug", "Missing customer_unique_id in SharedPreferences")
                    return@setOnClickListener
                }

                val request = PatientRegistrationRequest(
                    paitent_name = binding.etName.text.toString(),
                    paitent_mobile = binding.etMobile.text.toString(),
                    paitent_email = binding.etEmail.text.toString(),
                    gender = binding.etGender.text.toString(),
                    dob = binding.etDob.text.toString(),
                    address = binding.etAdress.text.toString(),
                    customer_unique_id = customerId
                )

                Log.d("RegistrationDebug", "Sending request: $request")

                // ⏳ Show progress before API call
                showProgress(true)

                ApiClient.instance.registerPatient(request).enqueue(object :
                    Callback<PatientRegistrationResponse> {
                    override fun onResponse(
                        call: Call<PatientRegistrationResponse>,
                        response: Response<PatientRegistrationResponse>
                    ) {
                        // ✅ Hide progress after getting any response
                        showProgress(false)

                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                Log.d("RegistrationDebug", "API Success: $body")

                                if (body.status) {
                                    val patient = body.paitent
                                    val sharedPref = getSharedPreferences("paitent_data", MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putString("paitent_id", patient.paitent_id.toString())
                                        putString("paitent_unique_id", patient.paitent_unique_id)
                                        putString("paitent_name", patient.paitent_name)
                                        putString("paitent_email", patient.paitent_email)
                                        putString("paitent_mobile", patient.paitent_mobile)
                                        putString("gender", patient.gender)
                                        putString("dob", patient.dob)
                                        putString("age", patient.age)
                                        putString("address", patient.address)
                                        putString("token", patient.token)
                                        apply()
                                    }

                                    Log.d("paitent_id", "${sharedPref.getString("paitent_id", "")}")
                                    Log.d("token", "${sharedPref.getString("token", "")}")
                                    Log.d("paitent_unique_id", "${sharedPref.getString("paitent_unique_id", "")}")
                                    Toast.makeText(
                                        this@RegistrationScreenActivity,
                                        body.message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    Log.d("RegistrationDebug", "Patient data saved to SharedPreferences")
                                    showSuccessDialog()
                                } else {
                                    Toast.makeText(
                                        this@RegistrationScreenActivity,
                                        body.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.d("RegistrationDebug", "API status=false: ${body.message}")
                                }
                            } else {
                                Toast.makeText(this@RegistrationScreenActivity, "Empty response", Toast.LENGTH_SHORT).show()
                                Log.d("RegistrationDebug", "Empty response body")
                            }
                        } else {
                            // ⚠ Handle non-200 errors (e.g. 400, 500)
                            showProgress(false)
                            val errorBodyString = response.errorBody()?.string()
                            Log.d("RegistrationDebug", "API Response Error Body: $errorBodyString")

                            errorBodyString?.let {
                                try {
                                    val jsonObject = JSONObject(it)
                                    val message = jsonObject.getString("message")
                                    Toast.makeText(this@RegistrationScreenActivity, message, Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(this@RegistrationScreenActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PatientRegistrationResponse>, t: Throwable) {
                        showProgress(false)
                        Log.e("RegistrationDebug", "API call failed", t)
                        Toast.makeText(
                            this@RegistrationScreenActivity,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                Log.d("RegistrationDebug", "Validation failed: not all fields are filled")
            }
        }


    }

    private fun showProgress(show: Boolean) {
        binding.btnLoader.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegistrationText.visibility = if (show) View.GONE else View.VISIBLE
        binding.btnContainer.isEnabled = !show
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
