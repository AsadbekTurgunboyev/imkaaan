package com.example.taxi.ui.home.order

import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.order.Address
import com.example.taxi.domain.model.order.OrderData
import com.example.taxi.domain.model.order.updateTextView
import com.example.taxi.utils.ConversionUtil.calculateDistance
import com.example.taxi.utils.ConversionUtil.calculateDistanceDouble
import com.example.taxi.utils.convertToCyrillic
import com.example.taxi.utils.setPriceCost

open class OrderAdapter(
    private val list: List<OrderData<Address>>,
    private val location: Location?,
    private val receiveItem: BottomSheetInterface,
    private val layoutItem: Int
) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressFromTextView: TextView = itemView.findViewById(R.id.addressFromTextView)
        private val addressToTextView: TextView = itemView.findViewById(R.id.addressToTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        private val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)
        private val typeTextView: AppCompatTextView = itemView.findViewById(R.id.textView_type)
        private val infoForPriceTv: TextView = itemView.findViewById(R.id.infoForPriceTv)
        private val layoutSecondDestination: LinearLayout? = itemView.findViewById(R.id.layout_second_destination)
        fun bind(order: OrderData<Address>) {

            Log.d("buyurtma", "bind: $order")
            addressFromTextView.convertToCyrillic(order.address.from)

            if (order.predict_cost != null && order.predict_distance != null) {
                infoForPriceTv.text = itemView.context.getString(R.string.taxminiy_narx)
                Log.d("buyurtma", "bind: ${order.predict_cost}")
                priceTextView.setPriceCost(order.predict_cost)

            } else {
                Log.d("buyurtma", "bind: ${order.predict_cost} va ${order.start_cost}")
                infoForPriceTv.text = itemView.context.getString(R.string.boshlang_ich_summa)
                priceTextView.setPriceCost(order.start_cost)

            }

            if (order.address.to.isEmpty() || order.address.to == "-") {
                layoutSecondDestination?.visibility = View.GONE
                addressToTextView.text = "-"
            } else {
                layoutSecondDestination?.visibility = View.VISIBLE
                addressToTextView.convertToCyrillic(order.address.to)
            }

//            orderTime.text = order.created_at.time.substring(0,5)

            val lat2 = location?.latitude
            val long2 = location?.longitude

            val distance = lat2?.let {
                long2?.let { it1 ->
                    calculateDistance(
                        lat1 = order.latitude1.toDouble(),
                        lat2 = it,
                        lon1 = order.longitude1.toDouble(),
                        lon2 = it1
                    )
                }
            }
            distanceTextView.text = distance


            order.type?.let { updateTextView(it, typeTextView) }

            itemView.setOnClickListener {
                distance?.let { it1 -> receiveItem.showBottom(order, distance = it1) }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(layoutItem, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = list[position]
        holder.bind(order)
    }

    fun sortWithKm(){
            if (list.isNotEmpty() && location != null) {
                val sortedList = list.sortedBy { order ->
                    val distance = calculateDistanceDouble(
                        lat1 = order.latitude1.toDouble(),
                        lat2 = location.latitude,
                        lon1 = order.longitude1.toDouble(),
                        lon2 = location.longitude
                    )
                    distance
                }
                // Saralangan ro'yxatni adapterga o'tkazish uchun yangi ro'yxat yaratish kerak.
                (list as MutableList).clear()
                (list as MutableList).addAll(sortedList)
                notifyDataSetChanged()
        }
    }
}