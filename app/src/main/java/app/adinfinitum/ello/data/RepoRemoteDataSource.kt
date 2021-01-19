package app.adinfinitum.ello.data

import android.util.Log
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.model.PhoneBookItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private val MY_TAG="RepoRemoteDataSource"

interface IRepoRemoteDataSource {

    suspend fun signUpToServer(signIn: NetRequest_SignUp): Result<NetResponse_SignUp>

    suspend fun authorizeThisUser(phone: String, smsToken: String, email: String, password: String): Result<NetResponse_Authorization>

    suspend fun  setAccountEmailAndPasswordForUser(phoneNumber: String, token: String, email: String, password: String): Result<NetResponse_SetAccountEmailAndPass>

    fun resetSipAccess(phone: String, authToken: String)

    suspend fun callSetNewE1(phone: String, token: String): Result<NetResponse_SetE1Prenumber>

    suspend fun getCredit(phone: String, token: String): Result<NetResponse_GetCurrentCredit>
    suspend fun exportPhoneBook(token: String, phoneNumber: String, phoneBook: List<PhoneBookItem>): Result<NetResponse_ExportPhonebook>
    suspend fun getSipAccessCredentials(token: String, phone: String): Result<NetResponse_GetSipAccessCredentials>
}

class RepoRemoteDataSource (val myAPIService: MyAPIDataService, val dispatcher: CoroutineDispatcher=Dispatchers.IO) :
    IRepoRemoteDataSource {

    //SignIn Route
    override suspend fun signUpToServer(signIn: NetRequest_SignUp): Result<NetResponse_SignUp> {

        try{
            val result=myAPIService.signUpToServer(
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

        val defResponse=myAPIService.authorizeUser(
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

            val defResult = myAPIService.setAccountEmailAndPasswordForUser(
                phoneNumber = phoneNumber,
                signature = produceJWtToken(
                    Pair(Claim.NUMBER.myClaim, phoneNumber),
                    Pair(Claim.AUTH_TOKEN.myClaim, token),
                    Pair(Claim.EMAIL.myClaim, email),
                    Pair(Claim.PASSWORD.myClaim, password)
                ),
                request = NetRequest_SetAccountEmailAndPass(
                    phoneNumber = phoneNumber, authToken = token, email = email, password = password
                )
            )
            try {
                val result = defResult.await()
                Log.i(MY_TAG, "NetRequest_SetAccountEmailAndPass $result")
                return Result.Success(result)

            } catch (e: Exception) {
                Log.i(MY_TAG, "NetRequest_SetAccountEmailAndPass error ${e.message}")
                return Result.Error(e)

            }
    }

    // Reset Sip Access data on server
    override fun resetSipAccess(
        phone:String,
        authToken: String
    ) {
        try {
            myAPIService.resetSipAccess(
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

        val defResult = myAPIService.setNewE1(
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

    //DialPad fragment
    override suspend fun getCredit(phone:String, token:String):Result<NetResponse_GetCurrentCredit>{

                try {
                    val result = myAPIService.getCurrentCredit(
                        phoneNumber = phone,
                        signature = produceJWtToken(
                            Pair(Claim.TOKEN.myClaim, token),
                            Pair(Claim.PHONE.myClaim, phone)
                        ),
                        request = NetRequest_GetCurrentCredit(authToken = token, phoneNumber = phone)
                    ).await()

                     return Result.Success(result)

                } catch (e: Exception) {
                     return Result.Error(e)
                }

    }


    override suspend fun exportPhoneBook(token:String, phoneNumber:String, phoneBook:List<PhoneBookItem>):Result<NetResponse_ExportPhonebook>{
        Log.i(MY_TAG, "EXPORTING PHONEBOOK")

        try {

            val result = myAPIService.exportPhoneBook(
                phoneNumber=phoneNumber,
                signature = produceJWtTokenWithArrayInput(
                    inputArray=Pair(Claim.PHONEBOOK.myClaim,phoneBook.toTypedArray()),
                    claimsAndValues1 = Pair(Claim.TOKEN.myClaim,token),
                    claimsAndValues2 = Pair(Claim.PHONENUMBER.myClaim,phoneNumber)
                ),
                request = NetRequest_ExportPhonebook(
                    token,
                    phoneNumber,
                    phoneBook.toTypedArray()
                )
            ).await()

            return Result.Success(result)

        } catch (e: Exception) {
            Log.i(MY_TAG, "EXPORTING PHONEBOOK FAILURE, ${e.message}")
            return Result.Error(e)
        }
    }


    override suspend fun getSipAccessCredentials(token: String, phone: String):Result<NetResponse_GetSipAccessCredentials>{
        return withContext(dispatcher) {

            try {
                val response = myAPIService.getSipAccess(
                    phoneNumber = phone,
                    signature = produceJWtToken(
                        Pair(Claim.TOKEN.myClaim, token),
                        Pair(Claim.PHONE.myClaim, phone)
                    ),
                    request = NetRequest_GetSipAccessCredentials(
                        authToken = token,
                        phoneNumber = phone
                    )
                ).await()

                Result.Success(response)

            } catch (e: Exception) {

                Result.Error(e)

            }
        }
    }


}