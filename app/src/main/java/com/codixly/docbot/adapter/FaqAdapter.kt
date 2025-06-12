package com.codixly.docbot.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codixly.docbot.R
import com.codixly.docbot.model.FaqModel
import com.google.android.material.card.MaterialCardView

class FaqAdapter(private var faqList: List<FaqModel>) :
    RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.faq_item, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val item = faqList[position]

        holder.title.text = item.title ?: "No Title"
        holder.para.text = item.para ?: "No Content"

        val isExpanded = position == expandedPosition
        holder.para.visibility = if (isExpanded) View.VISIBLE else View.GONE

        // Background color logic
        holder.faqItem.setBackgroundColor(
            holder.itemView.context.getColor(
                if (isExpanded) R.color.primary2 else android.R.color.white
            )
        )

        // Border logic
        holder.bottomBorder.visibility = if (isExpanded) View.GONE else View.VISIBLE

        // Corner radius logic
        val radiusInDp = if (isExpanded) 24f else 0f
        holder.cardFaq.radius = radiusInDp

        holder.faqItem.setOnClickListener {
            val prevExpanded = expandedPosition
            expandedPosition = if (isExpanded) RecyclerView.NO_POSITION else position
            notifyItemChanged(prevExpanded)
            notifyItemChanged(position)
        }
    }


    override fun getItemCount(): Int = faqList.size

    fun updateFaqList(newList: List<FaqModel>) {
        faqList = newList
        expandedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvFaqTitle)
        val para: TextView = itemView.findViewById(R.id.tvFaqPara)
        val faqItem: LinearLayout = itemView.findViewById(R.id.faqItem)
        val bottomBorder: View = itemView.findViewById(R.id.bottomBorder)
        val cardFaq: MaterialCardView = itemView.findViewById(R.id.cardFaq)
    }

}
