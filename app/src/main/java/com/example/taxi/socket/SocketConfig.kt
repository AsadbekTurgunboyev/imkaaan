package com.example.taxi.socket

object SocketConfig {
    const val ORDER_NEW = "order_new"
    const val ORDER_ONLY_FOR_YOU = "order_only_for_you"
    const val ORDER_ACCEPTED = "order_accepted"
    const val ORDER_CANCELLED = "order_cancelled"
    const val ORDER_UPDATE ="order_update"
    const val SEND_NOTIFICATION = "send_notification"
    const val SEND_NOTIFICATION_NEW_ORDER = "send_busy_driver_new_order"
    const val PAYMENT_RECEIVED = "bill_via_bonus"
    const val RECONNECT_DELAY_MS = 3000L

}