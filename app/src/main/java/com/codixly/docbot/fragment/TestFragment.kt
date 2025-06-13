package com.codixly.docbot.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.codixly.docbot.adapter.TestItemAdapter
import com.codixly.docbot.databinding.FragmentTestBinding
import com.codixly.docbot.model.TestItem
import com.codixly.docbot.R
import com.codixly.docbot.activity.DeviceInfoScreenActivity
import com.codixly.docbot.activity.GridSpacingItemDecoration
import com.codixly.docbot.activity.NotificationActivity

class TestFragment : Fragment() {

    private lateinit var binding: FragmentTestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notification.setOnClickListener {
            navigateToNotificationActivity()
        }
        binding.monitoring.setOnClickListener {
            navigateToDeviceActivity()
        }

        // Dummy data
        val dummyList = listOf(
            TestItem("Blood Group", R.drawable.ic_heart),
            TestItem("Thyroid", R.drawable.ic_heart),
            TestItem("Diabetes", R.drawable.ic_heart),
            TestItem("Kidney Test", R.drawable.ic_heart),
            TestItem("Liver Panel", R.drawable.ic_heart),
            TestItem("CBC", R.drawable.ic_heart),
            TestItem("Blood Group", R.drawable.ic_heart),
            TestItem("Thyroid", R.drawable.ic_heart),
            TestItem("Diabetes", R.drawable.ic_heart),
            TestItem("Kidney Test", R.drawable.ic_heart),
            TestItem("Liver Panel", R.drawable.ic_heart),
        )

        val adapter = TestItemAdapter(dummyList)
//
//        binding.testRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
//        binding.testRecyclerView.adapter = adapter
        val spanCount = 3 // Number of columns
        val spacing = 16  // Space in pixels (you can also use resources)
        val includeEdge = true

        binding.testRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.testRecyclerView.adapter = adapter
        binding.testRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))

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
