package com.example.taxi.ui.home.tarif

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.tarif.Mode
import com.example.taxi.utils.convertToCyrillic
import kotlinx.coroutines.launch

class ModeAdapter(
    private val list: List<Mode>,
    private val modeToggleInterface: ModeToggleInterface,
    private val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<ModeAdapter.ModeViewHolder>() {

    inner class ModeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleName: TextView = itemView.findViewById(R.id.textViewp)
        val switchEnabled: SwitchCompat = itemView.findViewById(R.id.switchTarif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarif, parent, false)
        return ModeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ModeViewHolder, position: Int) {

        with(holder) {
            titleName.convertToCyrillic(list[position].name)
            switchEnabled.isChecked = list[position].value == "1"
//            price.text = PhoneNumberUtil.formatMoneyNumberPlate(list[position].cost)
//            switchEnabled.setOnCheckedChangeListener { _, _ ->
//                setSwitch(
//                    position = position,
//                    switchEnabled = switchEnabled,
//                    context = holder.itemView.context,
//                    modeToggleInterface = modeToggleInterface // Replace with your ModeToggleInterface object
//                )
//            }


//            itemView.setOnClickListener {
//                switchEnabled.isChecked = !switchEnabled.isChecked
////                setSwitch(position = position, switchEnabled = switchEnabled)
//            }
        }
    }

    private fun setSwitch(
        position: Int,
        switchEnabled: SwitchCompat,
        context: Context,
        modeToggleInterface: ModeToggleInterface
    ) {
        val tarifStatus = if (switchEnabled.isChecked) {
            context.getString(R.string.tarif_yoqildi)
        } else {
            context.getString(R.string.tarif_nofaol)
        }

        val message = "${list[position].name} $tarifStatus"
        val title = context.getString(R.string.tarif_rejim)

        lifecycleOwner.lifecycleScope.launch {
            modeToggleInterface.toggle(
                id = list[position].id,
                title = title,
                message = message,
                color = switchEnabled.isChecked
            )
        }
    }


}