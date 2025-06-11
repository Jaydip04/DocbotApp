//package com.codixly.docbot.fragment
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.codixly.docbot.R
//
//class TestFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_test, container, false)
//    }
//
//
//}
package com.codixly.docbot.fragment

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
import com.codixly.docbot.activity.GridSpacingItemDecoration

class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
