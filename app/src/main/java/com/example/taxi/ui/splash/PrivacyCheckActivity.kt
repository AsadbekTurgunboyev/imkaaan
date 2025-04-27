package com.example.taxi.ui.splash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.taxi.domain.model.FORBIDDEN_ERROR_MESSAGE
import com.example.taxi.domain.model.checkAccess.AccessModel
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.HomeActivity
import com.example.taxi.ui.login.LoginActivity
import com.example.taxi.ui.permission.NotPermissionActivity
import com.example.taxi.ui.permission.PermissionCheckActivity
import com.example.taxi.ui.permission.packagesToCheck
import com.example.taxi.ui.update.UpdateActivity
import com.example.taxi.utils.LocationPermissionUtils
import com.example.taxi.utils.ResourceState
import com.example.taxi.utils.ViewUtils
import com.jaredrummler.android.device.DeviceName
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class PrivacyCheckActivity : AppCompatActivity() {
    private val viewModel: SplashViewModel by viewModel()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var app_detail = ""
    private val permissionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (allPermissionsAndSettingsGranted()) {
                nextActivity()
            }
        }
    private val userPreferenceManager: UserPreferenceManager by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewUtils.setTheme(userPreferenceManager.getTheme())
        checkAccess()


    }

    private fun allPermissionsAndSettingsGranted(): Boolean {
        // Your logic to check if all required permissions and settings are granted/enabled
        return LocationPermissionUtils.isBasicPermissionGranted(this)
                && LocationPermissionUtils.isBackgroundPermissionGranted(this)
                && LocationPermissionUtils.isLocationEnabled(this)
                && LocationPermissionUtils.isPowerSavingModeEnabled(this)
                && Settings.canDrawOverlays(this)
    }

    private fun checkAccess() {
        val manufacture = Build.MANUFACTURER
        var versionCode: Long = 0
        var versionName: String = "1.0"


        try {
            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                pInfo.versionCode.toLong()
            }
            versionName = pInfo.versionName

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val m = DeviceName.getDeviceInfo(this).manufacturer
        val infoName = DeviceName.getDeviceInfo(this).name
        val deviceName = "$m $infoName"       // "Galaxy S8+"

        app_detail = if (m == manufacture) deviceName else manufacture

        val checkAccess = AccessModel(
            app_version = versionCode.toInt(),
            app_version_name = versionName,
            phone_model = app_detail
        )
        viewModel.checkAccess(checkAccess)

    }

    override fun attachBaseContext(newBase: Context) {
        val newContext = ViewUtils.setLanguage(newBase, userPreferenceManager.getLanguage())
        super.attachBaseContext(newContext)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, start the foreground service
                if (allPermissionsAndSettingsGranted()) {
                    nextActivity()
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (checkAppsInstalled(packagesToCheck)) {
            NotPermissionActivity.open(this)
        } else {
            if (allPermissionsAndSettingsGranted()) {
                nextActivity()
            } else {
                permissionActivityResultLauncher.launch(
                    PermissionCheckActivity.getOpenIntent(
                        context = this,
                        isPromptMode = false
                    )
                )
            }
        }


    }

    private fun continueDeleteActivity() {
        NotPermissionActivity.open(this)
        finish()
    }

    private fun checkAppsInstalled(packageNames: List<String>): Boolean {
        var allAppsInstalled = false

        packageNames.forEach { packageName ->
            if (isAppInstalled(this, packageName)) {
                allAppsInstalled = true

            }
        }

        return allAppsInstalled
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    override fun onResume() {
        super.onResume()

        if (allPermissionsAndSettingsGranted()) {
            nextActivity()
        }

    }


    private fun nextActivity() {
        if (userPreferenceManager.getRegisterComplete()) {
            if (userPreferenceManager.getDriverStatus() != UserPreferenceManager.DriverStatus.COMPLETED) {
                continueHome()
            } else {
                viewModel.checkAccess.removeObservers(this)
                viewModel.checkAccess.observe(this) {
                    when (it.state) {
                        ResourceState.ERROR -> {
                            if ((userPreferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.COMPLETED) && (it.message == FORBIDDEN_ERROR_MESSAGE)) {
                                continueUpdating()
                            } else {
                                continueHome()
                            }
                        }

                        ResourceState.SUCCESS -> {
                            continueHome()
                        }

                        ResourceState.LOADING -> {
                        }
                    }

                }
            }

        } else {
            continueLogin()
        }
    }

    private fun continueUpdating() {
        UpdateActivity.open(this)
        finish()
    }

    private fun continueLogin() {
        LoginActivity.open(this)
        finish()
    }

    private fun continueHome() {
        HomeActivity.open(this)
        finish()
    }

    companion object {
        fun open(activity: Activity) {
            val intent = Intent(activity, PrivacyCheckActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }

}