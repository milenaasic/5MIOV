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

class Repo (val myDatabaseDao: MyDatabaseDao, val myAPI: MyAPIDataService) : IRepo,LogStateOrErrorToServer{


    //User Live Data
    override fun getUserData()=myDatabaseDao.getUser()

    //SignIn Route
     override suspend fun signUpToServer(signIn: NetRequest_SignUp): Result<NetResponse_SignUp> {

        try{
            val result=myAPI.signUpToServer(
                phoneNumber = signIn.phoneNumber,
                signature = produceJWtToken(Pair(Claim.NUMBER.myClaim,signIn.phoneNumber)),
                request = signIn
            ).await()

            return Result.Success(result)

        }catch (e:Exception){
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

    /* override suspend fun logStateOrErrorToOurServer(phoneNumber:String, myoptions:Map<String,String>){
        logStateOrErrorToOurServer(phoneNumber = phoneNumber,myDatabaseDao=myDatabaseDao,myAPIService = myAPI,myoptions = myoptions)


    }*/

    //log errors and states
    suspend fun logStateOrErrorToOurServer(phoneNumber: String="",myoptions:Map<String,String>){
        logStateOrErrorToOurServer(phoneNumber=phoneNumber,myDatabaseDao=myDatabaseDao,myAPIService = myAPI,myoptions = myoptions)

    }


}

interface IRepo {
    //User Live Data
    fun getUserData(): LiveData<User>

    //SignIn Route
    suspend fun signUpToServer(signIn: NetRequest_SignUp): Result<NetResponse_SignUp>

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

    //suspend fun logStateOrErrorToOurServer(phoneNumber: String, myoptions: Map<String, String>)
}



