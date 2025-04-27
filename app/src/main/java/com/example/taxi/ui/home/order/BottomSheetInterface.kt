package com.example.taxi.ui.home.order

import com.example.taxi.domain.model.order.Address
import com.example.taxi.domain.model.order.OrderData

interface BottomSheetInterface {
    fun showBottom(orderData: OrderData<Address>, distance: String)

    fun acceptOrder(id: Int, latitude1: String, longitude1: String,latitude2: String? = null,longitude2: String?= null)
}