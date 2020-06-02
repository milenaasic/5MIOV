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

    //set sharedPref TO false because of Log out
    private val _loggingOut= MutableLiveData<Boolean>()
    val loggingOut: LiveData<Boolean>
        get() = _loggingOut


    //reset Logging Out
    fun resetLoggingOutToFalse(){
        _loggingOut.value=false
    }

    fun resetSipAccessInBackGround(){
        val myJobSip=UncancelableJobSip(myDatabaseDao,myAPI)
        GlobalScope.launch {
                withContext(Dispatchers.IO){
                    myJobSip.doJob()

                }
         }

    }


    suspend fun getUserNoLiveData()=myDatabaseDao.getUserNoLiveData()

    suspend fun getSipAccessCredentials(token: String,phone: String){
        val defResponse=myAPI.getSipAccess(
                phoneNumber = phone,
                signature = produceJWtToken(
                    Pair(Claim.TOKEN.myClaim,token),
                    Pair(Claim.PHONE.myClaim,phone)
                ),
                request = NetRequest_GetSipAccessCredentials(authToken = token,phoneNumber = phone)
                )
        try {
            val response=defResponse.await()
            if(response.authTokenMismatch==true) {
                _loggingOut.value=true
                coroutineScope {
                    withContext(Dispatchers.IO) {
                        logoutAll(myDatabaseDao)
                    }
                }
            }
            else _getSipAccessCredentialsNetSuccess.value=response

        }catch (e:Throwable){
            Log.i(MYTAG," getSipAccess greska ${e.message}")
            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    SendErrorrToServer(myAPI,phone,"getSipAccessCredentials $phone, $token",e.message.toString()).sendError()
                } }

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
        Log.i(MY_TAG, "usao u doJOb")
        val deferredUser = GlobalScope.async(Dispatchers.IO) {
            //delay(3000)
            myDatabaseDao.getUserNoLiveData()
        }
        try {
            val myUser = deferredUser.await()
            if (myUser.userToken.isNotEmpty() && myUser.userPhone.isNotEmpty()) resetSipAccess(myUser.userToken,myUser.userPhone)

        } catch (e: Exception) {
            Log.i(MY_TAG, "db greska reset sip credentials ${e.message}")

        }

    }


    private suspend fun resetSipAccess(
        authToken: String,
        phone: String
    ) {
        Log.i(MY_TAG, "usao u resetSipAccess")
        val defResult = myAPI.resetSipAccess(
                phoneNumber = phone,
                signature = produceJWtToken(
                    Pair(Claim.TOKEN.myClaim,authToken),
                    Pair(Claim.PHONE.myClaim,phone),
                    Pair(Claim.FORCE_RESET.myClaim, CLAIM_VALUE_1)
                ),
                request = NetRequest_ResetSipAccess(authToken=authToken,phoneNumber = phone)
                )
        try {
            val result = defResult.await()

        } catch (e: Throwable) {
            Log.i(MY_TAG, "greska resetSipAccess ${e.message}")
            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    SendErrorrToServer(myAPI,phone,"resetSipAccess $phone, $authToken",e.message.toString()).sendError()
                } }
        }
    }

}
