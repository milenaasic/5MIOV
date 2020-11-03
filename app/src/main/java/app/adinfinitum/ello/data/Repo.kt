package app.adinfinitum.ello.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService, val mobileAppVer:String="0.0"){

    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()


    // Registration fragment
    suspend fun sendRegistationToServer(phone:String,smsResend:Boolean,verificationMethod:String): Result<NetResponse_Registration> {

        val defResponse=myAPI.sendRegistrationToServer(
                phoneNumber = phone,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,phone)),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_Registration(phoneNumber = phone,verificationMethod = verificationMethod )
                )
        try{

            val result:NetResponse_Registration=defResponse.await()

            return Result.Success(result)

        } catch (e:Exception){
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"sendRegistationToServer,$phone,smsResend_$smsResend",e.message.toString()).sendError()
                } }

            return Result.Error(e)

        }

    }



    // ASSIGN NUMBER TO ACCOUNT
    suspend fun assignPhoneNumberToAccount(phone:String, email:String,password:String,smsResend: Boolean=false,verificationMethod: String):Result<NetResponse_AddNumberToAccount>{

        val defResponse=myAPI.sendAddNumberToAccount(
                phoneNumber = phone,
                signature = produceJWtToken(
                                Pair(Claim.NUMBER.myClaim,phone),
                                Pair(Claim.EMAIL.myClaim,email),
                                Pair(Claim.PASSWORD.myClaim,password)
                                ),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_AddNumberToAccount(
                        phoneNumber = phone,
                        email = email,
                        password = password,
                        verificationMethod = verificationMethod ))
        try{
            val result=defResponse.await()

            return Result.Success(result)

        }
        catch (e:Exception){

            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"assignPhoneNumberToAccount $phone, $email,$password smsResend $smsResend",e.message.toString()).sendError()
                } }

            return Result.Error(e)

        }

    }



    //NUMBER EXISTS IN DB  fragment
    suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber:String, email:String, password:String,smsResend: Boolean=false,verificationMethod: String):Result<NetResponse_NmbExistsInDB>{

        val defResult=myAPI.numberExistsInDBVerifyAccount(
                phoneNumber = enteredPhoneNumber,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,enteredPhoneNumber),
                                            Pair(Claim.EMAIL.myClaim,email),
                                            Pair(Claim.PASSWORD.myClaim,password),
                                            Pair(Claim.SIGN_IN.myClaim, CLAIM_VALUE_TRUE)
                ),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_NmbExistsInDB_UserHasAccount(
                    phoneNumber = enteredPhoneNumber,
                    email = email,
                    password = password,
                    verificationMethod = verificationMethod)
                )
        try{
            val result=defResult.await()

            return Result.Success(result)
        }
        catch (e:Exception){
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,enteredPhoneNumber,"numberExistsInDBVerifyAccount $enteredPhoneNumber $email,$password smsResend $smsResend",e.message.toString()).sendError()
                } }

                return Result.Error(e)

        }
    }


    suspend fun numberExistsInDB_NOAccount(enteredPhoneNumber:String,smsResend: Boolean=false,verificationMethod: String):Result<NetResponse_NmbExistsInDB>{

        val defResult=myAPI.numberExistsInDB_NOAccount(
                phoneNumber = enteredPhoneNumber,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,enteredPhoneNumber),
                                            Pair(Claim.SIGN_IN.myClaim, CLAIM_VALUE_FALSE)),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_NmbExistsInDB_NoAccount(phoneNumber = enteredPhoneNumber,verificationMethod = verificationMethod)
                )
        try{
            val result=defResult.await()

            return Result.Success(result)
        }
        catch (e:Exception){

            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,enteredPhoneNumber,"numberExistsInDB_NOAccount $enteredPhoneNumber, smsResend $smsResend",e.message.toString()).sendError()
                } }

            return Result.Error(e)
        }
    }


    //Authorization fragment
    suspend fun authorizeThisUser(phone:String,smsToken:String,email: String,password: String):Result<NetResponse_Authorization>{

        val defResponse=myAPI.authorizeUser(
                phoneNumber = phone,
                signature = produceJWtToken(
                                Pair(Claim.NUMBER.myClaim,phone),
                                Pair(Claim.TOKEN.myClaim,smsToken),
                                Pair(Claim.EMAIL.myClaim,email),
                                Pair(Claim.PASSWORD.myClaim,password)
                            ),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_Authorization(phoneNumber = phone,smstoken = smsToken,email = email,password = password )
                )
        try{
            val result=defResponse.await()

            /*if(result.success==true) {
                val uncancelableJob = UncancelableJob(
                    phone=phone,
                    resultAuthorization = result,
                    resultSetAccountEmailAndPass = null,
                    myDatabaseDao = myDatabaseDao,
                    myAPI = myAPI,
                    mobileAppVer = mobileAppVer
                )

                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        uncancelableJob.startAuthorizationJob()

                    }
                }

                /*withContext(Dispatchers.IO) {
                    if (result.email.isNotEmpty()
                        && result.email.isNotBlank()
                        && result.authToken.isNotEmpty()
                        && result.authToken.isNotBlank()
                        ) myDatabaseDao.updateUsersPhoneTokenEmail(phone, result.authToken, result.email)
                    else if(result.authToken.isNotEmpty() && result.authToken.isNotBlank()) myDatabaseDao.updateUsersPhoneAndToken(phone, result.authToken)
                }*/
            }*/

          return Result.Success(result)

        }
        catch (e:Exception){

            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"authorizeThisUser,$phone,$smsToken,$email,$password",e.message.toString()).sendError()
                } }

            return Result.Error(e)

        }

    }



    //SetAccountAndEmail Fragment - Main part of the app
    suspend fun  setAccountEmailAndPasswordForUser(phoneNumber:String,token: String,email: String,password: String):Result<NetResponse_SetAccountEmailAndPass>{

        val defResult=myAPI.setAccountEmailAndPasswordForUser(
            phoneNumber = phoneNumber,
            signature = produceJWtToken(
                Pair(Claim.NUMBER.myClaim,phoneNumber),
                Pair(Claim.AUTH_TOKEN.myClaim,token),
                Pair(Claim.EMAIL.myClaim,email),
                Pair(Claim.PASSWORD.myClaim,password)
            ),
            mobileAppVersion = mobileAppVer,
            request = NetRequest_SetAccountEmailAndPass(
                            phoneNumber=phoneNumber,authToken= token,email = email,password = password)
        )
        try {
            val result = defResult.await()
                Log.i(MY_TAG,"NetRequest_SetAccountEmailAndPass $result")
                /*if (result.success == true) {
                    val myUncancelableJob: UncancelableJob = UncancelableJob(
                        phone = phoneNumber,
                        resultAuthorization = null,
                        resultSetAccountEmailAndPass = result,
                        myDatabaseDao = myDatabaseDao,
                        myAPI = myAPI,
                        mobileAppVer = mobileAppVer
                    )

                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            myUncancelableJob.startSetAccountEmailAndPassUncancelableJob(token)
                        }
                    }

                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            if (result.email.isNotEmpty() && result.email.isNotBlank()) myDatabaseDao.updateUserEmail(result.email)
                        }
                    }

                }*/
                return Result.Success(result)

        }
        catch (e:Exception){
            Log.i(MY_TAG,"NetRequest_SetAccountEmailAndPass error ${e.message}")

            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phoneNumber,"setAccountEmailAndPasswordForUser $phoneNumber, $token, $email, $password",e.message.toString()).sendError()
                } }

            return Result.Error(e)

        }
    }


    // Reset Sip Access data on server
    suspend fun resetSipAccess(
        phone:String,
        authToken: String
    ) {
          try {
                myAPI.resetSipAccess(
                    phoneNumber = phone,
                    signature = produceJWtToken(
                        Pair(Claim.TOKEN.myClaim,authToken),
                        Pair(Claim.PHONE.myClaim,phone),
                        Pair(Claim.FORCE_RESET.myClaim, CLAIM_VALUE_1)
                    ),
                    mobileAppVersion = mobileAppVer,
                    request = NetRequest_ResetSipAccess(authToken=authToken,phoneNumber = phone)
                )

            }catch (e:Throwable){
                        SendErrorrToServer(myAPI,phone,"resetSIPAccess_from_uncancelable_job,$phone,$authToken",e.message.toString()).sendError()
            }

    }

    //get E1 prenumber
    suspend fun callSetNewE1(phone: String,token: String) {

        val defResult = myAPI.setNewE1(
            phoneNumber = phone,
            signature = produceJWtToken(
                Pair(Claim.TOKEN.myClaim,token),
                Pair(Claim.PHONE.myClaim,phone)
            ),
            mobileAppVersion = mobileAppVer,
            request = NetRequest_SetE1Prenumber(
                authToken = token,
                phoneNumber = phone
            )
        )
        try {
            val result = defResult.await()

            if (!result.e1prenumber.isNullOrEmpty() && !result.e1prenumber.isNullOrBlank()) {
                myDatabaseDao.updatePrenumber(result.e1prenumber, System.currentTimeMillis())
            }


        }catch (e:Throwable){

            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"setNewE1_from_uncancelable_job,$phone,$token",e.message.toString()).sendError()
                }
            }
        }

    }


    suspend fun getUser()=withContext(Dispatchers.IO){
        myDatabaseDao.getUserNoLiveData()

    }


    fun updateUsersPhoneTokenEmail(phone: String,token: String,email: String){
        myDatabaseDao.updateUsersPhoneTokenEmail(phoneNb = phone,token = token,email = email)
    }


    fun updateUsersPhoneAndToken (phone:String,token:String) {
        myDatabaseDao.updateUsersPhoneAndToken(phoneNb = phone,token =  token)
    }

    fun updateUserEmail (email: String){
        myDatabaseDao.updateUserEmail(email = email)
    }

    fun updatePrenumber(e1Phone:String, timestamp:Long){
        myDatabaseDao.updatePrenumber(prenumber = e1Phone,timestamp = timestamp)
    }

    fun updateWebApiVersion(webApiVer:String){
        myDatabaseDao.updateWebApiVersion(webApiVer =webApiVer )
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

/*class UncancelableJob(
                        val phone: String,
                        val resultAuthorization:NetResponse_Authorization?,
                        val resultSetAccountEmailAndPass:NetResponse_SetAccountEmailAndPass?,
                        val myDatabaseDao: MyDatabaseDao,
                        val myAPI: MyAPIService,
                        val mobileAppVer: String ){

    val MY_TAG="MY_UNCANCELABLE_JOB"

    suspend fun startAuthorizationJob(){

        if(resultAuthorization!=null){

            if(resultAuthorization.authToken.isNotEmpty() && resultAuthorization.authToken.isNotBlank()) {

                if(resultAuthorization.sipReady==false) resetSipAccess(resultAuthorization.authToken)

                if (resultAuthorization.e1phone.isNullOrEmpty() || resultAuthorization.e1phone.isBlank()) callSetNewE1(
                    phone = phone,
                    token = resultAuthorization.authToken
                )
                else {
                    myDatabaseDao.updatePrenumber(
                        resultAuthorization.e1phone,
                        System.currentTimeMillis()
                    )
                    Log.i(MY_TAG, "Authorize proccess- updating e1 phone version in DB")
                }

                if (!resultAuthorization.appVersion.isNullOrEmpty() && !resultAuthorization.appVersion.isNullOrBlank()) {
                    myDatabaseDao.updateWebApiVersion(resultAuthorization.appVersion)
                    Log.i(MY_TAG, "Authorize proccess- updating web api version in DB")
                }
            }
        }
    }


    suspend fun startSetAccountEmailAndPassUncancelableJob(token: String){

        if(resultSetAccountEmailAndPass!=null){

            if(resultSetAccountEmailAndPass.e1phone.isNullOrEmpty() || resultSetAccountEmailAndPass.e1phone.isNullOrBlank() ) callSetNewE1(phone=phone,token=token)
            else  { myDatabaseDao.updatePrenumber(resultSetAccountEmailAndPass.e1phone,System.currentTimeMillis())
                Log.i(MY_TAG, "Set account- updating e1 phone version in DB") }

            if(!resultSetAccountEmailAndPass.appVersion.isNullOrEmpty() || !resultSetAccountEmailAndPass.appVersion.isNullOrBlank()) {
                myDatabaseDao.updateWebApiVersion(resultSetAccountEmailAndPass.appVersion)
                Log.i(MY_TAG, "Set account- updating web api version in DB")}
        }

    }

    suspend fun startRefreshE124HPassed(token: String){
        callSetNewE1(phone=phone,token = token)

    }

    private suspend fun resetSipAccess(
        authToken: String
    ) {

        val defResult= myAPI.resetSipAccess(
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
            val result=defResult.await()
        }catch (e:Throwable){
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"resetSIPAccess_from_uncancelable_job,$phone,$authToken",e.message.toString()).sendError()
                }
            }
        }
    }

    suspend fun callSetNewE1(phone: String,token: String) {


        val defResult = myAPI.setNewE1(
            phoneNumber = phone,
            signature = produceJWtToken(
                Pair(Claim.TOKEN.myClaim,token),
                Pair(Claim.PHONE.myClaim,phone)
            ),
            mobileAppVersion = mobileAppVer,
            request = NetRequest_SetE1Prenumber(
                    authToken = token,
                    phoneNumber = phone
            )
        )
        try {
            val result = defResult.await()

            if (!result.e1prenumber.isNullOrEmpty() && !result.e1prenumber.isNullOrBlank()) {
                myDatabaseDao.updatePrenumber(result.e1prenumber, System.currentTimeMillis())
            }

            if (!result.appVersion.isNullOrEmpty() && !result.appVersion.isNullOrBlank()) {
                myDatabaseDao.updateWebApiVersion(result.appVersion)
            }

        }catch (e:Throwable){

            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"setNewE1_from_uncancelable_job,$phone,$token",e.message.toString()).sendError()
                }
            }
        }

    }


}*/

