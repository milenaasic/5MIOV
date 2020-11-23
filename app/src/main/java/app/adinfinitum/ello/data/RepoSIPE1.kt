package app.adinfinitum.ello.data

import android.util.Log
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import kotlinx.coroutines.*
import kotlin.Exception


private const val MYTAG="MY_SIP_REPO"

class RepoSIPE1 (val myDatabaseDao: MyDatabaseDao, val myAPI: MyAPIService,val mobileAppVer:String="0.0"):LogStateOrErrorToServer{

    fun getUserNoLiveData()=myDatabaseDao.getUserNoLiveData()

    suspend fun getSipAccessCredentials(token: String,phone: String):Result<NetResponse_GetSipAccessCredentials>{
        try {
            val response=myAPI.getSipAccess(
                    phoneNumber = phone,
                    signature = produceJWtToken(
                        Pair(Claim.TOKEN.myClaim,token),
                        Pair(Claim.PHONE.myClaim,phone)
                    ),
                    mobileAppVersion = mobileAppVer,
                    request = NetRequest_GetSipAccessCredentials(authToken = token,phoneNumber = phone)
                    ).await()

            return Result.Success(response)

        }catch (e:Exception){

            return Result.Error(e)

        }
    }


    suspend fun resetSipAccess(
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
            Log.i(MYTAG, "resetSipAccess $result")
        } catch (e: Throwable) {

        }
    }



    //log errors and states
    suspend fun logStateOrErrorToOurServer(phoneNumber: String="",myoptions:Map<String,String>){
        logStateOrErrorToOurServer(phoneNumber=phoneNumber,myDatabaseDao=myDatabaseDao,myAPIService = myAPI,myoptions = myoptions)

    }



}
