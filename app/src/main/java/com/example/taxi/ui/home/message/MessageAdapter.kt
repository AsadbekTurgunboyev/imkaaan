package com.example.taxi.ui.home.message

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.message.MessageItem
import com.example.taxi.utils.convertToCyrillic
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(val list: List<MessageItem>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val messageContent: TextView = itemView.findViewById(R.id.content_message)
        private val timeMessage: TextView = itemView.findViewById(R.id.time_textView)
        private val dayMessage: TextView = itemView.findViewById(R.id.date_textView)

        fun bind(model: MessageItem){
            messageContent.convertToCyrillic(model.message)
            timeMessage.text = getTimeFromString(model.time)
            dayMessage.text = convertDateTimeFormat(model.time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message,parent,false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
       holder.bind(list[position])
    }

    fun getTimeFromString(dateStr: String): String {
        val inputFormat = SimpleDateFormat("MMM dd, yyyy'y' HH:mm:ss", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

        val date = inputFormat.parse(dateStr)
        return outputFormat.format(date ?: return "")
    }

    fun convertDateTimeFormat(dateTimeStr: String): String {
        val inputFormat = SimpleDateFormat("MMM dd, yyyy'y' HH:mm:ss", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("uz"))

        return try {
            val date = inputFormat.parse(dateTimeStr)
            outputFormat.format(date ?: return "")
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
}