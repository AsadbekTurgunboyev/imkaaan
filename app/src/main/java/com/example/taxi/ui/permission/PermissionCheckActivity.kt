package com.example.taxi.ui.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.taxi.R
import com.example.taxi.databinding.ActivityPermissionCheckBinding
import com.example.taxi.utils.LocationPermissionUtils
import com.google.android.material.button.MaterialButton


class PermissionCheckActivity : AppCompatActivity() {
    private var isPromptMode = false

    private lateinit var viewBinding: ActivityPermissionCheckBinding

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                handlePermissionsGranted()
            }
//            else {
//                showPermissionDeniedDialog(getString(R.string.location))
//            }
        }

    private val backgroundLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                checkAndProceed()
            } else {
                showPermissionDeniedDialog(getString(R.string.location_background_1))
            }
        }

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                checkAndProceed()
            } else {
                showPermissionDeniedDialog(getString(R.string.overlay_perm))
            }
        }

    private val batteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                checkAndProceed()
            } else {
                showPermissionDeniedDialog(getString(R.string.batter_perm))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        isPromptMode = intent.getBooleanExtra(IS_PROMPT_MODE, false)

        if (isPromptMode) {
            checkAndAsk()
        } else {
            viewBinding = ActivityPermissionCheckBinding.inflate(layoutInflater)
            setContentView(viewBinding.root)
            if (!LocationPermissionUtils.isLocationEnabled(this)) {
                requestEnableLocation()
            } else if (!LocationPermissionUtils.isBasicPermissionGranted(this) || !LocationPermissionUtils.isBackgroundPermissionGranted(
                    this
                )
            ) {
//                showPermissionExplanationDialog()
            }

            viewBinding.askBattery.setOnClickListener {
                if (!LocationPermissionUtils.isPowerSavingModeEnabled(
                        this
                    )
                ) requestIgnoreBatteryOptimizations() else checkAndProceed()
            }
            viewBinding.askOverlay.setOnClickListener { if (!Settings.canDrawOverlays(this)) showOverlayPermissionDialog() else checkAndProceed() }
            viewBinding.askLocation.setOnClickListener { requestFineLocation() }
            viewBinding.askBackgroundLocation.setOnClickListener { checkBackgroundLocationPermission() }
            viewBinding.buttonConfirmPermissions.setOnClickListener { checkAndAsk() }
        }
    }

    private fun checkBackgroundLocationPermission() {
        if (LocationPermissionUtils.isLocationEnabled(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !LocationPermissionUtils.isBackgroundPermissionGranted(this)
            ) {
                showPermissionExplanationDialog()
            } else {
                checkAndProceed()
            }
        } else {
            Log.d("tekshirish", "checkBackgroundLocationPermission: enable")

            requestEnableLocation()
        }
    }

    private fun requestFineLocation() {
//        if (!LocationPermissionUtils.isLocationEnabled(this)) {
//            requestEnableLocation()
//        } else
            if (!LocationPermissionUtils.isBasicPermissionGranted(this)) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
            //        else {
//            checkAndProceed()
//        }
    }

    override fun onResume() {
        super.onResume()
        updateStatusImages()
        checkAndProceed()
    }

    private fun checkAndAsk() {
        if (!LocationPermissionUtils.isLocationEnabled(this)) {
            requestEnableLocation()
        } else {
            when {
                checkPermissions() -> handlePermissionsGranted()
                shouldShowRationalePermission() -> showPermissionExplanationDialog()
                else -> requestPermissions()
            }
        }
    }

    private fun checkPermissions(): Boolean =
        LocationPermissionUtils.isBasicPermissionGranted(this) &&
                LocationPermissionUtils.isBackgroundPermissionGranted(this) &&
                Settings.canDrawOverlays(this) &&
                LocationPermissionUtils.isPowerSavingModeEnabled(this)

    private fun shouldShowRationalePermission(): Boolean =
        LocationPermissionUtils.shouldShowRationaleBasicPermission(this)

    private fun requestPermissions() {
        when {
            !LocationPermissionUtils.isBasicPermissionGranted(this) -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !LocationPermissionUtils.isBackgroundPermissionGranted(this) -> {
                Log.d("tekshirish", "requestPermissions: ")
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            !Settings.canDrawOverlays(this) -> {
                showOverlayPermissionDialog()
            }

            !LocationPermissionUtils.isPowerSavingModeEnabled(this) -> {
                showBatteryPermissionDialog()
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_ask_permission)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.CENTER)
            }
        }

        dialog.findViewById<MaterialButton>(R.id.button_permissions).setOnClickListener {
            requestPermissions()
            dialog.dismiss()
        }

        dialog.findViewById<MaterialButton>(R.id.button_skip).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.neеded_permission))
            .setMessage(getString(R.string.draw_permission))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
            .create()
            .show()
    }

    private fun showBatteryPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.neеded_permission))
            .setMessage(getString(R.string.battery_permission))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                requestIgnoreBatteryOptimizations()
            }
            .create()
            .show()
    }

    private fun updateStatusImages() {
        updateImageView(
            viewBinding.imgBattery,
            LocationPermissionUtils.isPowerSavingModeEnabled(this)
        )
        updateImageView(viewBinding.imgOverlay, Settings.canDrawOverlays(this))
        updateImageView(
            viewBinding.imgLocation,
            LocationPermissionUtils.isBasicPermissionGranted(this) && LocationPermissionUtils.isLocationEnabled(
                this
            )
        )
        updateImageView(
            viewBinding.imgGps,
            LocationPermissionUtils.isBackgroundPermissionGranted(this)
        )
    }

    private fun updateImageView(imageView: ImageView, isActive: Boolean) {
        val paddingInPx = (6 * resources.displayMetrics.density + 0.5f).toInt()

        imageView.apply {
            setImageResource(if (isActive) R.drawable.ic_check else R.drawable.ic_cancel_settings)
            setPadding(
                if (isActive) 0 else paddingInPx,
                if (isActive) 0 else paddingInPx,
                if (isActive) 0 else paddingInPx,
                if (isActive) 0 else paddingInPx
            )
        }
    }

    private fun handlePermissionsGranted() {
        if (!LocationPermissionUtils.isLocationEnabled(this)) {
            requestEnableLocation()
        } else if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog()
        } else if (!LocationPermissionUtils.isPowerSavingModeEnabled(this)) {
            showBatteryPermissionDialog()
        } else {
            proceed()
        }
    }

    private fun handleLocationPermissionGranted() {
        if (checkPermissions()) {
            proceed()
        } else {
            requestPermissions()
        }
    }

    private fun handleLocationSettingsEnabled() {
        LocationPermissionUtils.compute(this)
        finish()
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        if (!LocationPermissionUtils.isPowerSavingModeEnabled(this)) {
            val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:$packageName"))
            batteryOptimizationLauncher.launch(intent)
        }
    }

    private fun requestEnableLocation() {
        LocationPermissionUtils.askEnableLocationRequest(this) { locationEnabled ->
            if (locationEnabled) checkAndAsk()
        }
    }

    private fun checkAndProceed() {
        if (checkPermissions()) {
            proceed()
        }
    }

    private fun proceed() {
        LocationPermissionUtils.compute(this)
        setResult(RESULT_OK)
        finish()
    }

    private fun showPermissionDeniedDialog(permission: String) {
//        AlertDialog.Builder(this)
//            .setTitle(getString(R.string.permission_denied))
//            .setMessage(getString(R.string.ruxsat_berilmagan, permission))
//            .setPositiveButton(R.string.go_to_settings) { _, _ ->
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                intent.data = Uri.parse("package:$packageName")
//                startActivity(intent)
//            }
//            .create()
////            .show()
    }

    companion object {
        private const val IS_PROMPT_MODE = "IS_PROMPT_MODE"

        fun getOpenIntent(context: Context, isPromptMode: Boolean = false): Intent {
            return Intent(context, PermissionCheckActivity::class.java).apply {
                putExtra(IS_PROMPT_MODE, isPromptMode)
            }
        }
    }
}

