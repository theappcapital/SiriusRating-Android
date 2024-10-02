package com.theappcapital.siriusrating.support.versionproviders

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

class PackageInfoCompatAppVersionProvider(private val context: Context) : AppVersionProvider {

    override val appVersion: String
        get() {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)

            return PackageInfoCompat.getLongVersionCode(packageInfo).toString()
        }

}