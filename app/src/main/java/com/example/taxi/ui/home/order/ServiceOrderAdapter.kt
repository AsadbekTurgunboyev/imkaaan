package com.example.taxi.ui.home.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.order.Service
import com.example.taxi.ui.home.service.ModeDrawableMapper
import com.example.taxi.utils.PhoneNumberUtil
import com.example.taxi.utils.convertToCyrillic

class ServiceOrderAdapter(val list: List<Service>) : RecyclerView.Adapter<ServiceOrderAdapter.ServiceOrderViewHolder>() {

    inner class ServiceOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val serviceTitle : TextView = itemView.findViewById(R.id.service_title_txt)
        val servicePrice: TextView = itemView.findViewById(R.id.textView8)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServiceOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_order, parent, false)
        return ServiceOrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ServiceOrderViewHolder, position: Int) {
       with(holder){

           serviceTitle.isSelected = true;
           servicePrice.text = PhoneNumberUtil.formatMoneyNumberPlate(list[position].cost.toString())
           list[position].name?.let { serviceTitle.convertToCyrillic(it) }
       }
    }
}