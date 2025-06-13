package com.codixly.docbot.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codixly.docbot.R
import com.codixly.docbot.activity.AuthenticateScreenActivity
import com.codixly.docbot.activity.DeviceInfoScreenActivity
import com.codixly.docbot.activity.NotificationActivity
import com.codixly.docbot.activity.RegistrationScreenActivity
import com.codixly.docbot.databinding.FragmentHomeBinding
import com.codixly.docbot.databinding.FragmentPatientsBinding


class PatientsFragment : Fragment() {
    private lateinit var binding: FragmentPatientsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPatientsBinding.inflate(inflater, container, false)
        binding.notification.setOnClickListener {
            navigateToNotificationActivity()
        }
        binding.monitoring.setOnClickListener {
            navigateToDeviceActivity()
        }

        binding.addPatients.setOnClickListener {
            navigateToPatientsActivity()
        }

        return binding.root
    }
    private fun navigateToPatientsActivity() {
//        val intent = Intent(requireContext(), RegistrationScreenActivity::class.java)
        val intent = Intent(requireContext(), AuthenticateScreenActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToDeviceActivity() {
        val intent = Intent(requireContext(), DeviceInfoScreenActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToNotificationActivity() {
        val intent = Intent(requireContext(), NotificationActivity::class.java)
        startActivity(intent)
    }

}