package com.example.taxi.ui.home.transfer.transferhistory

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.google.android.material.card.MaterialCardView

class CountPageAdapter(
    private val count: Int,
    private val is_active: Int,
    val pageInterface: SelectPageInterface
) :
    RecyclerView.Adapter<CountPageAdapter.ViewHolderCount>() {

    inner class ViewHolderCount(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val count: TextView = itemView.findViewById(R.id.count_number)
        private val badge: MaterialCardView = itemView.findViewById(R.id.count_badge)

        fun bind(position: Int) {
            count.text = position.toString()
            if (position == is_active) {
                badge.backgroundTintList =
                    ColorStateList.valueOf(itemView.context.getColor(R.color.blue))
                count.setTextColor(itemView.context.getColor(R.color.white))

            } else {
                badge.backgroundTintList =
                    ColorStateList.valueOf(itemView.context.getColor(R.color.white))
                count.setTextColor(itemView.context.getColor(R.color.only_white))
            }

            itemView.setOnClickListener {
                if (position != is_active) {
                    pageInterface.setPageCount(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCount {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_count_page, parent, false)

        return ViewHolderCount(view)
    }

    override fun getItemCount(): Int {
        return count
    }

    override fun onBindViewHolder(holder: ViewHolderCount, position: Int) {
        holder.bind(position + 1)
    }


}