package com.codixly.docbot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codixly.docbot.databinding.SelecteTestItemBinding

class SelectedTestAdapter(
    private val testList: List<String>,
    private val onRemoveClick: (position: Int) -> Unit
) : RecyclerView.Adapter<SelectedTestAdapter.TestViewHolder>() {

    inner class TestViewHolder(val binding: SelecteTestItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val binding = SelecteTestItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        val testName = testList[position]
        holder.binding.testNumber.text = (position + 1).toString()
        holder.binding.testName.text = testName

        // Handle Remove icon click
        holder.binding.removeIcon.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount(): Int = testList.size
}