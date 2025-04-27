package com.example.taxi.ui.home.transfer.transferdash

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentTransferDashboardBinding
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.PaymentUrl
import com.example.taxi.domain.model.balance.BalanceData
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import com.example.taxi.ui.home.dashboard.DashboardViewModel
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.EditTextIdUtils
import com.example.taxi.utils.PhoneNumberUtil
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TransferDashboardFragment : Fragment() {

    lateinit var viewBinding: FragmentTransferDashboardBinding
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()
    lateinit var dialog: Dialog
    private var clickButton: MaterialCardView? = null
    private var payMeButton: MaterialCardView? = null
    private var uzumButton: MaterialCardView? = null
    private var edMoneyValue: EditText? = null
    private var sendMoneyButton: MaterialButton? = null
    private var loadingDialog: Dialog? = null

    val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentTransferDashboardBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = DialogUtils.loadingDialog(requireContext())

        dialog = Dialog(requireContext())
        viewBinding.transferMoneyButton.setOnClickListener {
            navController.navigate(R.id.transferMoneyFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.getBalance()
            dashboardViewModel.getDriverData()
        }

        dashboardViewModel.paymentProgress.observe(viewLifecycleOwner) {
            updatePaymentProgress(it)
        }
        dashboardViewModel.paymentType.observe(viewLifecycleOwner) {
            updatePaymentUi(it)
        }

        dashboardViewModel.balanceResponse.observe(viewLifecycleOwner) {
            updateMoneyUi(it)
        }

        dashboardViewModel.driverDataResponse.observe(viewLifecycleOwner) {
            setDataUi(it)
        }

        viewBinding.fbnBackHome.setOnClickListener {
            navController.navigateUp()
        }

        viewBinding.transaction.setOnClickListener {
            navController.navigate(R.id.transferHistoryFragment)
        }

        viewBinding.buttonTransfer.setOnClickListener {
            showTransferDialog()
        }


    }

    private fun updatePaymentProgress(resource: Resource<MainResponse<PaymentUrl>>?) {

         when (resource?.state) {
            ResourceState.LOADING -> {
                loadingDialog?.show()
            }
            ResourceState.SUCCESS -> {
                loadingDialog?.dismiss()
                dialog.dismiss()
                val url = resource.data?.data?.url
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
//                dashboardViewModel.clearPaymentProgress()

            }
            ResourceState.ERROR -> {
                loadingDialog?.dismiss()
                dialog.dismiss()
                dashboardViewModel.clearPaymentProgress()
                DialogUtils.createChangeDialog(
                    activity = requireActivity(),
                    title = getString(R.string.error),
                    message = "${resource.message}",
                    color =  R.color.tred
                )
            }
            else -> {}
        }


    }

    private fun updatePaymentUi(type: Int?) {

        when(type){
            1 -> {
                clickButton?.strokeWidth = 4
                payMeButton?.strokeWidth = 0
                uzumButton?.strokeWidth = 0
            }
            2 -> {
                clickButton?.strokeWidth = 0
                payMeButton?.strokeWidth = 4
                uzumButton?.strokeWidth = 0
            }
            3 ->{
                clickButton?.strokeWidth = 0
                payMeButton?.strokeWidth = 0
                uzumButton?.strokeWidth = 4
            }
        }

    }

    private fun setDataUi(resource: Resource<MainResponse<SelfieAllData<IsCompletedModel, StatusModel>>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.ERROR -> {}
                ResourceState.SUCCESS -> {
                    viewBinding.driverId.text = it.data!!.data.id.toString()
                }

                ResourceState.LOADING -> {}
            }
        }
    }

    private fun updateMoneyUi(resource: Resource<MainResponse<BalanceData>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {

                }
                ResourceState.SUCCESS -> {

                    viewBinding.balanceValue.text =
                        PhoneNumberUtil.formatMoneyNumberPlate(resource.data!!.data.total.toString())
                }

                ResourceState.ERROR -> {


                }
            }
        }
    }

    private fun showTransferDialog() {
        dashboardViewModel.paymentClick()
        dialog.setContentView(R.layout.dialog_payment)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        clickButton = dialog.findViewById(R.id.buttonClick)
        payMeButton = dialog.findViewById(R.id.buttonPayme)
        uzumButton = dialog.findViewById(R.id.buttonUzum)
        edMoneyValue = dialog.findViewById(R.id.ed_money_value)
        sendMoneyButton = dialog.findViewById(R.id.sendMoney_button)
        clickButton?.setOnClickListener { dashboardViewModel.paymentClick() }
        payMeButton?.setOnClickListener { dashboardViewModel.paymentPayme() }
        uzumButton?.setOnClickListener{dashboardViewModel.paymentUzum()}

        sendMoneyButton?.setOnClickListener {
            val amount = edMoneyValue?.text.toString()
            dashboardViewModel.payment(amount.toInt())
        }

        edMoneyValue?.let {
            EditTextIdUtils.setEditTextListenerForButton(it) { m ->
                val money = m.toIntOrNull()
                if (money != null) {
                    sendMoneyButton?.isEnabled = money >= 1000
                } else {
                    sendMoneyButton?.isEnabled = false
                }
            }
        }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dashboardViewModel.clearPaymentProgress()
    }
}