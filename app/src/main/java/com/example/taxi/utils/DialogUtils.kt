package com.example.taxi.utils

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.taxi.R
import com.example.taxi.databinding.DialogDetailFilterBinding
import com.example.taxi.databinding.DialogDetailHistoryBinding
import com.example.taxi.databinding.DialogFilterBinding
import com.example.taxi.databinding.DialogFinishOrderBinding
import com.example.taxi.databinding.DialogHistoryFilterBinding
import com.example.taxi.domain.model.history.Ride
import com.example.taxi.domain.model.history.RideAddress
import com.example.taxi.domain.model.history.RideCreatedAt
import com.example.taxi.domain.model.history.RideStatus
import com.example.taxi.domain.model.history.RideType
import com.example.taxi.domain.model.history.RideUser
import com.example.taxi.domain.model.transfer.DataItem
import com.example.taxi.domain.model.transfer.HistoryCreatedAt
import com.example.taxi.domain.model.transfer.HistoryType
import com.example.taxi.ui.home.transfer.transferhistory.IN_COME
import com.example.taxi.ui.home.transfer.transferhistory.OUT_COME
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.tapadoo.alerter.Alerter

object DialogUtils {

    fun createChangeDialog(
        activity: Activity,
        title: String? = null,
        message: String? = null,
        color: Int
    ) {

        if (title != null) {
            Alerter.create(activity = activity)
                .setTitle(title = title)
                .setText(message.toString())
                .setBackgroundColorRes(colorResId = color)
                .show()
        }
    }

    fun loadingDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }
        dialog.setCancelable(false)

        return dialog
    }


    fun checkSuccessDialog(
        context: Context,
        onSuccessAction: () -> Unit = {},
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCancelable(false)
        val exit = dialog.findViewById<MaterialButton>(R.id.btn_error_exit_suc)
        dialog.findViewById<TextView>(R.id.txt_error_desc).text = context.getString(R.string.success_check_photo)
        dialog.findViewById<TextView>(R.id.txt_error_balance).visibility = View.GONE
        val moneyTextView = dialog.findViewById<TextView>(R.id.txt_balance_num)
        moneyTextView.visibility = View.GONE

        exit.setOnClickListener {
            onSuccessAction()
            dialog.dismiss()
        }

        return dialog
    }

    fun blockDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_block_user)
        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }
        dialog.setCancelable(false)

        return dialog
    }


    fun showConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.confirm))
            .setMessage(context.getString(R.string.are_you_sure))
            .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                onConfirm.invoke()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    fun showIsSocketConnect(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.you_are_work))
        builder.setMessage(context.getString(R.string.you_are_connect_to_socket))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.understand)) { dialog, id ->
                //do things
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    fun showExitDialog(
        context: Context,
        onSuccessAction: () -> Unit = {},
        onNegativeAction: () -> Unit = {}
    ): Dialog {
        val builder =
            AlertDialog.Builder(context)
        builder.setTitle("Exit")
        builder.setMessage("Do you want to exit the app?")
        builder.setPositiveButton(
            "Ha"
        ) { _: DialogInterface?, _: Int ->
            onSuccessAction()

        }
        builder.setNegativeButton(
            "Yo'q"
        ) { _: DialogInterface?, _: Int ->
            onNegativeAction()
        }
        return builder.create()
    }

    fun warningDialog(
        context: Context,
        onSuccessAction: () -> Unit = {}
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_warning)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val exit = dialog.findViewById<MaterialButton>(R.id.btn_error_exit)
        val yes = dialog.findViewById<MaterialButton>(R.id.btn_error_yes)

        yes.setOnClickListener {
            onSuccessAction()
            dialog.dismiss()
        }
        exit.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    fun successDialog(
        context: Context,
        money: String,
        onSuccessAction: () -> Unit = {},
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCancelable(false)
        val exit = dialog.findViewById<MaterialButton>(R.id.btn_error_exit_suc)
        val moneyTextView = dialog.findViewById<TextView>(R.id.txt_balance_num)
        moneyTextView.text = money
        exit.setOnClickListener {
            onSuccessAction()
            dialog.dismiss()
        }

        return dialog
    }

    fun orderCancelDialog(
        context: Context,
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_order_cancel)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCancelable(true)
        val exit = dialog.findViewById<MaterialButton>(R.id.btn_error_exit_cancel)
        exit.setOnClickListener {

            dialog.dismiss()
        }

        return dialog
    }

    fun failedDialog(
        context: Context,
        message: String,
        onFillBalance: () -> Unit = {},
        onExit: () -> Unit = {}
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_error)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCancelable(false)
        val addMoneyButton = dialog.findViewById<MaterialButton>(R.id.btn_add_money)
        val exitButton = dialog.findViewById<MaterialButton>(R.id.btn_error_exit)
        val moneyTextView = dialog.findViewById<TextView>(R.id.txt_error_desc_error)
        moneyTextView.text = message

        addMoneyButton.setOnClickListener {
            onFillBalance()
            dialog.dismiss()
        }
        exitButton.setOnClickListener {
            onExit()
            dialog.dismiss()
        }

        return dialog
    }

    fun createDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveAction: String? = null,
        negativeAction: String? = null,
        onSuccessAction: () -> Unit = {},
        onNegativeAction: () -> Unit = {}
    ): Dialog {
        val alertDialogBuilder =
            AlertDialog.Builder(context, R.style.AppDialogTheme)
        if (title != null) alertDialogBuilder.setTitle(title)
        if (message != null) alertDialogBuilder.setMessage(message)
        if (positiveAction != null) alertDialogBuilder.setPositiveButton(
            positiveAction
        ) { _: DialogInterface?, _: Int ->
            onSuccessAction()
        }
        if (negativeAction != null) {
            alertDialogBuilder.setNegativeButton(
                negativeAction
            ) { _: DialogInterface?, _: Int ->
                onNegativeAction()
            }
            alertDialogBuilder.setCancelable(false)
        } else {
            alertDialogBuilder.setCancelable(true)
        }
        return alertDialogBuilder.create()
    }


    fun showFinishDialog(
        context: Context,
        currentDriveCost: String,
        finish: () -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val viewDialog = DialogFinishOrderBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setContentView(viewDialog.root)
        dialog.setCancelable(false)

        viewDialog.finishButtonDialog.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        viewDialog.priceAll.text = currentDriveCost


        dialog.show()

    }

    fun filterDialog(
        context: Context,
        dataRange: List<String>,
        typeOld: Int,
        range: (range: List<String>, type: Int) -> Unit,
        removeFilter: () -> Unit = {}
    ): Dialog {

        val dialog = Dialog(context)
        val viewDialog = DialogFilterBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(viewDialog.root)

        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.BOTTOM)

        viewDialog.dismissDialog.setOnClickListener {
            dialog.dismiss()
        }
        viewDialog.removeFilterButton.setOnClickListener {
            removeFilter()
            dialog.dismiss()
        }

        val type = arrayOf(
            context.getString(R.string.all),
            context.getString(R.string.income),
            context.getString(R.string.outcome)
        )
        if (dataRange.isNotEmpty()) {
            viewDialog.fromButton.text = dataRange[0]
            viewDialog.toButton.text = dataRange[1]
        }
        viewDialog.autoTypeData.setText(type[typeOld])

        if (viewDialog.fromButton.text.isEmpty()) viewDialog.fromButton.text =
            context.getString(R.string.from)
        if (viewDialog.toButton.text.isEmpty()) viewDialog.toButton.text =
            context.getString(R.string.to)


        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            type
        )

        viewDialog.autoTypeData.setAdapter(adapter)
        var selectedTypeName = typeOld
        viewDialog.autoTypeData.setOnItemClickListener { _, _, position, _ ->
            selectedTypeName = position
        }
        viewDialog.autoTypeData.threshold = 1

        val newDateRange = mutableListOf<String>("", "")

        fun showDatePickerAndUpdateButton(
            context: Context,
            targetButton: TextView,
            dateIndex: Int,
            oppositeDateIndex: Int
        ) {
            DatePickerUtils.showDatePickerDialog(context as Activity) { selectedDate ->
                targetButton.text = selectedDate
                newDateRange[dateIndex] = selectedDate
                if (newDateRange[oppositeDateIndex].isEmpty()) {
                    showDatePickerAndUpdateButton(
                        context,
                        if (dateIndex == 0) viewDialog.toButton else viewDialog.fromButton,
                        oppositeDateIndex,
                        dateIndex
                    )
                }
            }
        }

        viewDialog.fromButton.setOnClickListener {
            showDatePickerAndUpdateButton(
                context,
                viewDialog.fromButton,
                0,
                1
            )
        }

        viewDialog.toButton.setOnClickListener {
            showDatePickerAndUpdateButton(
                context,
                viewDialog.toButton,
                1,
                0
            )
        }



        viewDialog.acceptOrder.setOnClickListener {
            if (newDateRange.size != 2) {
                Alerter.create(context as Activity)
                    .setText(context.getString(R.string.enter_range))
                    .setBackgroundColorRes(R.color.red)
                    .setDuration(1000)
                    .show()
            } else {
                if (newDateRange[0].isNotEmpty() || newDateRange[1].isNotEmpty() || selectedTypeName != 0) {
                    if (newDateRange[0].isEmpty() && newDateRange[1].isEmpty()) {
                        range(dataRange, selectedTypeName)
                    } else {
                        range(newDateRange, selectedTypeName)
                    }
                } else if (newDateRange[0].isEmpty() && newDateRange[1].isEmpty() && selectedTypeName == 0) {

                    if (dataRange.isNotEmpty()) {

                        if (dataRange[0].isNotEmpty() && dataRange[1].isNotEmpty()) {
                            range(dataRange, selectedTypeName)
                        } else {
                            removeFilter()
                        }
                    } else {
                        dialog.dismiss()
                    }
                }
            }
            dialog.dismiss()
        }


        return dialog
    }

    fun filterHistoryDialog(
        context: Context,
        dataRange: List<String>,
        oldType: Int,
        oldStatus: Int,
        range: (range: List<String>, type: Int, status: Int) -> Unit,
        removeFilter: () -> Unit = {}
    ): Dialog {
        val dialog = Dialog(context)
        val viewDialog = DialogHistoryFilterBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(viewDialog.root)

        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.BOTTOM)

        viewDialog.dismissDialog.setOnClickListener {
            dialog.dismiss()
        }
        viewDialog.removeFilterButton.setOnClickListener {
            removeFilter()
            dialog.dismiss()
        }

        if (dataRange.isNotEmpty()) {
            viewDialog.fromButton.text = dataRange[0]
            viewDialog.toButton.text = dataRange[1]
        }

        if (viewDialog.fromButton.text.isEmpty()) viewDialog.fromButton.text =
            context.getString(R.string.from)
        if (viewDialog.toButton.text.isEmpty()) viewDialog.toButton.text =
            context.getString(R.string.to)

        val type = arrayOf(
            context.getString(R.string.all),
            context.getString(R.string.admin),
            context.getString(R.string.tg),
            context.getString(R.string.mobile)

        )
        val status = arrayOf(
            context.getString(R.string.all),
            context.getString(R.string.success),
            context.getString(R.string.cancelOrder)

        )
        viewDialog.autoTypeData.setText(type[oldType])
        val old = when (oldStatus) {
            0 -> 0 // for all
            5 -> 1 // for success code
            4 -> 2 // for cancel code
            else -> {
                0
            }
        }
        viewDialog.autoStatusData.setText(status[old])

        val typeAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            type
        )
        val statusAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            status
        )
        viewDialog.autoTypeData.setAdapter(typeAdapter)
        viewDialog.autoStatusData.setAdapter(statusAdapter)
        var selectedTypeName = oldType
        var selectedStatusName: Int = oldStatus
        viewDialog.autoTypeData.setOnItemClickListener { _, _, position, _ ->
            selectedTypeName = position
        }
        viewDialog.autoStatusData.setOnItemClickListener { _, _, position, _ ->
            selectedStatusName = when (position) {
                0 -> 0 // for all
                1 -> 5 // for success code
                2 -> 4 // for cancel code
                else -> {
                    0
                }
            }
        }

        viewDialog.autoTypeData.threshold = 1
        viewDialog.autoStatusData.threshold = 1

        val newDateRange = mutableListOf<String>("", "")

        fun showDatePickerAndUpdateButton(
            context: Context,
            targetButton: TextView,
            dateIndex: Int,
            oppositeDateIndex: Int
        ) {
            DatePickerUtils.showDatePickerDialog(context as Activity) { selectedDate ->
                targetButton.text = selectedDate
                newDateRange[dateIndex] = selectedDate
                if (newDateRange[oppositeDateIndex].isEmpty()) {
                    showDatePickerAndUpdateButton(
                        context,
                        if (dateIndex == 0) viewDialog.toButton else viewDialog.fromButton,
                        oppositeDateIndex,
                        dateIndex
                    )
                }
            }
        }

        viewDialog.fromButton.setOnClickListener {
            showDatePickerAndUpdateButton(
                context,
                viewDialog.fromButton,
                0,
                1
            )
        }

        viewDialog.toButton.setOnClickListener {
            showDatePickerAndUpdateButton(
                context,
                viewDialog.toButton,
                1,
                0
            )
        }



        viewDialog.acceptOrder.setOnClickListener {
            if (newDateRange.isNullOrEmpty()) {
                Alerter.create(context as Activity)
                    .setText(context.getString(R.string.enter_range))
                    .setBackgroundColorRes(R.color.red)
                    .setDuration(1000)
                    .show()

            } else {

                if (newDateRange[0].isNotEmpty() || newDateRange[1].isNotEmpty() || selectedTypeName != 0 || selectedStatusName != 0) {
                    if (newDateRange[0].isEmpty() && newDateRange[1].isEmpty()) {
                        range(dataRange, selectedTypeName, selectedStatusName)
                    } else {
                        range(newDateRange, selectedTypeName, selectedStatusName)
                    }
//                    range(newDateRange, selectedTypeName, selectedStatusName)
                } else if (newDateRange[0].isEmpty() && newDateRange[1].isEmpty() && selectedTypeName == 0 && selectedStatusName == 0) {
                    if (dataRange.isNotEmpty()) {
                        if (dataRange[0].isNotEmpty() && dataRange[1].isNotEmpty()) {
                            range(dataRange, selectedTypeName, selectedStatusName)
                        } else {
                            removeFilter()
                        }
                    } else {
                        dialog.dismiss()
                    }

//                    removeFilter()
                }
            }
            dialog.dismiss()
        }


        return dialog
    }

    @SuppressLint("SetTextI18n")
    fun showDetailTransferHistoryDialog(
        data: DataItem<HistoryType, HistoryCreatedAt>,
        context: Context
    ): Dialog {
        val dialog = Dialog(context)
        val viewDialog = DialogDetailFilterBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(viewDialog.root)

        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.BOTTOM)



        with(viewDialog) {
            dismissDialog.setOnClickListener {
                dialog.dismiss()
            }
            dismissButton.setOnClickListener {
                dialog.dismiss()
            }

            driverIdFilter.text = data.id.toString()
            detailTransferType.text = data.type.name
            when (data.type.number) {
                IN_COME -> {
                    detailTransferValue.apply {
                        setTextColor(context.getColor(R.color.green))
                        text = "+${PhoneNumberUtil.formatMoneyNumberPlate(data.value.toString())}"
                    }

                }

                OUT_COME -> {
                    detailTransferValue.apply {
                        setTextColor(context.getColor(R.color.red))
                        text = "-${PhoneNumberUtil.formatMoneyNumberPlate(data.value.toString())}"
                    }
                }

                else -> {}
            }
            detailTransferBeforeValue.text = data.total.toString()

            detailTransferReason.text = data.reason
            detailTransferTime.text = data.createdAt.datetime

        }


        return dialog

    }


    fun showDetailHistoryDialog(
        data: Ride<RideType, RideAddress, RideUser, RideStatus, RideCreatedAt>,
        context: Context
    ): Dialog {
        val dialog = Dialog(context)
        val viewDialog = DialogDetailHistoryBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(viewDialog.root)

        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.BOTTOM)



        with(viewDialog) {
            dismissDialog.setOnClickListener {
                dialog.dismiss()
            }
            dismissButton.setOnClickListener {
                dialog.dismiss()
            }

            driverIdFilter.text = data.order_id.toString()
            detailToHistory.text = data.address.to
            detailFromHistory.text = data.address.from
            detailTransferTime.text = data.created_at.date

            detailHistoryPrice.text = PhoneNumberUtil.formatMoneyNumberPlate(data.cost.toString())

        }


        return dialog

    }


}