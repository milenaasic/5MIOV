package app.adinfinitum.ello.ui.myapplication

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.annotation.*
import org.acra.annotation.AcraHttpSender
import org.acra.data.StringFormat
import org.acra.sender.HttpSender
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext


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

    val applicationScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }


    //todo remove timer
    private var mTimer: Timer=Timer("Application object")
    val lTask: TimerTask = object : TimerTask() {
        override fun run() {
            Log.i(MYTAG,"App is alive, ${System.currentTimeMillis()}")
        }
    }

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



    override fun onCreate() {
        super.onCreate()
        mTimer.schedule(lTask, 0, 5000)
    }
}