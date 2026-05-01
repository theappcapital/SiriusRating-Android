package com.theappcapital.siriusrating.support.versionproviders

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

class PackageInfoCompatAppVersionProvider(context: Context) : AppVersionProvider {

    private val applicationContext: Context = context.applicationContext

    override val appVersion: String
        get() {
            val packageManager = applicationContext.packageManager
            val packageInfo = packageManager.getPackageInfo(applicationContext.packageName, 0)

            return PackageInfoCompat.getLongVersionCode(packageInfo).toString()
        }

}
