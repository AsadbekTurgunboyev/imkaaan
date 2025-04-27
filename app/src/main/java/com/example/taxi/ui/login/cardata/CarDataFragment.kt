package com.example.taxi.ui.login.cardata

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentCarDataBinding
import com.example.taxi.domain.model.register.car_data.*
import com.example.taxi.utils.*
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CarDataFragment : Fragment() {

    lateinit var viewBinding: FragmentCarDataBinding
    private val carDataViewModel: CarDataViewModel by viewModel()
    private lateinit var viewModel: CarViewModel
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(owner = this)[CarViewModel::class.java]

        carDataViewModel.carDataResponse.observe(this, ::updateCarDataUi)
        carDataViewModel.carColorResponse.observe(this, ::updateCarColorUi)
        carDataViewModel.carInfo.observe(this, ::updateFillData)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentCarDataBinding.inflate(inflater, container, false)
        loadingDialog = DialogUtils.loadingDialog(requireContext())
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLoading()
        lifecycleScope.launch {
            carDataViewModel.getCarData()
            carDataViewModel.getCarColor()
        }

        nextChangeEdittext(viewBinding.autoCarFirstNumber) {
            viewBinding.carNumberPlateEditText.requestFocus()
        }
        /**
         * agar keyinchalik avtomabil uchun qolip boyicha soralsa ushbu koment ochib qo'yiladi
         */
//        createCarNumberPlateEditText(viewBinding.carNumberPlateEditText)

        /**
         * agar keyinchalik texpasport qolip boyicha soralsa ushbu koment ochib qo'yiladi
         */
//        createCarLicencePlateEdittext(viewBinding.carPassportEdittext)

        with(viewBinding) {


            nextDataCarFbn.setOnClickListener {
                if (!validateInputs()) return@setOnClickListener

                val carId = viewModel.carId.value
                val colorId = viewModel.colorId.value
                carId?.let { id ->
                    colorId?.let { colorId ->
                        CarInfoRequest(
                            car_id = id,
                            position = autoCarPosition.text.toString().toInt(),
                            tech_pass_number = carPassportEdittext.text.toString(),
                            car_number = combineTwo(autoCarFirstNumber.text.toString(), carNumberPlateEditText.text.toString()),
                            color_id = colorId
                        )
                    }
                }?.let(carDataViewModel::fillCarInfo)
            }

            backPersonDataFbn.setOnClickListener {
                findNavController().navigateUp()
            }

        }


    }


    private fun updateFillData(resource: Resource<CarInfoResponse>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    loadingDialog.show()
                }
                ResourceState.ERROR -> {
                    loadingDialog.dismiss()
                    DialogUtils.createChangeDialog(requireActivity(),
                        title = getString(R.string.error),
                        message = it.message,
                        color = R.color.red)
                }
                ResourceState.SUCCESS -> {
                    loadingDialog.dismiss()
                    findNavController().navigate(R.id.fillSelfieFragment)
                }
            }
        }

    }

    private fun updateCarDataUi(resource: Resource<CarDataResponse>?) {

        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {

                }
                ResourceState.ERROR -> {
                }
                ResourceState.SUCCESS -> {
                    updateAutoCompleteUI(resource.data?.data, viewBinding.autoCarData, viewModel::setCarId)
                }
            }
        }

    }

    private fun updateAutoCompleteUI(
        colorDataList: List<CarData>?,
        autoCompleteTextView: AutoCompleteTextView,
        idSetter: (Int?) -> Unit
    ) {
        val colorNameList = colorDataList?.map { it.name }
        val colorNameToDataMap = colorDataList?.associateBy { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            colorNameList ?: emptyList()
        )
        autoCompleteTextView.apply {
            setAdapter(adapter)
            setOnItemClickListener { parent, _, position, _ ->
                val selectedColorName = parent.getItemAtPosition(position) as? String
                val selectedData = selectedColorName?.let { colorNameToDataMap?.get(it) }
                idSetter(selectedData?.id?.toInt())
            }
            threshold = 1
        }
    }

    private fun updateCarColorUi(resource: Resource<CarColorResponse>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    setLoading()

                }
                ResourceState.ERROR -> {

                }
                ResourceState.SUCCESS -> {
                    stopLoading()
                    updateAutoColorCompleteUI(resource.data?.data, viewBinding.autoCarColor,viewModel::setColorId)
                    setUpPositionAutoComplete()
                }
            }
        }
    }

    private fun updateAutoColorCompleteUI(colorDataList: List<CarColorData>?, autoCompleteTextView: AutoCompleteTextView, idSetter: (Int?) -> Unit) {
        val colorNameList = colorDataList?.map { it.name }
        val colorNameToDataMap = colorDataList?.associateBy { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            colorNameList ?: emptyList()
        )
        autoCompleteTextView.apply {
            setAdapter(adapter)
            setOnItemClickListener { parent, _, position, _ ->
                val selectedColorName = parent.getItemAtPosition(position) as? String
                val selectedData = selectedColorName?.let { colorNameToDataMap?.get(it) }
                idSetter(selectedData?.id?.toInt())
            }
            threshold = 1
        }
    }

    private fun setUpPositionAutoComplete() {
        val items = arrayListOf("1", "2", "3", "4", "5")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        viewBinding.autoCarPosition.apply {
            setAdapter(adapter)
            threshold = 1
        }
    }

    private fun stopLoading() {
        with(viewBinding) {
            dataCar.visibility = View.VISIBLE
            shimmerCarData.stopShimmer()
            shimmerCarData.visibility = View.INVISIBLE
        }
    }


    private fun setLoading() {
        with(viewBinding) {
            dataCar.visibility = View.INVISIBLE
            shimmerCarData.startShimmer()
            shimmerCarData.visibility = View.VISIBLE
        }

    }


    private fun validateInputs(): Boolean {
        with(viewBinding) {
            if (!isValidInput(autoCarDataInput, ValidationUtils::isValidCarName, getString(R.string.avtomobil_markasini_kiriting))) return false
            if (!isValidInput(autoCarColorInput, ValidationUtils::isValidCarColor, getString(R.string.avtomobil_rangini_kiriting))) return false
            if (!isValidInput(autoCarPositionInput, ValidationUtils::isValidCarPosition, getString(R.string.avtomobil_pozitsiyasini_kiriting))) return false
            if (!isValidInput(autoCarFirstNumberInput, ValidationUtils::isValidCarFirstTwoNumber,getString(R.string.error))) return false
            if (!isValidInput(carNumberPlateInput, ValidationUtils::isValidCarMainNumber,getString(R.string.raqamni_kiriting))) return false
            if (!isValidInput(carPassportEdittextInput, ValidationUtils::isValidCarPassport, getString(R.string.texpassport_kiriting))) return false
        }
        return true
    }


    private fun isValidInput(input: TextInputLayout, validationFunction: (String) -> Boolean, errorMessage: String): Boolean {
        if (!validationFunction(input.editText?.text.toString())) {
            input.error = errorMessage
            return false
        }
        input.error = null
        return true
    }

    private fun combineTwo(a: String, b: String): String {
        return a + b.replace("\\s+".toRegex(), "")
    }
}