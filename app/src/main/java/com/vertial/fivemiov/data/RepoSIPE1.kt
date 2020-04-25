package com.vertial.fivemiov.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.api.*
import com.vertial.fivemiov.database.MyDatabaseDao
import kotlinx.coroutines.*
import java.lang.Exception


private const val MYTAG="MY_Sip_and_e1_repo"

class RepoSIPE1 (val myDatabaseDao: MyDatabaseDao, val myAPI: MyAPIService){


    private val _getSipAccessCredentialsNetSuccess= MutableLiveData<NetResponse_GetSipAccessCredentials?>()
    val getSipAccessCredentialsNetSuccess: LiveData<NetResponse_GetSipAccessCredentials?>
        get() = _getSipAccessCredentialsNetSuccess

    private val _getSipAccessCredentialsNetError= MutableLiveData<String?>()
    val getSipAccessCredentialsNetError: LiveData<String?>
        get() = _getSipAccessCredentialsNetError


    suspend fun resetSipAccess(token:String){

        val defResponse=myAPI.resetSipAccess(request = NetRequest_ResetSipAccess(authToken = token))
        try {
            val response=defResponse.await()
        }catch (e:Throwable){
            Log.i(MYTAG," ruta resetSipAccess, greska ${e.message}")
        }

    }

    fun resetSipAccessInBackGround(){
        val myJobSip=UncancelableJobSip(myDatabaseDao,myAPI)
        GlobalScope.launch {
                withContext(Dispatchers.IO){
                    myJobSip.doJob()

                }
         }

    }

    suspend fun setNewE1(token: String){

        val defResponse=myAPI.setNewE1(request = NetRequest_SetE1Prenumber(authToken = token))
        try {
            val response=defResponse.await()
        }catch (e:Throwable){
            Log.i(MYTAG," ruta setNewE1, greska ${e.message}")

        }

    }

    suspend fun getSipAccessCredentials(token: String){
        val defResponse=myAPI.getSipAccess(request = NetRequest_GetSipAccessCredentials(authToken = token))
        try {
            val response=defResponse.await()
            _getSipAccessCredentialsNetSuccess.value=response

        }catch (e:Throwable){
            Log.i(MYTAG," getSipAccess greska ${e.message}")
            _getSipAccessCredentialsNetError.value=e.message

        }
    }

    fun resetGetSipAccessCredentialsNetSuccess(){
        _getSipAccessCredentialsNetSuccess.value=null
    }

    fun resetGetSipAccessCredentialsNetError(){
        _getSipAccessCredentialsNetError.value=null
    }

}

class UncancelableJobSip(val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService) {

    val MY_TAG = "MY_klasaUncanceJObSIP"

    suspend fun doJob() {
        var authtoken = ""
        val deferredToken = GlobalScope.async(Dispatchers.IO) {
            //delay(3000)
            myDatabaseDao.getToken()
        }
        try {
            authtoken = deferredToken.await()

        } catch (e: Exception) {
            Log.i(MY_TAG, "db greska reset sip credentials ${e.message}")
        }

        if (authtoken.isNotEmpty()) resetSipAccess(authtoken)


    }


    private suspend fun resetSipAccess(
        authToken: String
    ) {
        Log.i(MY_TAG, "usao u resetSipAccess")
        val defResult = myAPI.resetSipAccess(request = NetRequest_ResetSipAccess(authToken))
        try {
            val result = defResult.await()
        } catch (e: Throwable) {
            Log.i(MY_TAG, "greska resetSipAccess ${e.message}")
        }
    }

    suspend fun callSetNewE1(token: String) {
        Log.i(MY_TAG, "usao u callSetNewE1")

        val defResult = myAPI.setNewE1(request = NetRequest_SetE1Prenumber(token))
        try {
            val result = defResult.await()
            if (!result.e1prenumber.isNullOrEmpty()) {
                myDatabaseDao.updatePrenumber(result.e1prenumber, System.currentTimeMillis())

            } else {
            }
        } catch (e: Throwable) {
            Log.i(MY_TAG, "greska ${e.message}")
        }


    }
}
