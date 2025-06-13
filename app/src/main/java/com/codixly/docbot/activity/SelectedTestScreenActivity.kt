//package com.codixly.docbot.activity
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.codixly.docbot.R
//
//class SelectedTestScreenActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_selected_test_screen)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}
package com.codixly.docbot.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codixly.docbot.adapter.SelectedTestAdapter
import com.codixly.docbot.databinding.ActivitySelectedTestScreenBinding

class SelectedTestScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectedTestScreenBinding
    private lateinit var adapter: SelectedTestAdapter
    private val testList = mutableListOf(
        "Height", "Weight", "Diabetes", "Fever",
        "Height", "Weight", "Height", "Weight", "Height", "Weight"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ViewBinding setup
        binding = ActivitySelectedTestScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView Adapter setup
        adapter = SelectedTestAdapter(testList) { position ->
            testList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, testList.size)
        }

        binding.testRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.testRecyclerView.adapter = adapter

        // Back arrow handling
        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Continue button click
        binding.continueButton.setOnClickListener {
            // Add your logic for continuing
        }
    }
}
