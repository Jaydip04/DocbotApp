package com.codixly.docbot.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codixly.docbot.activity.NotificationActivity
import com.codixly.docbot.activity.DeviceInfoActivity
import com.codixly.docbot.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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

        return binding.root
    }


}