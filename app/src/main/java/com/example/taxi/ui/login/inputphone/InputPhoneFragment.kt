package com.example.taxi.ui.login.inputphone

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentInputPhoneBinding
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.register.RegisterData
import com.example.taxi.domain.model.register.RegisterRequest
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class InputPhoneFragment : Fragment() {

    private lateinit var viewBinding: FragmentInputPhoneBinding
    private val registerViewModel: RegisterViewModel by viewModel()
    private val preferenceManager: UserPreferenceManager by inject()
    private lateinit var loadingDialog: Dialog
    private lateinit var blockDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentInputPhoneBinding.inflate(layoutInflater, container, false)
        loadingDialog = DialogUtils.loadingDialog(requireContext())
        blockDialog = DialogUtils.blockDialog(requireContext())
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createPhoneNumberPlateEditText(viewBinding.edtInputPhone, viewBinding.textInputLayout)
        registerViewModel.registerResponse.observe(viewLifecycleOwner, ::updateView)

        val originalText = if (preferenceManager.getLanguage().code == "uz")"Давом этиш орқали сиз Imkaan taxi Хизмат кўрсатиш шартларига рози бўласиз ва Махфийлик сиёсатемизни ўқиганлигингизни тасдиқлайсиз." else "Davom etish orqali siz Imkaan taxining Xizmat ko'rsatish shartlariga rozi bo'lasiz va Maxfiylik siyosatimizni o'qiganligingizni tasdiqlaysiz."
        val styledText = getStyledText(originalText)
        viewBinding.tvPrivacy.text = styledText
        viewBinding.tvPrivacy.movementMethod = LinkMovementMethod.getInstance()
        viewBinding.tvPrivacy.highlightColor = resources.getColor(android.R.color.transparent)
        viewBinding.fbnInputPhone.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            val phone = "${viewBinding.textInputLayout.prefixText} ${viewBinding.edtInputPhone.text}"

            registerViewModel.register(
                RegisterRequest(
                    PhoneNumberUtil.formatPhoneNumber(phoneNumber = phone, countryCode = "UZ")
                        .toString()
                )
            )
        }

    }

    private fun validateInputs(): Boolean {
        with(viewBinding) {
            if (!isValidInput(
                    textInputLayout,
                    ValidationUtils::isValidPhoneNumber,
                    getString(R.string.input_phone_number)
                )
            ) return false
        }
        return true
    }

    private fun isValidInput(
        input: TextInputLayout,
        validationFunction: (String) -> Boolean,
        errorMessage: String
    ): Boolean {
        if (!validationFunction(input.editText?.text.toString())) {
            input.error = errorMessage
            return false
        }
        input.error = null
        return true
    }

    private fun updateView(resource: Resource<MainResponse<RegisterData>>?) {
        resource?.let {
            when (resource.state) {
                ResourceState.LOADING -> {
                    loadingDialog.show()
                }
                ResourceState.SUCCESS -> {
                    loadingDialog.dismiss()
                    if (it.data?.status == 203){
                        blockDialogShow(it.data.data)

                    }else{
                        processSuccessState(resource.data)
                    }
                }
                ResourceState.ERROR -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "${resource.message} ${resource.data}", Toast.LENGTH_SHORT).show()

                }
            }
        }

    }

    private fun blockDialogShow(data: RegisterData) {
        val error : TextView = blockDialog.findViewById(R.id.txt_error_desc_error_block)
        val callButton : MaterialButton = blockDialog.findViewById(R.id.btn_call_to_dispetcher)
        val cancelButton: MaterialButton = blockDialog.findViewById(R.id.btn_call_to_cancel)
        error.text = data.message
        blockDialog.show()

        callButton.setOnClickListener {
            ButtonUtils.callToDispatchWhenBlock(requireContext(),data.phone)
        }

        cancelButton.setOnClickListener {

            blockDialog.dismiss()
        }

    }

    private fun processSuccessState(response: MainResponse<RegisterData>?) {
        response?.data?.let {
            preferenceManager.apply {
                saveToken(it.token)
                savePhone(it.phone)
            }
            registerViewModel.clear()
            viewBinding.edtInputPhone.text?.clear()
            findNavController().navigate(R.id.inputPasswordFragment)
        }
    }

    private fun getStyledText(text: String): SpannableString {
        val spannableString = SpannableString(text)

        // Define the parts to change size
        val a = if(preferenceManager.getLanguage().code == "uz") "Хизмат кўрсатиш" else "Xizmat ko'rsatish"
        val xizmatStart =   text.indexOf(a)
        val xizmatEnd = xizmatStart + a.length

        val b = if(preferenceManager.getLanguage().code == "uz") "Махфийлик сиёсатемизни" else "Maxfiylik siyosatimizni"

        val maxfiylikStart = text.indexOf(b)
        val maxfiylikEnd = maxfiylikStart + b.length

        // Apply size span
        spannableString.setSpan(RelativeSizeSpan(1.2f), xizmatStart, xizmatEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), xizmatStart, xizmatEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(RelativeSizeSpan(1.2f), maxfiylikStart, maxfiylikEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), maxfiylikStart, maxfiylikEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl()
            }
        }, maxfiylikStart, maxfiylikEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


        return spannableString
    }

    private fun openUrl() {
        val url = "https://bekjaantaxi.uz/policy"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

}