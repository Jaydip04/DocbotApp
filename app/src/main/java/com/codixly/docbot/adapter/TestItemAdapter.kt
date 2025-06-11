package com.codixly.docbot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codixly.docbot.databinding.TestItemItemBinding
import com.codixly.docbot.model.TestItem

class TestItemAdapter(
    private val itemList: List<TestItem>
) : RecyclerView.Adapter<TestItemAdapter.TestItemViewHolder>() {

    inner class TestItemViewHolder(val binding: TestItemItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestItemViewHolder {
        val binding = TestItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.binding.iconImageView.setImageResource(item.iconResId)
        holder.binding.labelTextView.text = item.name
    }

    override fun getItemCount(): Int = itemList.size
}
