package com.example.taxi.ui.login.personaldata

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentPersonalDataBinding
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.register.confirm_password.UserData
import com.example.taxi.domain.model.register.person_data.PersonDataRequest
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.utils.*
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class PersonalDataFragment : Fragment() {

    private lateinit var viewBinding: FragmentPersonalDataBinding
    private val personalDataViewModel: PersonalDataViewModel by viewModel()
    private lateinit var preferenceManager: UserPreferenceManager

    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentPersonalDataBinding.inflate(inflater, container, false)
        dialog = DialogUtils.loadingDialog(requireContext())
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = UserPreferenceManager(requireContext())
        personalDataViewModel.personDataResponse.observe(this, ::handleUi)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {

            with(inputBirthEt) {
                keyListener = null
                setOnClickListener {
                    DatePickerUtils.showDatePickerDialog(requireActivity()) {
                        viewBinding.inputBirthDayEt.error = null
                        setText(it)
                    }
                }
            }

            setupGenderSelectButtons()

            nextDetailCarFbn.setOnClickListener {
                val name = viewBinding.inputNameEt.editText?.text.toString()
                val surname = viewBinding.inputSurnameEt.editText?.text.toString()
                val birthDay = viewBinding.inputBirthDayEt.editText?.text.toString()
                val gender = if (viewBinding.selectManRadio.isChecked) 1 else 2

                if (!isValidInput(inputNameEt, ValidationUtils::isValidName, R.string.input_name) ||
                    !isValidInput(
                        inputSurnameEt,
                        ValidationUtils::isValidName,
                        R.string.input_surname
                    ) ||
                    !isValidInput(
                        inputBirthDayEt,
                        ValidationUtils::isValidBirthday,
                        R.string.input_birth
                    )
                ) {
                    return@setOnClickListener
                }


                val requestData = PersonDataRequest(
                    first_name = name,
                    last_name = surname,
                    born = birthDay,
                    gender = gender
                )
                personalDataViewModel.fillPersonData(requestData)
            }


        }

    }


    private fun isValidInput(
        inputLayout: TextInputLayout,
        validationFunction: (String) -> Boolean,
        errorMessageId: Int
    ): Boolean {
        val input = inputLayout.editText?.text.toString()
        if (!validationFunction(input)) {
            inputLayout.error = getString(errorMessageId)
            return false
        }
        inputLayout.error = null
        return true
    }

    private fun FragmentPersonalDataBinding.setupGenderSelectButtons() {
        selectManButton.setOnClickListener { setGender(true) }
        selectWomenButton.setOnClickListener { setGender(false) }
        selectManRadio.setOnClickListener { setGender(true) }
        selectWomenRadio.setOnClickListener { setGender(false) }
    }

    private fun FragmentPersonalDataBinding.setGender(isMale: Boolean) {
        selectManRadio.isChecked = isMale
        selectWomenRadio.isChecked = !isMale
    }

    private fun handleUi(resource: Resource<MainResponse<UserData<IsCompletedModel>>>?) {

        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                   dialog.show()
                }
                ResourceState.ERROR -> {
                    dialog.dismiss()
                    DialogUtils.createChangeDialog(
                        requireActivity(),
                        title = getString(R.string.error),
                        message = it.message,
                        color = R.color.red
                    )
                }
                ResourceState.SUCCESS -> {
                    dialog.dismiss()
                    findNavController().navigate(R.id.carDataFragment)
                }
            }
        }
    }
}

