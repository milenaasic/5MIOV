package com.vertial.fivemiov.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.api.*
import com.vertial.fivemiov.database.MyDatabaseDao
import kotlinx.coroutines.*
import java.lang.Exception


private const val MYTAG="MY_SIP_REPO"

class RepoSIPE1 (val myDatabaseDao: MyDatabaseDao, val myAPI: MyAPIService,val mobileAppVer:String="0.0"){


    private val _getSipAccessCredentialsNetSuccess= MutableLiveData<NetResponse_GetSipAccessCredentials?>()
    val getSipAccessCredentialsNetSuccess: LiveData<NetResponse_GetSipAccessCredentials?>
        get() = _getSipAccessCredentialsNetSuccess

    private val _getSipAccessCredentialsNetError= MutableLiveData<String?>()
    val getSipAccessCredentialsNetError: LiveData<String?>
        get() = _getSipAccessCredentialsNetError

    //set sharedPref to false because of Log out
    private val _loggingOut= MutableLiveData<Boolean>()
    val loggingOut: LiveData<Boolean>
        get() = _loggingOut


    //reset Logging Out
    fun resetLoggingOutToFalse(){
        _loggingOut.value=false
    }

    fun resetSipAccessInBackGround(){
        val myJobSip=UncancelableJobSip(myDatabaseDao,myAPI,mobileAppVer)
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
                mobileAppVersion = mobileAppVer,
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

    fun logCredentialsForSipCall(sipUsername:String?,sipPassword:String?,sipDisplayname:String?,sipServer:String?){
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                val def=myAPI.sendErrorToServer(phoneNumber = "$sipUsername",process="make sip call function",
                    errorMsg= "credentials: $sipUsername,$sipPassword,$sipDisplayname,$sipServer")
                try {
                    val defResponse=def.await()
                }catch (t:Throwable){
                    Log.i("MY_Send Error To Server","${t.message}")

                }
            } }

    }

}

class UncancelableJobSip(val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService,val mobileAppVer: String) {

    val MY_TAG = "MY_SIP_UNCANCELABLE_JOB"

    suspend fun doJob() {

        val deferredUser = GlobalScope.async(Dispatchers.IO) {
            myDatabaseDao.getUserNoLiveData()
        }
        try {
            val myUser = deferredUser.await()
            if (myUser.userToken.isNotEmpty() && myUser.userPhone.isNotEmpty()) resetSipAccess(myUser.userToken,myUser.userPhone)

        } catch (e: Exception) {
            Log.i(MY_TAG, "DB error ${e.message}")

        }

    }


    private suspend fun resetSipAccess(
        authToken: String,
        phone: String
    ) {

        val defResult = myAPI.resetSipAccess(
                phoneNumber = phone,
                signature = produceJWtToken(
                    Pair(Claim.TOKEN.myClaim,authToken),
                    Pair(Claim.PHONE.myClaim,phone),
                    Pair(Claim.FORCE_RESET.myClaim, CLAIM_VALUE_1)
                ),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_ResetSipAccess(authToken=authToken,phoneNumber = phone)
                )
        try {
            val result = defResult.await()

        } catch (e: Throwable) {

            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    SendErrorrToServer(myAPI,phone,"resetSipAccess $phone, $authToken",e.message.toString()).sendError()
                } }
        }
    }

    //Log state to our server
    fun logStateToServer(process:String="No_Process_Defined",state:String="No_State_Defined"){
        GlobalScope.launch {
            with(Dispatchers.IO){
                val phoneNumberDef=async {
                    myDatabaseDao.getPhone()
                }
                try {
                    val phoneNumber=phoneNumberDef.await()
                    SendErrorrToServer(myAPIService = myAPI,phoneNumber = phoneNumber,
                        process = process,errorMsg = state).sendError()

                }catch (t:Throwable){
                    Log.i(MY_TAG, "error in logStateToServer ${t.message}")

                }
            }
        }

    }


}
