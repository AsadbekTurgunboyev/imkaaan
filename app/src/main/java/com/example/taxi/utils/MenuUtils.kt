package com.example.taxi.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.taxi.R
import com.example.taxi.domain.preference.UserPreferenceManager

object MenuUtils {
    fun getActionViewLanguage(
        context: Context,
        preferenceManager: UserPreferenceManager,
    ): View {
        val actionView = LayoutInflater.from(context).inflate(R.layout.item_menu_language, null)
        val language: TextView = actionView.findViewById(R.id.which_language)
        language.text = when (preferenceManager.getLanguage()) {
            UserPreferenceManager.Language.RUSSIAN -> "Русский язык"
            UserPreferenceManager.Language.UZBEK -> "O'zbek tili"
            UserPreferenceManager.Language.KRILL -> "Узбек тили"
        }

        return actionView
    }

    fun getActionViewNext(
        context: Context,
    ): View {
        return LayoutInflater.from(context).inflate(R.layout.item_action_next, null)
    }

    fun getActionViewMessage(
        context: Context,
        preferenceManager: UserPreferenceManager,
    ): View {
        val actionView = LayoutInflater.from(context).inflate(R.layout.item_menu_message, null)

        val badge: TextView = actionView.findViewById(R.id.message_badge)
        val next: ImageView = actionView.findViewById(R.id.next_icon)
        if (preferenceManager.getMessageCount() > 0) {
            badge.visibility = View.VISIBLE
            badge.text = preferenceManager.getMessageCount().toString()
            next.visibility = View.GONE
        } else {
            badge.visibility = View.GONE
            next.visibility = View.VISIBLE
        }


        return actionView
    }


}


class MenuClass(val context: Context, val preferenceManager: UserPreferenceManager) {
    private val a = if (preferenceManager.getAdvertisingStatus() == 1) listOf(
        Triple(
            661, "Group 8", listOf(
                Triple(239, context.getString(R.string.foto_nazorat), R.drawable.ic_camera)
            )
        )
    ) else emptyList()

    private val b = listOf(
        Triple(
            237, "Group 5", listOf(
                Triple(990, context.getString(R.string.sign_out), R.drawable.baseline_logout_24)
            )
        )
    )

    private val c = listOf(
        Triple(
            234, "Group 2", listOf(
                Triple(987, context.getString(R.string.about_us), R.drawable.ic_info)
            )
        )
    )


    private val groups: List<Triple<Int, String, List<Triple<Int, String, Int>>>> = listOf(
        Triple(
            123, "Group 1", listOf(
                Triple(456, context.getString(R.string.my_profile), R.drawable.ic_profile)
            )
        ),
        Triple(
            333, "Group 7", listOf(
                Triple(233, context.getString(R.string.taximeter), R.drawable.ic_taxometr)
            )
        ),
        Triple(
            334, "Group 8", listOf(
                Triple(234, context.getString(R.string.message), R.drawable.ic_message)
            )
        ),

//        Triple(
//            661, "Group 8", listOf(
//                Triple(239, context.getString(R.string.foto_nazorat), R.drawable.ic_photo_control)
//            )
//        ),

//        Triple(
//            235, "Group 3", listOf(
//                Triple(988, context.getString(R.string.faq), R.drawable.ic_faq)
//            )
//        ),
//        Triple(
//            236, "Group 4", listOf(
//                Triple(989, context.getString(R.string.baho_bering), R.drawable.ic_star)
//            )
//        ),
//        Triple(238,"Group 6", listOf(
//            Triple(985,context.getString(R.string.qr),R.drawable.ic_qr_code)
//        )),
//        Triple(
//            237,"Group 5", listOf(
//                Triple(990,context.getString(R.string.sign_out),R.drawable.baseline_logout_24)
//            )
//        )


    ) + a  + b

    private val itemActionViews: Map<Int, View> = mapOf(
        456 to MenuUtils.getActionViewNext(context),
        233 to MenuUtils.getActionViewNext(context),
        234 to MenuUtils.getActionViewMessage(context, preferenceManager),
        239 to MenuUtils.getActionViewNext(context),
        987 to MenuUtils.getActionViewNext(context),
        988 to MenuUtils.getActionViewNext(context),
        989 to MenuUtils.getActionViewNext(context),
        985 to MenuUtils.getActionViewNext(context)
    )

    fun getMenu(): List<Triple<Int, String, List<Triple<Int, String, Int>>>> {
        return groups
    }

    fun getItemActionView(): Map<Int, View> {
        return itemActionViews
    }
}

