package com.codixly.docbot.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codixly.docbot.R
import com.codixly.docbot.activity.NotificationActivity
import com.codixly.docbot.databinding.FragmentHomeBinding
import com.codixly.docbot.databinding.FragmentPatientsBinding


class PatientsFragment : Fragment() {
    private lateinit var binding: FragmentPatientsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPatientsBinding.inflate(inflater, container, false)
        // 🔔 Set click listener on notification button/view
        binding.notificationButton.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }


}