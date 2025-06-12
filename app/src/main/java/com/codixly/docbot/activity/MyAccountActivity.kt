package com.codixly.docbot.activity

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.codixly.docbot.R
import com.codixly.docbot.databinding.ActivityMyAccountBinding
import com.codixly.docbot.model.CustomerDataRequest
import com.codixly.docbot.model.CustomerDataResponse
import com.codixly.docbot.network.ApiClient
import com.codixly.docbot.utils.TextDrawable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Apply insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fetch data from shared preferences
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)
        val token = sharedPref.getString("token", null)

        if (!customerId.isNullOrEmpty() && !token.isNullOrEmpty()) {
            fetchCustomerData(customerId, token)
        }

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun fetchCustomerData(customerId: String, token: String) {
        val request = CustomerDataRequest(customerId, token)

        ApiClient.instance.getCustomerData(request).enqueue(object : Callback<CustomerDataResponse> {
            override fun onResponse(call: Call<CustomerDataResponse>, response: Response<CustomerDataResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val customerResponse = response.body()!!
                    if (customerResponse.status && customerResponse.customer_data != null) {
                        val customer = customerResponse.customer_data

//                        val formattedName = customer.name.replaceFirstChar { it.uppercaseChar() }
                        binding.etName.setText("${customer.name}")
                        binding.etUsername.setText("${customer.username}")
                        binding.etEmail.setText("${customer.email}")
                        binding.etMobile.setText("${customer.mobile}")

                        if (customer.customer_profile.isNullOrEmpty()) {
                            val firstLetter = customer.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                            val drawable = TextDrawable(firstLetter, this@MyAccountActivity)
                            binding.profileImage.setImageDrawable(drawable)
                        } else {
                            Glide.with(this@MyAccountActivity)
                                .load(customer.customer_profile)
                                .placeholder(R.drawable.doctor)
                                .into(binding.profileImage)
                        }

                    } else {
                        Toast.makeText(this@MyAccountActivity, customerResponse.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MyAccountActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CustomerDataResponse>, t: Throwable) {
                Toast.makeText(this@MyAccountActivity, "Failed: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
