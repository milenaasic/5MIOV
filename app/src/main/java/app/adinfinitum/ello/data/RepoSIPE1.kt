package app.adinfinitum.ello.data

import android.util.Log
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import kotlinx.coroutines.*
import kotlin.Exception


private const val MYTAG="MY_SIP_REPO"

class RepoSIPE1 (val myDatabaseDao: MyDatabaseDao, val myAPI: MyAPIService,val mobileAppVer:String="0.0"){

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

            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    SendErrorrToServer(myAPI,phone,"getSipAccessCredentials $phone, $token",e.message.toString()).sendError()
                } }

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

            GlobalScope.launch {
                withContext(Dispatchers.IO){
                    SendErrorrToServer(myAPI,phone,"resetSipAccess $phone, $authToken",e.message.toString()).sendError()
                } }
        }
    }



    fun logCredentialsForSipCall(sipUsername:String?,sipPassword:String?,sipDisplayname:String?,sipServer:String?,stunServer:String?){
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                try {
                    myAPI.sendErrorToServer(phoneNumber = "$sipUsername",process="initializeCore function",
                        errorMsg= "credentials: $sipUsername,$sipPassword,$sipDisplayname,$sipServer,$stunServer")

                }catch (t:Throwable){
                    Log.i("MY_Send Error To Server","${t.message}")

                }
            } }

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
                    Log.i(MYTAG, "error in logStateToServer ${t.message}")

                }
            }
        }

    }


}
