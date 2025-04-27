package com.example.taxi.ui.home.order

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.order.Address
import com.example.taxi.domain.model.order.OrderData
import com.example.taxi.domain.model.order.updateTextView
import com.example.taxi.utils.setAddress
import com.example.taxi.utils.setPriceCost
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton


class MyBottomSheet(
    context: Context,
    theme: Int,
    private val orderData: OrderData<Address>,
    private val distance: String,
    private val bottomSheetInterface: BottomSheetInterface
) : BottomSheetDialog(context, theme) {


    private val typeTextView by lazy { findViewById<AppCompatTextView>(R.id.textView_type_dialog) }
    private val priceTextView by lazy { findViewById<TextView>(R.id.priceTextView_dialog) }
    private val addressFromTextView by lazy { findViewById<TextView>(R.id.addressFromTextView_dialog) }
    private val addressToTextView by lazy { findViewById<TextView>(R.id.addressToTextView_dialog) }
    private val distanceTextView by lazy { findViewById<TextView>(R.id.distanceTextView_dialog) }
    private val passengerCommentTextView by lazy { findViewById<TextView>(R.id.comment_textView) }
    private val serviceRecyclerView by lazy { findViewById<RecyclerView>(R.id.service_recyclerView_dialog) }
    private val commentTextView by lazy { findViewById<TextView>(R.id.comment_textView) }

    override fun setContentView(view: View) {
        val bottomView =
            LayoutInflater.from(context).inflate(R.layout.bottom_sheet_order_accepted, null, false)
        val containerView = bottomView.findViewById<LinearLayout>(R.id.bottomsheet)
        containerView.addView(view)
        super.setContentView(bottomView)

        priceTextView?.setPriceCost(orderData.start_cost)
        addressToTextView?.setAddress(orderData.address.to)
        addressFromTextView?.setAddress(orderData.address.from)
        orderData.comment?.let { commentTextView?.setAddress(it) }

        orderData.type?.let { typeTextView?.let { it1 -> updateTextView(it, it1) } }
//        addressTextView?.text = orderData.address.from
        distanceTextView?.text = distance
        passengerCommentTextView?.text = orderData.comment
        passengerCommentTextView?.movementMethod = ScrollingMovementMethod()
        serviceRecyclerView?.adapter = ServiceOrderAdapter(orderData.services)


        findViewById<ImageView>(R.id.dismiss_dialog)?.setOnClickListener {
            dismiss()
        }
        findViewById<MaterialButton>(R.id.accept_order)?.setOnClickListener {
            bottomSheetInterface.acceptOrder(
                id = orderData.id,
                latitude1 = orderData.latitude1,
                longitude1 = orderData.longitude1,
                latitude2 = orderData.latitude2,
                longitude2 = orderData.longitude2
            )
            dismiss()
        }

    }


}