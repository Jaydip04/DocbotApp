package com.codixly.docbot.fragment

//<<<<<<< HEAD
//=======
import android.content.Context
//>>>>>>> e19a48088527dd6191bce803dafcd19d9d849359
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//<<<<<<< HEAD
import com.codixly.docbot.activity.NotificationActivity
import com.codixly.docbot.activity.DeviceInfoActivity
import com.codixly.docbot.databinding.FragmentHomeBinding
//=======
import android.widget.Toast
import com.bumptech.glide.Glide
import com.codixly.docbot.R
//import com.codixly.docbot.databinding.FragmentHomeBinding
import com.codixly.docbot.model.CustomerDataRequest
import com.codixly.docbot.model.CustomerDataResponse
import com.codixly.docbot.network.ApiClient
import com.codixly.docbot.network.ApiService
import com.codixly.docbot.utils.TextDrawableHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.navigation.fragment.findNavController
import com.codixly.docbot.activity.DeviceInfoScreenActivity
//import com.codixly.docbot.activity.NotificationActivity
//>>>>>>> e19a48088527dd6191bce803dafcd19d9d849359

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false)
//    }
//    private privatevar _binding: FragmentHomeBinding? = null
//    privatevarte val bindinginding get() = _binding!!
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
//<<<<<<< HEAD
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // 🔔 Set click listener on notification button/view
        binding.notification.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }
        binding.monitoring.setOnClickListener{
            val intent=Intent(requireContext(),DeviceInfoActivity::class.java)
            startActivity(intent)
        }

//=======
//    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
//>>>>>>> e19a48088527dd6191bce803dafcd19d9d849359
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
        binding.profileImage.setOnClickListener {
            navigateToProfileScreen()
        }

        binding.notification.setOnClickListener {
            navigateToNotificationActivity()
        }
        binding.monitoring.setOnClickListener {
            navigateToDeviceActivity()
        }
        binding.addPatientCard.setOnClickListener {
            navigateToPatientScreen()
        }
        binding.takeTestCard.setOnClickListener {
            navigateToTestScreen()
        }
    }

    private fun navigateToProfileScreen() {
        findNavController().navigate(R.id.profileFragment)
    }

    private fun navigateToPatientScreen() {
        findNavController().navigate(R.id.patientsFragment)
    }

    private fun navigateToTestScreen() {
        findNavController().navigate(R.id.testFragment)
    }

    private fun navigateToDeviceActivity() {
        val intent = Intent(requireContext(), DeviceInfoScreenActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToNotificationActivity() {
        val intent = Intent(requireContext(), NotificationActivity::class.java)
        startActivity(intent)
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
                            val drawable = TextDrawableHome(firstLetter, requireContext())
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

}