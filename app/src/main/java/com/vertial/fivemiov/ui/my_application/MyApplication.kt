package com.vertial.fivemiov.ui.my_application

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.api.MyAPIService
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.data.RepoSIPE1
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.database.MyDatabaseDao
import retrofit2.Retrofit
import java.security.AccessControlContext

class MyApplication: Application() {
    private val MYTAG="MY_Application"

  lateinit var myAppContainer:AppContainer

    override fun onCreate() {
        super.onCreate()
        myAppContainer= AppContainer(this)
    }
}

interface Provider{
    fun provideDetailContactViewModelFactory()
}

class AppContainer(val context: MyApplication):Provider{
    private val MYTAG="MY_AppContainer"
    //val ctx=context as Application

    val myDBDao:MyDatabaseDao=MyDatabase.getInstance(context).myDatabaseDao

    val myApi:MyAPIService= MyAPI.retrofitService

    val contactsRepo:RepoContacts by lazy { RepoContacts(context.contentResolver,myDBDao,myApi)}
    val repo: Repo by lazy {  Repo(myDBDao,myApi)}
    val sipRepo:RepoSIPE1 by lazy {RepoSIPE1(myDBDao,myApi)}

    override fun provideDetailContactViewModelFactory() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

class NetworkAvailabilityInfoProvider(val context: Application){

    val isWiFiAvailable= MutableLiveData<Boolean>()

    val connMgr =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkRequestWiFi = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    val networkRequestCelular = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    fun registerCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            connMgr.registerNetworkCallback(networkRequestWiFi,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                            isWiFiAvailable.value=true
                    }

                    override fun onLost(network: Network) {
                        isWiFiAvailable.value=false
                    }
                })

        }
    }

}
