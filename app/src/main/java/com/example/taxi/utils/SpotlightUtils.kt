package com.example.taxi.utils

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.FragmentActivity
import com.example.taxi.R
import com.example.taxi.databinding.FragmentDashboardBinding
import com.example.taxi.domain.preference.UserPreferenceManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal

object SpotlightUtils {

    fun showSpotlight(activity: FragmentActivity, viewBinding: FragmentDashboardBinding, userPreferenceManager: UserPreferenceManager) {
        val fullText = activity.getString(R.string.show_message_for_status)
        val targetText = activity.getString(R.string.see)
        val spannableString = SpannableString(fullText)
        val startIndex = fullText.indexOf(targetText, ignoreCase = true)
        val endIndex = startIndex + targetText.length

        val colorSpan = ForegroundColorSpan(Color.GREEN)
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(
                viewBinding.navigationView.getHeaderView(0).findViewById(R.id.active_status_shape)
            )
            .setPrimaryText(activity.getString(R.string.your_status))
            .setSecondaryText(spannableString)
            .setPromptBackground(RectanglePromptBackground())
            .setBackButtonDismissEnabled(true)
            .setPromptFocal(RectanglePromptFocal())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    userPreferenceManager.saveSeenStatus(true)
                }
            }
            .show()
    }

    fun showIntroTarget(activity: FragmentActivity, viewBinding: FragmentDashboardBinding,userPreferenceManager: UserPreferenceManager) {

        TapTargetSequence(activity)
            .targets(
                TapTarget.forView(
                    viewBinding.callBtn,
                    activity.getString(R.string.title_intro_phone),
                    activity.getString(R.string.message_intro_phone)
                )
                    .dimColor(android.R.color.black)
                    .outerCircleColor(R.color.blue)
                    .targetCircleColor(android.R.color.holo_blue_light)
                    .transparentTarget(true)
                    .cancelable(true)
                    .textColor(android.R.color.black),
                TapTarget.forView(
                    viewBinding.socketIsConnected,
                    activity.getString(R.string.title_intro_socket),
                    activity.getString(R.string.message_intro_socket)
                )
                    .dimColor(android.R.color.black)
                    .outerCircleColor(R.color.blue)
                    .targetCircleColor(android.R.color.holo_blue_light)
                    .transparentTarget(true)
                    .textColor(android.R.color.black),
            )
            .listener(object : TapTargetSequence.Listener {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                override fun onSequenceFinish() {
                    userPreferenceManager.saveSeenIntro(true)

                }

                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    // Perform action for the current target
                }

                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    userPreferenceManager.saveSeenIntro(true)
                }
            }).start()
    }
}