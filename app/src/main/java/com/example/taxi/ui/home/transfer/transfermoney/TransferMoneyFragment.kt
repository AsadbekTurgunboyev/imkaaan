package com.example.taxi.ui.home.transfer.transfermoney

import android.app.ActionBar
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentTransferMoneyBinding
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.balance.BalanceData
import com.example.taxi.domain.model.transfer.DriverNameByIdResponse
import com.example.taxi.domain.model.transfer.TransferRequest
import com.example.taxi.ui.home.dashboard.DashboardViewModel
import com.example.taxi.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import kotlin.properties.Delegates

class TransferMoneyFragment : Fragment() {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineJob)

    lateinit var dialog : Dialog
    var money = 0
    var amount by Delegates.notNull<Int>()
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()
    private val transferMoneyViewModel: TransferMoneyViewModel by viewModel()

    lateinit var viewBinding: FragmentTransferMoneyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentTransferMoneyBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDialog()
        dashboardViewModel.getBalance()
        EditTextIdUtils.setEditTextChangedListener(viewBinding.etId) {
            val id = it.toIntOrNull()
            if (id != null) {
                coroutineScope.launch {
                    try {
                        transferMoneyViewModel.getDriverNameById(it.toInt())
                    } catch (e: IOException) {
                        Log.e("API", "Request failed: ${e.message}")
                    }
                }
            }

        }

        EditTextIdUtils.setEditTextListenerForButton(viewBinding.edMoneyValue) {
            val money = it.toIntOrNull()
            if (money != null) {
                viewBinding.sendMoneyButton.isEnabled = money >= 1000
            } else {
                viewBinding.sendMoneyButton.isEnabled = false
            }
        }

        viewBinding.sendMoneyButton.setOnClickListener {
            val transferId = viewBinding.etId.text.toString().toIntOrNull()
            amount = viewBinding.edMoneyValue.text.toString().toInt()
            if (transferId != null) {

                DialogUtils.warningDialog(requireContext()) {
                    transferMoneyViewModel.transferMoney(
                        TransferRequest(
                            to_id = transferId,
                            amount = amount
                        )
                    )
                }.show()

            }


        }

        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }
        dashboardViewModel.balanceResponse.observe(viewLifecycleOwner) { resource ->
            updateBalance(resource)
        }

        transferMoneyViewModel.transferMoney.observe(viewLifecycleOwner) {
            setTransferUi(it)
        }

        transferMoneyViewModel.driverName.observe(viewLifecycleOwner) {
            setDriverName(it)
        }
    }

    private fun setDialog() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_loading)
        dialog.window?.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setCancelable(false)
    }

    private fun setTransferUi(resource: Resource<MainResponse<Any>>?) {

        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    dialog.show()
                }
                ResourceState.ERROR -> {
                    dialog.dismiss()
                    it.message?.let { it1 ->
                        Log.e("tekshirish", "setTransferUi: $it1 ", )
                        DialogUtils.failedDialog(context = requireContext(),
                            message = it1,
                            onExit = {
                                val navController = findNavController()
                                navController.navigateUp()
                            },
                            onFillBalance = {
                                val navController = findNavController()
                                navController.navigate(R.id.transferDashboardFragment)
                            }
                        ).show()
                    }

                }
                ResourceState.SUCCESS -> {
                    dialog.dismiss()
                    DialogUtils.successDialog(
                        context = requireContext(),
                        money = amount.toString()
                    ) {
                        val navController = findNavController()
                        navController.navigateUp()
                    }.show()
                }
            }
        }

    }

    private fun updateBalance(resource: Resource<MainResponse<BalanceData>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {

                }

                ResourceState.ERROR -> {

                }
                ResourceState.SUCCESS -> {
                    money = it.data?.data?.total!!
                    viewBinding.transferBalanceTextView.text =
                        PhoneNumberUtil.formatMoneyNumberPlate(money.toString())
                }
            }
        }
    }

    private fun setDriverName(resource: Resource<MainResponse<DriverNameByIdResponse>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    viewBinding.pbLogin.visibility = View.VISIBLE
                    viewBinding.edMoneyValue.isEnabled = false
                    viewBinding.titleTransfer.isEnabled = false
                    viewBinding.titleTransfer.alpha = 0.5f
                    viewBinding.transferPersonNameTextView.text = ""

                }
                ResourceState.SUCCESS -> {
                    viewBinding.pbLogin.visibility = View.GONE
                    viewBinding.transferPersonNameTextView.text = it.data?.data?.name
                    viewBinding.edMoneyValue.isEnabled = true
                    viewBinding.titleTransfer.isEnabled = true
                    viewBinding.titleTransfer.alpha = 1f
                    viewBinding.edMoneyValue.alpha = 1f
                }
                ResourceState.ERROR -> {
                    viewBinding.edMoneyValue.isEnabled = false
                    viewBinding.titleTransfer.isEnabled = false
                    viewBinding.titleTransfer.alpha = 0.5f
                    viewBinding.edMoneyValue.alpha = 0.5f
                    viewBinding.transferPersonNameTextView.text = it.message
                    viewBinding.pbLogin.visibility = View.GONE
                    Log.e("API", "Request failed: ${it.message}")


                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the coroutine job when the activity is destroyed
        coroutineJob.cancel()
    }
}