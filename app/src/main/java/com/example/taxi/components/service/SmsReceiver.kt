package com.example.taxi.components.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    val code = extractCode(message)

                    // Send local broadcast with the extracted code
                    val localIntent = Intent(ACTION_OTP_RECEIVED)
                    localIntent.putExtra(EXTRA_OTP_CODE, code)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
                }
            }
        }
    }

    private fun extractCode(message: String): String {
        val pattern = Pattern.compile("(?i)code:\\s*(\\d{4})")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) {
            matcher.group(1) ?: ""
        } else {
            ""
        }
    }

    companion object {
        const val ACTION_OTP_RECEIVED = "com.example.ACTION_OTP_RECEIVED"
        const val EXTRA_OTP_CODE = "com.example.EXTRA_OTP_CODE"
    }
}