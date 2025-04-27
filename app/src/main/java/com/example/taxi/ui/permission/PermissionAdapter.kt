package com.example.taxi.ui.permission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.checkAccess.NotPermissionApkModel

class PermissionAdapter(val list: List<NotPermissionApkModel>) :
    RecyclerView.Adapter<PermissionAdapter.MYViewHolder>() {

    inner class MYViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.appNameFirst)
        val icon: ImageView = itemView.findViewById(R.id.appIconFirst)

        fun bind(model: NotPermissionApkModel) {
            name.text = model.name
            icon.setImageDrawable(model.icon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MYViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_not_permission_apk, parent, false)
        return MYViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MYViewHolder, position: Int) {
        holder.run {
            bind(list[position])
        }
    }
}