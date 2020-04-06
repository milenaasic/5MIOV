package com.vertial.fivemiov.ui.my_application

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.database.MyDatabaseDao
import retrofit2.Retrofit
import java.security.AccessControlContext

class MyApplication: Application() {
    val myAppContainer=AppContainer(this)
    val myNetworkAvailabilityInfoProvider=NetworkAvailabilityInfoProvider(this).run{
        registerCallback()
    }
}

interface Provider{
    fun provideDetailContactViewModelFactory()

}

class AppContainer(val context: Application):Provider{

    val myDBDao=MyDatabase.getInstance(context).myDatabaseDao
    val myApi=MyAPI.retrofitService

    val contactsRepoContacts:RepoContacts= RepoContacts(context.contentResolver,myDBDao,myApi)


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
