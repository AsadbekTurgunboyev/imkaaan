package com.example.taxi.ui.home.transfer.transferhistory

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.transfer.DataItem
import com.example.taxi.domain.model.transfer.HistoryCreatedAt
import com.example.taxi.domain.model.transfer.HistoryType
import com.example.taxi.utils.ConversionUtil
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.PhoneNumberUtil
import com.example.taxi.utils.convertToCyrillic


const val IN_COME = 1
const val OUT_COME = 2


class TransferHistoryAdapter(private val list: List<DataItem<HistoryType, HistoryCreatedAt>>) :
    RecyclerView.Adapter<TransferHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val historyType: AppCompatTextView = itemView.findViewById(R.id.history_type)
        private val enteredMoneyText: TextView = itemView.findViewById(R.id.txt_entered_sum)
        private val actionText: TextView = itemView.findViewById(R.id.txt_action)
        private val timeText: TextView = itemView.findViewById(R.id.txt_date_time)

        @SuppressLint("SetTextI18n")
        fun bind(data: DataItem<HistoryType, HistoryCreatedAt>) {

//            actionText.movementMethod = ScrollingMovementMethod()
            actionText.isSelected = true;

            if (data.type.number == IN_COME) {
                enteredMoneyText.setTextColor(itemView.context.getColor(R.color.green))
                enteredMoneyText.text =
                    "+${PhoneNumberUtil.formatMoneyNumberPlate(data.value.toString())}"

            } else if (data.type.number == OUT_COME) {
                enteredMoneyText.setTextColor(itemView.context.getColor(R.color.red))
                enteredMoneyText.text =
                    "-${PhoneNumberUtil.formatMoneyNumberPlate(data.value.toString())}"

            }
            updateTextView(data.type, historyType)
            actionText.convertToCyrillic(data.reason)
            timeText.text = data.createdAt.datetime

            itemView.setOnClickListener {
                DialogUtils.showDetailTransferHistoryDialog(
                    data = data,
                    context = itemView.context
                ).show()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_added, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {

        holder.bind(list[position])
    }


    @SuppressLint("UseCompatTextViewDrawableApis")
    fun updateTextView(
        type: HistoryType,
        textView: AppCompatTextView
    ) {
        val typeEnum = TypeEnumHistory.values().find { it.ordinal + 1 == type.number }
        typeEnum?.let {
            textView.text = ConversionUtil.convertToCyrillic(type.name)
            textView.setTextColor(Color.parseColor(it.textColor))
            textView.compoundDrawableTintList =
                ColorStateList.valueOf(Color.parseColor(it.textColor))
            textView.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(it.backgroundColor))
        }
    }


    enum class TypeEnumHistory(val textColor: String, val backgroundColor: String) {
        TYPE_INCOME("#18A801", "#E3FFDE"),
        TYPE_OUTCOME("#F64242", "#FFEDED"),

    }
}