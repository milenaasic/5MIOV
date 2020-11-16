package app.adinfinitum.ello.ui.myapplication

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.api.MyAPIService
import app.adinfinitum.ello.data.Repo
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.RepoSIPE1
import app.adinfinitum.ello.database.MyDatabase
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


@AcraCore(buildConfigClass = BuildConfig::class,
        reportFormat = StringFormat.JSON
        )
class MyApplication : Application() {
    private val MYTAG="MY_ApplicationContext"

    val applicationScope by lazy {
        ProcessLifecycleOwner.get().lifecycleScope

        //CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }


    /*private var mTimer: Timer=Timer("Application object")
    val lTask: TimerTask = object : TimerTask() {
        override fun run() {
            Log.i(MYTAG,"App is alive, ${System.currentTimeMillis()}")
        }
    }*/

    val myContainer:MyContainer by lazy {
        MyContainer(this)
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ACRA.init(this)
    }


    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycleScope
       // mTimer.schedule(lTask, 0, 5000)
    }
}

class MyContainer(val application: MyApplication){

    private val myRetrofitService= MyAPI.retrofitService
    private val myDatabase=MyDatabase.getInstance(application).myDatabaseDao
    private val myContentProvider=application.contentResolver
    val myMobileAppVersion=getMobAppVersion()

    val repo =Repo(myDatabase,myRetrofitService,myMobileAppVersion)
    val repoContacts=RepoContacts(myContentProvider,myDatabase,myRetrofitService,myMobileAppVersion)
    val repoSIPE1=RepoSIPE1(myDatabase,myRetrofitService,myMobileAppVersion)

    private fun getMobAppVersion():String{
        var myversionName=""
        try {
            val packageInfo: PackageInfo = application.packageManager.getPackageInfo(application.packageName, 0);
            myversionName = packageInfo.versionName

        } catch ( e:Throwable) {
            e.printStackTrace();
        }

        return myversionName
    }

}