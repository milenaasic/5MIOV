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
@AcraHttpSender(uri = "https://5miov.vertial.net/api/mobileLog",
    httpMethod = HttpSender.Method.POST,
    basicAuthLogin = "5miov",
    basicAuthPassword = ("tester")
)
class MyApplication : Application() {
    private val MYTAG="MY_ApplicationContext"

    val applicationScope by lazy {
        ProcessLifecycleOwner.get().lifecycleScope
        //CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    /*val myContainer:MyContainer by lazy {
        MyContainer(this)
    }*/


    val repo:Repo
        get() = ServiceLocator.getRepo(this)

    val repoContacts:RepoContacts
        get() = ServiceLocator.getRepoContacts(this)

    val repoSIPE1:RepoSIPE1
        get()=ServiceLocator.getRepoSIPE1(this)

    val mobileAppVersion:String
    get() = ServiceLocator.getMobAppVersion(this)


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ACRA.init(this)
    }


}

object ServiceLocator{

    val myRetrofitService= MyAPI.retrofitService

    @Volatile
    var repo:Repo?=null

    @Volatile
    var repoContacts:RepoContacts?=null

    @Volatile
    var repoSIPE1:RepoSIPE1?=null

    fun getRepo(context:Context):Repo{
        synchronized(this){
            return repo?: creteRepo(context)
        }
    }

    fun getRepoContacts(context: Context):RepoContacts{
        synchronized(this){
            return repoContacts?: creteRepoContacts(context)
        }

    }

    fun getRepoSIPE1(context: Context):RepoSIPE1{
        synchronized(this){
            return repoSIPE1?: creteRepoSIPE1(context)
        }

    }

    private fun creteRepoSIPE1(context: Context): RepoSIPE1 {
        return RepoSIPE1(provideDatabase(context),myRetrofitService, getMobAppVersion(context = context))
    }

    private fun creteRepoContacts(context: Context): RepoContacts {
        return RepoContacts(contentResolver = context.contentResolver, provideDatabase(context),myRetrofitService, getMobAppVersion(context = context))
    }

    private fun creteRepo(context: Context): Repo {
        return Repo(provideDatabase(context),myRetrofitService, getMobAppVersion(context = context))
    }

    private fun provideDatabase(context: Context)=MyDatabase.getInstance(context).myDatabaseDao


    fun getMobAppVersion(context: Context):String{
        var myversionName=""
        try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0);
            myversionName = packageInfo.versionName

        } catch ( e:Throwable) {
            e.printStackTrace();
        }

        return myversionName
    }

}


/*class MyContainer(val application: MyApplication){

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

}*/