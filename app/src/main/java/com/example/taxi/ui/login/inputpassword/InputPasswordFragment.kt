package com.example.taxi.ui.login.inputpassword

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.example.taxi.R
import com.example.taxi.components.service.SmsReceiver.Companion.ACTION_OTP_RECEIVED
import com.example.taxi.components.service.SmsReceiver.Companion.EXTRA_OTP_CODE
import com.example.taxi.databinding.FragmentInputPasswordBinding
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.register.confirm_password.ConfirmationRequest
import com.example.taxi.domain.model.register.confirm_password.ResendSmsRequest
import com.example.taxi.domain.model.register.confirm_password.UserData
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.HomeActivity
import com.example.taxi.utils.*
import com.google.android.gms.auth.api.phone.SmsRetriever
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern


class InputPasswordFragment : Fragment() {
    private lateinit var viewBinding: FragmentInputPasswordBinding
    private val inputPasswordViewModel: InputPasswordViewModel by viewModel()
    private val preferenceManager: UserPreferenceManager by inject()
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var referrerClient: InstallReferrerClient

    private var step = 1
    var refererId: Int? = null

    private lateinit var imm: InputMethodManager

    companion object {
        const val DURATION_TIME: Long = 60_000
        const val INTERVAL_TIME: Long = 1_000
    }

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val otp = intent.getStringExtra(EXTRA_OTP_CODE)
            viewBinding.pinView.setText(otp)

        }
    }
    private lateinit var timer: ResendTimerUtil


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentInputPasswordBinding.inflate(inflater, container, false)
        // time start
        timer = ResendTimerUtil(durationMillis = DURATION_TIME,
            intervalMillis = INTERVAL_TIME,
            oonTick = {
                viewBinding.countDownTextView.text =
                    if ((it / 1000) >= 10) "00:${it / 1000}" else "00:0${it / 1000}"
            },
            oonFinish = { updateCountDownTextView() }
        ).apply { start() }

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        referrerClient = InstallReferrerClient.newBuilder(requireContext()).build()

        startSmsRetriever()

        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        val response: ReferrerDetails = referrerClient.installReferrer
                        val referrerUrl = response.installReferrer

                        val uri = Uri.parse(referrerUrl)
                        refererId = uri.getQueryParameter("referrer")?.toIntOrNull()
                        // Use the referrerUrl as needed for your app.
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to reconnect on the next app launch.
            }
        })
        imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputPasswordViewModel.confirmResponse.observe(viewLifecycleOwner, ::updateView)

        with(viewBinding) {
            pinView.requestFocus()
            imm.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT)

            txtShowPhone.text = PhoneNumberUtil.formatPhoneNumber(
                phoneNumber = preferenceManager.getPhone(), countryCode = "UZ"
            ).toString()

            fbnBackHome.setOnClickListener { navigateUp() }
            fbnInputPassword.setOnClickListener { handlePasswordInput() }

            PinViewUtils.setPinViewTextChangedListener(pinView) {
                inputPasswordViewModel.confirmPassword(
                    ConfirmationRequest(preferenceManager.getTokenForPhone().orEmpty(), it.toInt()),refererId
                )
            }
        }

    }


        private fun startSmsRetriever() {
            val client = SmsRetriever.getClient(requireActivity())
            val task = client.startSmsRetriever()
            task.addOnSuccessListener {
                // Successfully started retriever
            }
            task.addOnFailureListener {
                // Failed to start retriever
            }
        }

    private fun extractCode(message: String): String {
        // Assuming the OTP is a 6-digit code
        val pattern = Pattern.compile("(?i)Code:\\s*(\\d{4})")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) {
            matcher.group(1) ?: ""
        } else {
            ""
        }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    private fun navigateNext() {
        findNavController().navigate(R.id.personalDataFragment)
    }

    private fun handlePasswordInput() {
        if (step == 0) {
            activity?.let { HomeActivity.open(it) }
        } else {
            navigateNext()
        }
    }

    private fun incorrectPassword() {
        with(viewBinding) {
            pinView.apply {
                setLineColor(Color.parseColor("#F50000"))
                shake()
            }
            fbnInputPassword.isEnabled = false
            countDownTextView.isEnabled = true
        }

    }

    private fun correctPassword() {
        timer.stop()
        with(viewBinding.pinView) {
            setLineColor(Color.parseColor("#18A801"))
            isEnabled = false
            correct()
        }

        viewBinding.apply {
            countDownTextView.isEnabled = false
            fbnInputPassword.isEnabled = true
        }


    }

    private fun updateView(resource: Resource<MainResponse<UserData<IsCompletedModel>>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {}
                ResourceState.ERROR -> incorrectPassword()

                ResourceState.SUCCESS -> {
                    correctPassword()
                    step = it.data?.step ?: 0
                    preferenceManager.apply {
                        it.data?.data?.let { it1 -> saveResponse(it1) }
                        if (it.data?.data?.isCompleted?.string == true) setRegisterComplete()
                    }

                }
            }


        }
    }

    private fun View.addRipple() = with(TypedValue()) {
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
        setBackgroundResource(resourceId)
    }

    private fun View.shake() {
        startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_animation))
    }

    private fun View.correct() {
        startAnimation(AnimationUtils.loadAnimation(context, R.anim.correct_anim))
    }


    @SuppressLint("SetTextI18n")
    private fun updateCountDownTextView() {
        with(viewBinding.countDownTextView) {
            text = getString(R.string.resend_sms)
            isClickable = true
            isFocusable = true
            addRipple()
            setTextColor(Color.parseColor("#014EA8"))
            setOnClickListener {
                inputPasswordViewModel.resendSMS(
                    ResendSmsRequest(
                        preferenceManager.getTokenForPhone().orEmpty()
                    )
                )
                timer.stop()
                viewBinding.pinView.text?.clear()
                timer.start()
                /* Resend action here */
            }
        }
    }

    override fun onDestroy() {
        referrerClient.endConnection()
        super.onDestroy()
        timer.stop()
    }


    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(localReceiver, IntentFilter(ACTION_OTP_RECEIVED))
        startSmsRetriever()
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateUp()
            }
        }.also { requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it) }
    }

    override fun onPause() {
        onBackPressedCallback.remove()
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(localReceiver)
    }

}

