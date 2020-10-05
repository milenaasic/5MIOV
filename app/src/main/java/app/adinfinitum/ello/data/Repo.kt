package app.adinfinitum.ello.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService, val mobileAppVer:String="0.0"){

    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()

    // Registration fragment
    private val _registrationNetworkError= MutableLiveData<String?>()
    val registrationNetworkError: LiveData<String?>
        get() = _registrationNetworkError

    private val _registrationSuccess= MutableLiveData<NetResponse_Registration?>()
    val registrationSuccess: LiveData<NetResponse_Registration?>
        get() = _registrationSuccess



    // Assign number to existing account fragment
    private val _addNumberToAccountNetworkError= MutableLiveData<String?>()
    val addNumberToAccountNetworkError: LiveData<String?>
        get() = _addNumberToAccountNetworkError

    private val _addNumberToAccountNetworkSuccess= MutableLiveData<NetResponse_AddNumberToAccount?>()
    val addNumberToAccountNetworkSuccess: LiveData<NetResponse_AddNumberToAccount?>
        get() = _addNumberToAccountNetworkSuccess



    //Number exists in DB fragment (has two buttons)
    private val _nmbExistsInDBUserHasAccountSuccess= MutableLiveData<NetResponse_NmbExistsInDB?>()
    val nmbExistsInDBUserHasAccountSuccess: LiveData<NetResponse_NmbExistsInDB?>
        get() = _nmbExistsInDBUserHasAccountSuccess

    private val _nmbExistsInDBUserHasAccountError= MutableLiveData<String?>()
    val nmbExistsInDBUserHasAccountError: LiveData<String?>
        get() = _nmbExistsInDBUserHasAccountError


    private val _nmbExistsInDB_NoAccountSuccess= MutableLiveData<NetResponse_NmbExistsInDB?>()
    val nmbExistsInDB_NoAccountSuccess: LiveData<NetResponse_NmbExistsInDB?>
        get() = _nmbExistsInDB_NoAccountSuccess

    private val _nmbExistsInDB_NoAccountError= MutableLiveData<String?>()
    val nmbExistsInDB_NoAccountError: LiveData<String?>
        get() = _nmbExistsInDB_NoAccountError



    // authorization fragment
    private val _authorizationNetworkError= MutableLiveData<String?>()
    val authorizationNetworkError: LiveData<String?>
        get() = _authorizationNetworkError

    private val _authorizationSuccess= MutableLiveData<NetResponse_Authorization?>()
    val authorizationSuccess: LiveData<NetResponse_Authorization?>
        get() = _authorizationSuccess

    private val _smsResendNetworkError= MutableLiveData<String?>()
    val smsResendNetworkError: LiveData<String?>
        get() = _smsResendNetworkError

    private val _smsResendSuccess= MutableLiveData<String?>()
    val smsResendSuccess: LiveData<String?>
        get() = _smsResendSuccess



    // Main part of the app
    private val _setAccountEmailAndPassError= MutableLiveData<String?>()
    val setAccountEmailAndPassError: LiveData<String?>
        get() = _setAccountEmailAndPassError

    private val _setAccountEmailAndPassSuccess= MutableLiveData<NetResponse_SetAccountEmailAndPass?>()
    val setAccountEmailAndPassSuccess: LiveData<NetResponse_SetAccountEmailAndPass?>
        get() = _setAccountEmailAndPassSuccess




    // Registration fragment
    suspend fun sendRegistationToServer(phone:String,smsResend:Boolean,verificationMethod:String){

        val defResponse=myAPI.sendRegistrationToServer(
                phoneNumber = phone,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,phone)),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_Registration(phoneNumber = phone,verificationMethod = verificationMethod )
                )
        try{

            val result:NetResponse_Registration=defResponse.await()

            if(smsResend) _smsResendSuccess.value=result.code.toString()+result.userMessage
            else _registrationSuccess.value=result

        } catch (e:Throwable){
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"sendRegistationToServer,$phone,smsResend_$smsResend",e.message.toString()).sendError()
                } }
            if(smsResend) _smsResendNetworkError.value=e.message
            else _registrationNetworkError.value=e.toString()

        }

    }

    //Registration fragment reset state functions
    fun resetRegistrationNetSuccess(){
        _registrationSuccess.value=null
    }

    fun resetRegistrationNetError(){
        _registrationNetworkError.value=null
    }





    // ASSIGN NUMBER TO ACCOUNT
    suspend fun assignPhoneNumberToAccount(phone:String, email:String,password:String,smsResend: Boolean=false,verificationMethod: String){

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

            if(smsResend) _smsResendSuccess.value=result.code.toString()+result.usermessage
            else _addNumberToAccountNetworkSuccess.value=result

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"assignPhoneNumberToAccount $phone, $email,$password smsResend $smsResend",e.message.toString()).sendError()
                } }
            if(smsResend)_smsResendNetworkError.value=errorMessage
            else _addNumberToAccountNetworkError.value=errorMessage

        }

    }

    //ASSIGN NUMBER TO ACCOUNT  reset state functions
    fun resetAssignPhoneNumberToAccountNetSuccess(){
        _addNumberToAccountNetworkSuccess.value=null
    }

    fun resetAssignPhoneNumberToAccountNetError(){
        _addNumberToAccountNetworkError.value=null
    }


    //NUMBER EXISTS IN DB  fragment
    suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber:String, email:String, password:String,smsResend: Boolean=false,verificationMethod: String){

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

            if(smsResend) _smsResendSuccess.value=result.code.toString()+result.userMessage
            else _nmbExistsInDBUserHasAccountSuccess.value=result
        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,enteredPhoneNumber,"numberExistsInDBVerifyAccount $enteredPhoneNumber $email,$password smsResend $smsResend",e.message.toString()).sendError()
                } }
            if(smsResend) _smsResendNetworkError.value=e.message
            else _nmbExistsInDBUserHasAccountError.value=errorMessage

        }
    }


    suspend fun numberExistsInDB_NOAccount(enteredPhoneNumber:String,smsResend: Boolean=false,verificationMethod: String){

        val defResult=myAPI.numberExistsInDB_NOAccount(
                phoneNumber = enteredPhoneNumber,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,enteredPhoneNumber),
                                            Pair(Claim.SIGN_IN.myClaim, CLAIM_VALUE_FALSE)),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_NmbExistsInDB_NoAccount(phoneNumber = enteredPhoneNumber,verificationMethod = verificationMethod)
                )
        try{
            val result=defResult.await()

            if(smsResend) _smsResendSuccess.value=result.code.toString()+result.userMessage
            else _nmbExistsInDB_NoAccountSuccess.value=result
        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,enteredPhoneNumber,"numberExistsInDB_NOAccount $enteredPhoneNumber, smsResend $smsResend",e.message.toString()).sendError()
                } }
            _nmbExistsInDB_NoAccountError.value=errorMessage
        }
    }

    // NUMBER EXISTS IN DB reset functions

    fun resetNmbExistsInDB_VerifyAccount_NetSuccess(){
        _nmbExistsInDBUserHasAccountSuccess.value=null
    }

    fun resetNmbExistsInDB_VerifyAccount_NetError(){
        _nmbExistsInDBUserHasAccountError.value=null
    }

    fun resetNmbExistsInDB_NOAccount_NetSuccess(){
        _nmbExistsInDB_NoAccountSuccess.value=null
    }

    fun resetNmbExistsInDB_NOAccount_NetError(){
        _nmbExistsInDB_NoAccountError.value=null
    }



    //Authorization fragment
    suspend fun authorizeThisUser(phone:String,smsToken:String,email: String,password: String){

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

            if(result.success==true) {
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

                withContext(Dispatchers.IO) {
                    if (result.email.isNotEmpty()
                        && result.email.isNotBlank()
                        && result.authToken.isNotEmpty()
                        && result.authToken.isNotBlank()
                        ) myDatabaseDao.updateUsersPhoneTokenEmail(phone, result.authToken, result.email)
                    else if(result.authToken.isNotEmpty() && result.authToken.isNotBlank()) myDatabaseDao.updateUsersPhoneAndToken(phone, result.authToken)
                }
            }

            _authorizationSuccess.value=result

        }
        catch (e:Throwable){
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phone,"authorizeThisUser,$phone,$smsToken,$email,$password",e.message.toString()).sendError()
                } }
            _authorizationNetworkError.value=errorMessage

        }

    }

    //Authorization fragment reset functions
    fun resetAuthorization_NetSuccess(){
        _authorizationSuccess.value=null
    }

    fun resetAuthorization_NetError(){
        _authorizationNetworkError.value=null
    }

    fun resetSMSResend_NetSuccess(){
        _smsResendSuccess.value=null
    }

    fun resetSMSResend_NetError(){
        _smsResendNetworkError.value=null
    }


    //Main part of the app, SetAccountAndEmail Fragment
    suspend fun  setAccountEmailAndPasswordForUser(phoneNumber:String,token: String,email: String,password: String){

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
                if (result.success == true) {
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

                }
                _setAccountEmailAndPassSuccess.value = result

        }
        catch (e:Throwable){
            Log.i(MY_TAG,"NetRequest_SetAccountEmailAndPass error ${e.message}")
            val errorMessage:String?=e.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer(myAPI,phoneNumber,"setAccountEmailAndPasswordForUser $phoneNumber, $token, $email, $password",e.message.toString()).sendError()
                } }
            _setAccountEmailAndPassError.value=errorMessage

        }
    }

    //Reset functions
    fun resetSetAccountEmailAndPassNetSuccess(){
        _setAccountEmailAndPassSuccess.value=null
    }

    fun resetSetAccountEmailAndPassNetError(){
        _setAccountEmailAndPassError.value=null
    }

    suspend fun getUser()=withContext(Dispatchers.IO){
        myDatabaseDao.getUserNoLiveData()

    }



    suspend fun insertEmailIntoDatabase(email: String){

        withContext(Dispatchers.IO) {
            myDatabaseDao.updateUserEmail(email)
        }
    }


    suspend fun insertTokenAndPhoneIntoDatabase(phone:String,token:String) {
        withContext(Dispatchers.IO) {
            myDatabaseDao.updateUsersPhoneAndToken(phone, token)
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

class UncancelableJob(
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


}
