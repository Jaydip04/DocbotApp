//package com.codixly.docbot.activity
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.codixly.docbot.R
//
//class PatientReportResultActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_patient_report_result)
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.codixly.docbot.R
import com.codixly.docbot.adapter.ReportAdapter
import com.codixly.docbot.databinding.ActivityPatientReportResultBinding
import com.codixly.docbot.model.ReportModel

class PatientReportResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientReportResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPatientReportResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val reportList = listOf(
            ReportModel("Blood Report", "06/06/25", "5:00 PM", R.drawable.ic_patients),
            ReportModel("X-Ray Result", "07/06/25", "6:30 PM", R.drawable.ic_patients),
            ReportModel("ECG", "08/06/25", "7:45 PM", R.drawable.ic_patients)
        )

        val adapter = ReportAdapter(reportList)
        binding.reportRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportRecyclerView.adapter = adapter
    }
}
