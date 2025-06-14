package com.codixly.docbot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.codixly.docbot.databinding.ItemReportBinding
import com.codixly.docbot.model.ReportModel

class ReportAdapter(
    private val reportList: List<ReportModel>
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]
        with(holder.binding) {
            textView.text = report.title
            textDate.text = report.date
            textTime.text = report.time
            image.setImageResource(report.imageResId)

            // Image Click
            image.setOnClickListener {
                Toast.makeText(root.context, "Image clicked: ${report.title}", Toast.LENGTH_SHORT).show()
                // Handle image view logic (e.g., open full screen)
            }

            // Download Click
            download.setOnClickListener {
                Toast.makeText(root.context, "Download clicked: ${report.title}", Toast.LENGTH_SHORT).show()
                // Handle download logic here
            }

            // Share Click
            share.setOnClickListener {
                Toast.makeText(root.context, "Share clicked: ${report.title}", Toast.LENGTH_SHORT).show()
                // Handle share logic here
            }
        }
    }

    override fun getItemCount(): Int = reportList.size
}
