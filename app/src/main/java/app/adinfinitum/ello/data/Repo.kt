package app.adinfinitum.ello.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import app.adinfinitum.ello.model.E1andCallVerificationEnabledCountries
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


private const val MY_TAG="MY_Repository"
class Repo (val myDatabaseDao: MyDatabaseDao,val myAPI: MyAPIService, val mobileAppVer:String="0.0"):LogStateOrErrorToServer{


    //User Live Data
    fun getUserData()=myDatabaseDao.getUser()

    //configuration route
    fun getConfigurationInfo():Result<NetResponse_Config>{
        try{
            //todo when route becomes active change this
            //val result=myAPI.getConfigurationInfo().await()
            return Result.Success(data = NetResponse_Config(success = true,e1EnabledCountryList = "381,382,384",callVerificationEnabledCountryList = "388"))

        }catch (e:Exception){

            return Result.Error(e)

        }

    }

    // Registration fragment
    suspend fun sendRegistationToServer(phone:String,verificationMethod:String): Result<NetResponse_Registration> {

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
    suspend fun assignPhoneNumberToAccount(phone:String, email:String,password:String,verificationMethod: String):Result<NetResponse_AddNumberToAccount>{

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
    suspend fun numberExistsInDBVerifyAccount(enteredPhoneNumber:String, email:String, password:String,verificationMethod: String):Result<NetResponse_NmbExistsInDB>{

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


    suspend fun numberExistsInDB_NOAccount(enteredPhoneNumber:String,verificationMethod: String):Result<NetResponse_NmbExistsInDB>{

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
            return Result.Success(result)

        }
        catch (e:Exception){
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
                return Result.Success(result)

        }
        catch (e:Exception){
            Log.i(MY_TAG,"NetRequest_SetAccountEmailAndPass error ${e.message}")
            return Result.Error(e)

        }
    }


    // Reset Sip Access data on server
    fun resetSipAccess(
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
    suspend fun callSetNewE1(phone: String,token: String):Result<NetResponse_SetE1Prenumber> {

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


    suspend fun getUser()=withContext(IO){
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

    fun updateE1EnabledCountries(e1EnabledCountries:String){
        myDatabaseDao.updateE1EnabledCoutries(e1EnabledCountries = e1EnabledCountries)
    }

    fun updateCallVerificationEnabledCountries(callVerificationEnabledCountries:String){
        myDatabaseDao.updateCallVerificationEnabledCoutries(callVerificationEnabledCountries = callVerificationEnabledCountries)
    }

    fun getCountriesWhereVerificationByCallIsEnabled():String{
        return myDatabaseDao.getCountriesWithVerificationCallEnabled()
    }

    suspend fun logStateOrErrorToOurServer(phoneNumber:String="",myoptions:Map<String,String>){
        logStateOrErrorToOurServer(phoneNumber = phoneNumber,myDatabaseDao=myDatabaseDao,myAPIService = myAPI,myoptions = myoptions)


    }



}



