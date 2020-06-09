package com.vertial.fivemiov.ui.myapplication

import android.app.Application
import android.app.job.JobInfo
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.annotation.AcraCore
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.HttpSenderConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.sender.HttpSender


class MyApplication : Application() {
private val MYTAG="MY_ApplicationContext"

    val mobileAppVersion:String by lazy {
        getMobAppVersion()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val builder = CoreConfigurationBuilder(this)
            .setBuildConfigClass(BuildConfig::class.java)
            .setReportFormat(StringFormat.JSON)



        builder.getPluginConfigurationBuilder(
            HttpSenderConfigurationBuilder::class.java
        )
            .setUri("https://5miov.vertial.net/api/mobileLog")
            .setHttpMethod(HttpSender.Method.POST)
            .setBasicAuthLogin("5miov")
            .setBasicAuthPassword("tester")
            .setEnabled(true)
        /*builder.getPluginConfigurationBuilder(SchedulerConfigurationBuilder::class.java)
            .setRequiresNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setEnabled(true)
        builder.getPluginConfigurationBuilder(LimiterConfigurationBuilder::class.java)
            .setEnabled(true)*/

        ACRA.init(this, builder)
    }

    private fun getMobAppVersion():String{

        var myversionName=""
        var versionCode=-1L

        try {
            val packageInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0);
            myversionName = packageInfo.versionName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode=packageInfo.longVersionCode
            }else{
                versionCode= packageInfo.versionCode.toLong()

            }
        } catch ( e:Throwable) {
            Log.i(MYTAG,"package manager $e")
            e.printStackTrace();
        }

        return myversionName
    }

}