package com.codixly.docbot.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.codixly.docbot.R
import com.codixly.docbot.activity.FaqActivity
import com.codixly.docbot.activity.MyAccountActivity
import com.codixly.docbot.activity.OnboardingScreenOneActivity
import com.codixly.docbot.activity.PatientReportsActivity
import com.codixly.docbot.activity.SplashScreenActivity
import com.codixly.docbot.databinding.FragmentProfileBinding
import com.codixly.docbot.model.CustomerDataRequest
import com.codixly.docbot.model.CustomerDataResponse
import com.codixly.docbot.model.DeleteAccountRequest
import com.codixly.docbot.model.DeleteAccountResponse
import com.codixly.docbot.model.LogoutRequest
import com.codixly.docbot.model.LogoutResponse
import com.codixly.docbot.network.ApiClient
import com.codixly.docbot.network.ApiService
import com.codixly.docbot.utils.TextDrawable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.instance

//        binding.report.setOnClickListener {
//            val intent = Intent(requireContext(), PatientReportsActivity::class.java)
//            startActivity(intent)
//        }
        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)
        val token = sharedPref.getString("token", null)

        if (!customerId.isNullOrEmpty() && !token.isNullOrEmpty()) {
            fetchCustomerData(customerId, token)
        }

        binding.patitnesIcon.setOnClickListener {
            navigateToPatientScreen()
        }

        binding.faqsIcon.setOnClickListener {
            navigateToFaqScreen()
        }
        binding.optionIcon.setOnClickListener {
            navigateToMyAccountActivityScreen()
        }

        setupLogoutButton()
        setupDeleteAccountButton()

    }

    private fun fetchCustomerData(customerId: String, token: String) {
        val request = CustomerDataRequest(customerId, token)

        apiService.getCustomerData(request).enqueue(object : Callback<CustomerDataResponse> {
            override fun onResponse(
                call: Call<CustomerDataResponse>,
                response: Response<CustomerDataResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val customerResponse = response.body()!!
                    if (customerResponse.status && customerResponse.customer_data != null) {
                        val customer = customerResponse.customer_data

                        val formattedName = customer.name.replaceFirstChar { it.uppercaseChar() }
                        binding.userName.text = formattedName

                        if (customer.customer_profile.isNullOrEmpty()) {
                            val firstLetter = customer.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                            val drawable = TextDrawable(firstLetter, requireContext())
                            binding.profileImage.setImageDrawable(drawable)

                        } else {
                            Glide.with(requireContext())
                                .load(customer.customer_profile)
                                .placeholder(R.drawable.doctor)
                                .into(binding.profileImage)
                        }

                        // You can store other fields if needed
                    } else {
                        Toast.makeText(requireContext(), customerResponse.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CustomerDataResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToPatientScreen() {
        findNavController().navigate(R.id.patientsFragment)
    }
    private fun navigateToFaqScreen() {
        val intent = Intent(requireContext(), FaqActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMyAccountActivityScreen() {
        val intent = Intent(requireContext(), MyAccountActivity::class.java)
        startActivity(intent)
    }

    private fun setupLogoutButton() {
        binding.btnlogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupDeleteAccountButton() {
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_logout_confirmation, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Transparent background to show rounded corners properly
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            val window = dialog.window
            if (window != null) {
                val params = window.attributes
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.horizontalMargin = 0.05f // 5% margin on left & right
                window.attributes = params
            }
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun showDeleteAccountDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delect_account_confirmation, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Transparent background to show rounded corners properly
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            val window = dialog.window
            if (window != null) {
                val params = window.attributes
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.horizontalMargin = 0.05f // 5% margin on left & right
                window.attributes = params
            }
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            dialog.dismiss()
            performDelectAccount()
        }

        dialog.show()
    }

    private fun performLogout() {
        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)

        if (customerId != null) {
            logoutUser(customerId)
        } else {
            clearUserData()
            navigateToSplashScreen()
        }
    }

    private fun performDelectAccount() {
        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val customerId = sharedPref.getString("customer_unique_id", null)
        val token = sharedPref.getString("token", null)

        if (customerId != null) {
            if (!customerId.isNullOrEmpty() && !token.isNullOrEmpty()) {
                deleteAccountUser(customerId, token)
            } else {
                Toast.makeText(requireContext(), "User info missing", Toast.LENGTH_SHORT).show()
            }
        } else {
            clearUserData()
            navigateToSplashScreen()
        }
    }

    private fun logoutUser(customerUniqueId: String) {
        val logoutRequest = LogoutRequest(customerUniqueId)

        apiService.logout(logoutRequest).enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()

                    if (res.status) {
                        clearUserData()
                        navigateToSplashScreen()
                    }
                    Log.d("ProfileFragment", "Logout response: ${response.body()?.message}")
                } else {
                    Log.e("ProfileFragment", "Logout API failed: ${response.code()}")
                }
                clearUserData()
                navigateToSplashScreen()
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                Log.e("ProfileFragment", "Logout API failed", t)
                clearUserData()
                navigateToSplashScreen()
            }
        })
    }


    private fun deleteAccountUser(customerId: String, token: String) {
        val request = DeleteAccountRequest(customerId, token)

        ApiClient.instance.deleteAccount(request).enqueue(object : Callback<DeleteAccountResponse> {
            override fun onResponse(
                call: Call<DeleteAccountResponse>,
                response: Response<DeleteAccountResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()

                    if (res.status) {
                        clearUserData()
                        navigateToSplashScreen()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteAccountResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API Failed: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun clearUserData() {
        val sharedPref = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }

    private fun navigateToSplashScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(requireContext(), SplashScreenActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }, 1000) // delay can be adjusted
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
