package com.example.taxi.utils

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.taxi.R
import com.example.taxi.domain.preference.UserPreferenceManager
import com.google.android.material.card.MaterialCardView
import org.koin.core.context.GlobalContext

data class ButtonInfo(
    val button: MaterialCardView,
    val cardView: MaterialCardView,
    val checkView: ImageView,
    val activeColor: Int,
    val inactiveColor: Int
)

fun setButtonState(buttonInfo: ButtonInfo, isActive: Boolean) {

    val colorStateList = ColorStateList.valueOf(
        ContextCompat.getColor(
            buttonInfo.button.context,
            if (isActive) buttonInfo.activeColor else buttonInfo.inactiveColor
        )
    )

    val bgColorStateList = ColorStateList.valueOf(
        ContextCompat.getColor(
            buttonInfo.button.context,
            if (isActive) R.color.black else buttonInfo.inactiveColor
        )
    )
    buttonInfo.button.post {
        buttonInfo.button.backgroundTintList = colorStateList
    }
    buttonInfo.checkView.post {
        buttonInfo.cardView.backgroundTintList = bgColorStateList

    }
    buttonInfo.checkView.post {
        buttonInfo.checkView.visibility = if (isActive) View.VISIBLE else View.INVISIBLE
    }
//    buttonInfo.checkView.visibility = if (isActive) ImageView.VISIBLE else ImageView.INVISIBLE

}

object ButtonUtils{

    private val userPreferenceManager by lazy {
        GlobalContext.get().get<UserPreferenceManager>()
    }

    fun callToDispatcher(context: Context){
        val phone = userPreferenceManager.getPhoneNumber()
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        context.startActivity(intent)
    }
    fun callToDispatchWhenBlock(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        context.startActivity(intent)
    }

    fun callToPassenger(context: Context){
        val phone = userPreferenceManager.getPassengerPhone()
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        context.startActivity(intent)
    }
}



