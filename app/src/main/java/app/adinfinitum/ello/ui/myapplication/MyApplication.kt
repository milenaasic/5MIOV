package app.adinfinitum.ello.ui.myapplication

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.annotation.*
import org.acra.annotation.AcraHttpSender
import org.acra.data.StringFormat
import org.acra.sender.HttpSender



@AcraCore(buildConfigClass = BuildConfig::class,
        reportFormat = StringFormat.JSON

        )
@AcraHttpSender(uri = "https://5miov.vertial.net/api/mobileLog",
                httpMethod = HttpSender.Method.POST,
                basicAuthLogin = "5miov",
                basicAuthPassword = ("tester")
                )
class MyApplication : Application() {
private val MYTAG="MY_ApplicationContext"

    val mobileAppVersion:String by lazy {
        getMobAppVersion()
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ACRA.init(this)
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