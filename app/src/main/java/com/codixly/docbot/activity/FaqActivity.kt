package com.codixly.docbot.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.codixly.docbot.adapter.FaqAdapter
import com.codixly.docbot.databinding.ActivityDeviceInfoScreenBinding
import com.codixly.docbot.databinding.ActivityFaqBinding
import com.codixly.docbot.model.FaqModel
import com.codixly.docbot.model.FaqResponse
import com.codixly.docbot.network.ApiClient
import com.codixly.docbot.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaqBinding
    private lateinit var apiService: ApiService
    private lateinit var faqAdapter: FaqAdapter
    private var faqList: List<FaqModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Apply insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        apiService = ApiClient.instance

        setupRecyclerView()
        fetchFaqs()

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.faqRecyclerView.layoutManager = LinearLayoutManager(this)
        faqAdapter = FaqAdapter(faqList)
        binding.faqRecyclerView.adapter = faqAdapter
    }

    private fun fetchFaqs() {
        apiService.getFaqs().enqueue(object : Callback<FaqResponse> {
            override fun onResponse(call: Call<FaqResponse>, response: Response<FaqResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    val faqItems = body.faq.orEmpty()
//                    if (body.status && faqItems.isNotEmpty()) {
                        faqList = faqItems
                        Log.d("FaqResponse", faqList.toString())
                        faqAdapter.updateFaqList(faqList)
//                    } else {
//                        Toast.makeText(this@FaqActivity, body.message ?: "No FAQs found", Toast.LENGTH_SHORT).show()
//                    }
                } else {
                    Toast.makeText(this@FaqActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FaqResponse>, t: Throwable) {
                Toast.makeText(this@FaqActivity, "Failed: ${t.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
