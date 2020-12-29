package app.adinfinitum.ello.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import app.adinfinitum.ello.model.E1andCallVerificationEnabledCountries
import app.adinfinitum.ello.model.User
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


private const val MY_TAG="MY_Repository"

class Repo (val myDatabaseDao: MyDatabaseDao, val myAPI: MyAPIService, val mobileAppVer:String="0.0"): IRepo {


    //User Live Data
    override fun getUserData()=myDatabaseDao.getUser()

    //SignIn Route
     override suspend fun signUpToServer(signIn: NetRequest_SignUp): Result<NetResponse_SignUp> {

        try{
            val result=myAPI.signUpToServer(
                phoneNumber = signIn.phoneNumber,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,signIn.phoneNumber)),
                mobileAppVersion = mobileAppVer,
                request = signIn
            ).await()

            return Result.Success(result)

        }catch (e:Exception){
            return Result.Error(e)

        }


    }


    // Registration fragment
    override suspend fun sendRegistationToServer(phone:String, verificationMethod:String): Result<NetResponse_Registration> {

        try{
            val result=myAPI.sendRegistrationToServer(
                phoneNumber = phone,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,phone)),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_Registration(phoneNumber = phone,verificationMethod = verificationMethod )
            ).await()

            return Result.Success(result)

        }catch (e:Exception){
            return Result.Error(e)

        }


    }



    // ASSIGN NUMBER TO ACCOUNT
    override suspend fun assignPhoneNumberToAccount(phone:String, email:String, password:String, verificationMethod: String):Result<NetResponse_AddNumberToAccount>{

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
            return Result.Error(e)

        }

    }



    //NUMBER EXISTS IN DB  fragment
    override suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber:String, email:String, password:String, verificationMethod: String):Result<NetResponse_NmbExistsInDB>{

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
            return Result.Error(e)
        }
    }


    override suspend fun numberExistsInDB_NOAccount(enteredPhoneNumber:String, verificationMethod: String):Result<NetResponse_NmbExistsInDB>{

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
            return Result.Error(e)
        }
    }


    //Authorization fragment
    override suspend fun authorizeThisUser(phone:String, smsToken:String, email: String, password: String):Result<NetResponse_Authorization>{

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
            return Result.Success(result)

        }
        catch (e:Exception){
            return Result.Error(e)

        }

    }



    //SetAccountAndEmail Fragment - Main part of the app
    override suspend fun  setAccountEmailAndPasswordForUser(phoneNumber:String, token: String, email: String, password: String):Result<NetResponse_SetAccountEmailAndPass>{

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
                return Result.Success(result)

        }
        catch (e:Exception){
            Log.i(MY_TAG,"NetRequest_SetAccountEmailAndPass error ${e.message}")
            return Result.Error(e)

        }
    }


    // Reset Sip Access data on server
    override fun resetSipAccess(
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

            }

    }

    //get E1 prenumber
    override suspend fun callSetNewE1(phone: String, token: String):Result<NetResponse_SetE1Prenumber> {

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
            return Result.Success(result)

        }catch (e:Exception){
            return Result.Error(e)
        }

    }


    override suspend fun getUser()=withContext(IO){
        myDatabaseDao.getUserNoLiveData()
    }

    override fun updateUsersPhoneTokenEmail(phone: String, token: String, email: String){
        myDatabaseDao.updateUsersPhoneTokenEmail(phoneNb = phone,token = token,email = email)
    }

    override fun updateUsersPhoneAndToken (phone:String, token:String) {
        myDatabaseDao.updateUsersPhoneAndToken(phoneNb = phone,token =  token)
    }

    override fun updateUserEmail (email: String){
        myDatabaseDao.updateUserEmail(email = email)
    }

    override fun updatePrenumber(e1Phone:String, timestamp:Long){
        myDatabaseDao.updatePrenumber(prenumber = e1Phone,timestamp = timestamp)
    }

    override fun updateWebApiVersion(webApiVer:String){
        myDatabaseDao.updateWebApiVersion(webApiVer =webApiVer )
    }

    override fun updateE1EnabledCountries(e1EnabledCountries:String){
        myDatabaseDao.updateE1EnabledCoutries(e1EnabledCountries = e1EnabledCountries)
    }

    override fun updateCallVerificationEnabledCountries(callVerificationEnabledCountries:String){
        myDatabaseDao.updateCallVerificationEnabledCoutries(callVerificationEnabledCountries = callVerificationEnabledCountries)
    }

    override fun getCountriesWhereVerificationByCallIsEnabled():String{
        return myDatabaseDao.getCountriesWithVerificationCallEnabled().callVerificationEnabledCountries
    }

    override suspend fun logStateOrErrorToOurServer(phoneNumber:String, myoptions:Map<String,String>){
        logStateOrErrorToOurServer(phoneNumber = phoneNumber,myDatabaseDao=myDatabaseDao,myAPIService = myAPI,myoptions = myoptions)


    }


}

interface IRepo : LogStateOrErrorToServer {
    //User Live Data
    fun getUserData(): LiveData<User>

    suspend fun signUpToServer(signIn: NetRequest_SignUp): Result<NetResponse_SignUp>

    // Registration fragment
    suspend fun sendRegistationToServer(phone: String, verificationMethod: String): Result<NetResponse_Registration>

    // ASSIGN NUMBER TO ACCOUNT
    suspend fun assignPhoneNumberToAccount(phone: String, email: String, password: String, verificationMethod: String): Result<NetResponse_AddNumberToAccount>

    //NUMBER EXISTS IN DB  fragment
    suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber: String, email: String, password: String, verificationMethod: String): Result<NetResponse_NmbExistsInDB>
    suspend fun numberExistsInDB_NOAccount(enteredPhoneNumber: String, verificationMethod: String): Result<NetResponse_NmbExistsInDB>

    //Authorization fragment
    suspend fun authorizeThisUser(phone: String, smsToken: String, email: String, password: String): Result<NetResponse_Authorization>

    //SetAccountAndEmail Fragment - Main part of the app
    suspend fun  setAccountEmailAndPasswordForUser(phoneNumber: String, token: String, email: String, password: String): Result<NetResponse_SetAccountEmailAndPass>

    // Reset Sip Access data on server
    fun resetSipAccess(
        phone: String,
        authToken: String
    )

    //get E1 prenumber
    suspend fun callSetNewE1(phone: String, token: String): Result<NetResponse_SetE1Prenumber>

    suspend fun getUser(): User
    fun updateUsersPhoneTokenEmail(phone: String, token: String, email: String)
    fun updateUsersPhoneAndToken (phone: String, token: String)
    fun updateUserEmail (email: String)
    fun updatePrenumber(e1Phone: String, timestamp: Long)
    fun updateWebApiVersion(webApiVer: String)
    fun updateE1EnabledCountries(e1EnabledCountries: String)
    fun updateCallVerificationEnabledCountries(callVerificationEnabledCountries: String)
    fun getCountriesWhereVerificationByCallIsEnabled(): String
    suspend fun logStateOrErrorToOurServer(phoneNumber: String ="", myoptions: Map<String, String>)
}

