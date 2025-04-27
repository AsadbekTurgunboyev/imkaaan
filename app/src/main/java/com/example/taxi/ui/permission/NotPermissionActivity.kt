package com.example.taxi.ui.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.taxi.databinding.ActivityNotPermissionBinding
import com.example.taxi.domain.model.checkAccess.NotPermissionApkModel
import com.example.taxi.ui.splash.PrivacyCheckActivity

val packagesToCheck = listOf<String>()
//    "uz.royaltaxi.driver",
//    "ru.taxi.id2788"
//)
class NotPermissionActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityNotPermissionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityNotPermissionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        checkAppsInstalled(packagesToCheck)
    }


    override fun onResume() {
        super.onResume()
        if (!isAppsInstalled(packagesToCheck)){
            PrivacyCheckActivity.open(this)
            finish()
        }
    }

    private fun isAppsInstalled(packageNames: List<String>): Boolean {
        var allAppsInstalled = false

        packageNames.forEach { packageName ->
            if (isAppInstalled(this, packageName)) {
                allAppsInstalled = true

            }
        }

        return allAppsInstalled
    }

    private fun checkAppsInstalled(
        packageNames: List<String>
    ) {
        val list = arrayListOf<NotPermissionApkModel>()
        packageNames.forEachIndexed { index, packageName ->
            if (isAppInstalled(this, packageName)) {

                val appIcon = getAppIcon(this, packageName)

                val appName = getAppName(this,packageName)
                Log.d("tekshirish", "checkAppsInstalled: $appName")
                list.add(NotPermissionApkModel(appName,appIcon))

            }
        }
        viewBinding.appRv.adapter = PermissionAdapter(list)
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun getAppName(context: Context, packageName: String): String?{
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "Aniqlanmadi"
        }
    }

    companion object {
        fun open(activity: Activity) {
            val intent = Intent(activity, NotPermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }

}
