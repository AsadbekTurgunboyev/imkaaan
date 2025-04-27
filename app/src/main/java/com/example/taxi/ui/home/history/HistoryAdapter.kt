package com.example.taxi.ui.home.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.databinding.ItemHistoryBinding
import com.example.taxi.domain.model.history.*
import com.example.taxi.domain.model.order.TypeEnum
import com.example.taxi.utils.ConversionUtil
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.PhoneNumberUtil
import com.example.taxi.utils.convertToCyrillic
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class HistoryAdapter(val list: List<Ride<RideType, RideAddress, RideUser, RideStatus, RideCreatedAt>>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val historyTimeText = binding.historyTimeTextView

        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.N)
        fun bind(data: Ride<RideType, RideAddress, RideUser, RideStatus, RideCreatedAt>) {
            binding.addressTextView.convertToCyrillic(data.address.from)
            binding.addressToTextViewHistory.convertToCyrillic(data.address.to)
            updateTextView(data.type, binding.textViewType)
            updateStatusTextView(data.status, binding.rideStatus)

            itemView.setOnClickListener {
                DialogUtils.showDetailHistoryDialog(context = itemView.context, data = data).show()
            }
            binding.priceTextView.text =
                PhoneNumberUtil.formatMoneyNumberPlate(data.cost.toString())
            binding.orderTimeTextView.text = getOrderTime(data.created_at.datetime)
            binding.distanceTextView.text =
                "${ConversionUtil.getDistanceWithKm(data.distance.toDouble())} ${
                    itemView.context.getString(R.string.km)
                }"
        }
    }

    private fun updateStatusTextView(status: RideStatus, rideStatus: TextView) {
        val color = if (status.number == 5) "#6618A802" else "#66F54336"

        rideStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
        rideStatus.convertToCyrillic(status.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {

        val currentMonth =
            getMonthName(list[position].created_at.timestamp.toLong(), holder.itemView.context)
        val previousMonth = if (position != 0) getMonthName(
            list[position - 1].created_at.timestamp.toLong(),
            holder.itemView.context
        ) else null

        with(holder.historyTimeText) {
            if (position == 0 || currentMonth != previousMonth) {
                visibility = View.VISIBLE
                text = currentMonth
            } else {
                visibility = View.GONE
            }
        }
        holder.bind(list[position])
    }


    fun updateTextView(type: RideType, textView: AppCompatTextView) {
        val typeEnum = TypeEnum.values().find { it.ordinal + 1 == type.number }
        typeEnum?.let {
            textView.convertToCyrillic(type.name)
            textView.setTextColor(Color.parseColor(it.textColor))
            textView.compoundDrawableTintList =
                ColorStateList.valueOf(Color.parseColor(it.textColor))
            textView.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(it.backgroundColor))
        }
    }

    fun getOrderTime(time: String): String {
        val t = time.split(" ")

        return "${t[0]} ${t[1]} ${t[3]}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMonthName(timestampMillis: Long, context: Context): String {


        val dateTime = LocalDateTime.ofEpochSecond(timestampMillis, 0, ZoneOffset.UTC)
        val month = dateTime.monthValue // This will return the month as an integer (0-11)
        val monthNames = context.resources.getStringArray(R.array.month_names)
        Log.d("vaqt", "getMonthName: $month")
        return monthNames[month - 1]
    }
}