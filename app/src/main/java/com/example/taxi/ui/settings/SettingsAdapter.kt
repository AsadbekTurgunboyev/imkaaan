package com.example.taxi.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.settings.SettingsModel
import com.google.android.material.radiobutton.MaterialRadioButton

class SettingsAdapter(
    val list: List<SettingsModel>,
    private val selectedPackage: String,
    private val listener: (String, String) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private var selectedPosition = list.indexOfFirst { it.value == selectedPackage }

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.tv_name_settings)
        val chooseButton: MaterialRadioButton = itemView.findViewById(R.id.button_choose)

        fun bind(model: SettingsModel) {
            nameTv.text = model.name
            chooseButton.isChecked = adapterPosition == selectedPosition
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_choose_settings, parent, false)
        return SettingsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.chooseButton.isChecked = position == selectedPosition
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            listener(list[selectedPosition].value, list[selectedPosition].name)
        }
        holder.chooseButton.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition

            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            listener(list[selectedPosition].value,list[selectedPosition].name)
        }
    }
}