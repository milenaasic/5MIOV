package app.adinfinitum.ello.ui.myapplication

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.data.*
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

    val repo:Repo
        get() = ServiceLocator.getRepo(this)

    val repoContacts:RepoContacts
        get() = ServiceLocator.getRepoContacts(this)

    val repoSIPE1:RepoSIPE1
        get()=ServiceLocator.getRepoSIPE1(this)

    val repoUser:RepoUser
        get()=ServiceLocator.getRepoUser(this)

    val repoPrenumberAndWebApiVer:RepoPrenumberAndWebApiVer
        get()=ServiceLocator.getRepoPrenumberAndWebApiVer(this)

    val repoRecentCalls:RepoRecentCalls
        get()=ServiceLocator.getRepoRecentCalls(this)

    val repoProvideContacts:RepoProvideContacts
        get()=ServiceLocator.getRepoProvideContacts(this)

    val repoRemoteDataSource:RepoRemoteDataSource
        get()=ServiceLocator.getRepoRemoteDataSource(this)

    val repoLogToServer:RepoLogToServer
        get()=ServiceLocator.getRepoLogToServer(this)

    val repoLogOut:RepoLogOut
        get()=ServiceLocator.getRepoLogOut(this)


    val mobileAppVersion:String
        get()=ServiceLocator.getMobAppVersion(this)

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ACRA.init(this)
    }


}

object ServiceLocator{

    @Volatile
    var repo:Repo?=null

    @Volatile
    var repoContacts:RepoContacts?=null

    @Volatile
    var repoSIPE1:RepoSIPE1?=null



    @Volatile
    var repoUser:RepoUser?=null

    @Volatile
    var repoPrenumberAndWebApiVer:RepoPrenumberAndWebApiVer ?=null

    @Volatile
    var repoRecentCalls:RepoRecentCalls ?=null

    @Volatile
    var repoProvideContacts:RepoProvideContacts ?=null

    @Volatile
    var repoRemoteDataSource:RepoRemoteDataSource ?=null

    @Volatile
    var repoLogToServer:RepoLogToServer ?=null

    @Volatile
    var repoLogOut:RepoLogOut?=null



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


    fun getRepoUser(context:Context):RepoUser{
        synchronized(this){
            return repoUser?: creteRepoUser(context)
        }
    }

    fun getRepoPrenumberAndWebApiVer(context:Context):RepoPrenumberAndWebApiVer{
        synchronized(this){
            return repoPrenumberAndWebApiVer?: creteRepoPrenumberAndWebApiVer(context)
        }
    }

    fun getRepoRecentCalls(context:Context):RepoRecentCalls{
        synchronized(this){
            return repoRecentCalls?: creteRepoRecentCalls(context)
        }
    }

    fun getRepoProvideContacts(context:Context):RepoProvideContacts{
        synchronized(this){
            return repoProvideContacts?: creteRepoProvideContacts(context)
        }
    }

    fun getRepoRemoteDataSource(context:Context):RepoRemoteDataSource{
        synchronized(this){
            return repoRemoteDataSource?: creteRepoRemoteDataSource(context)
        }
    }

    fun getRepoLogToServer(context:Context):RepoLogToServer{
        synchronized(this){
            return repoLogToServer?: creteRepoLogToServer(context)
        }
    }

    fun getRepoLogOut(context:Context):RepoLogOut{
        synchronized(this){
            return repoLogOut?: creteRepoLogOut(context)
        }
    }


    private fun creteRepoProvideContacts(context: Context): RepoProvideContacts {
       return RepoProvideContacts(contentResolver = context.contentResolver)
    }



    private fun creteRepoRecentCalls(context: Context): RepoRecentCalls {
        return RepoRecentCalls(provideDatabaseRecentCalls(context))
    }

    private fun creteRepoPrenumberAndWebApiVer(context: Context): RepoPrenumberAndWebApiVer {
        return RepoPrenumberAndWebApiVer(provideDatabasePrenumberAndWebApiVersion(context = context))
    }

    private fun creteRepoLogOut(context: Context): RepoLogOut {
        return RepoLogOut(context)
    }


    private fun creteRepoRemoteDataSource(context: Context): RepoRemoteDataSource {
        return RepoRemoteDataSource(provideNetworkDataService(context))
    }

    private fun creteRepoLogToServer(context: Context): RepoLogToServer {
    return RepoLogToServer(provideDatabaseUser(context), provideNetworkLogService(context))
    }
    private fun creteRepoSIPE1(context: Context): RepoSIPE1 {
        return RepoSIPE1(provideDatabase(context), provideNetworkService(context))
    }

    private fun creteRepoContacts(context: Context): RepoContacts {
        return RepoContacts(contentResolver = context.contentResolver, provideDatabase(context),provideNetworkService(context))
    }

    private fun creteRepo(context: Context): Repo {
        return Repo(provideDatabase(context),provideNetworkService(context))
    }


    private fun creteRepoUser(context: Context): RepoUser {
        return RepoUser(provideDatabaseUser(context))
    }


    private fun provideDatabaseUser(context: Context)=MyDatabase.getInstance(context).myDatabaseUser
    private fun provideDatabasePrenumberAndWebApiVersion(context: Context)=MyDatabase.getInstance(context).myDatabasePrenumberAndWebApiVersion
    private fun provideDatabaseRecentCalls(context: Context)=MyDatabase.getInstance(context).myDatabaseRecentCalls

    // to be deleted
    private fun provideDatabase(context: Context)=MyDatabase.getInstance(context).myDatabaseDao



    private fun provideNetworkService(context: Context):MyAPIDataService{
        MyAPI.mobileAppVersion= getMobAppVersion(context)
        return MyAPI.myAPIDataService
    }

    private fun provideNetworkDataService(context: Context): MyAPIDataService{
        MyAPI.mobileAppVersion= getMobAppVersion(context)
        return MyAPI.myAPIDataService
    }

    private fun provideNetworkLogService(context: Context): MyAPILogToServer {
        MyAPI.mobileAppVersion= getMobAppVersion(context)
        return MyAPI.myAPILogToServerService
    }

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

