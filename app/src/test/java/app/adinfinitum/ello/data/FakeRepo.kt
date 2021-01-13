package app.adinfinitum.ello.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import app.adinfinitum.ello.model.*
import app.adinfinitum.ello.utils.EMPTY_PHONE_NUMBER
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.Response

class FakeRepo:IRepo{

    var typeOfResponseToReturn:Int=1 // 1 is success
    override fun getUserData(): LiveData<User> {
        return MutableLiveData<User>().apply {
                value=User()
        }
    }

    override suspend fun signUpToServer(signIn: NetRequest_SignUp): Result<NetResponse_SignUp> {
        TODO("Not yet implemented")
    }

    /*override fun getConfigurationInfo(): Result<NetResponse_Config> {
        TODO("Not yet implemented")
    }*/

    override suspend fun sendRegistationToServer(
        phone: String,
        verificationMethod: String
    ): Result<NetResponse_Registration> {

        when(typeOfResponseToReturn){
            1-> return Result.Success(
                NetResponse_Registration(true,"mess","userMsg",10,true,"1231223",true)
            )

            else->return Result.Success(
                NetResponse_Registration(false,"mess","userMsg",10,true,"1231223",true)
            )
        }

    }

    override suspend fun assignPhoneNumberToAccount(
        phone: String,
        email: String,
        password: String,
        verificationMethod: String
    ): Result<NetResponse_AddNumberToAccount> {
        TODO("Not yet implemented")
    }

    override suspend fun numberExistsInDBVerifyAccount(
        enteredPhoneNumber: String,
        email: String,
        password: String,
        verificationMethod: String
    ): Result<NetResponse_NmbExistsInDB> {
        TODO("Not yet implemented")
    }

    override suspend fun numberExistsInDB_NOAccount(
        enteredPhoneNumber: String,
        verificationMethod: String
    ): Result<NetResponse_NmbExistsInDB> {
        TODO("Not yet implemented")
    }

    override suspend fun authorizeThisUser(
        phone: String,
        smsToken: String,
        email: String,
        password: String
    ): Result<NetResponse_Authorization> {
        TODO("Not yet implemented")
    }

    override suspend fun setAccountEmailAndPasswordForUser(
        phoneNumber: String,
        token: String,
        email: String,
        password: String
    ): Result<NetResponse_SetAccountEmailAndPass> {
        return Result.Success(NetResponse_SetAccountEmailAndPass(false,"","","","",""))
    }

    override fun resetSipAccess(phone: String, authToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun callSetNewE1(
        phone: String,
        token: String
    ): Result<NetResponse_SetE1Prenumber> {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(): User {
        return User(0L,"","","","")
    }

    override fun updateUsersPhoneTokenEmail(phone: String, token: String, email: String) {
        TODO("Not yet implemented")
    }

    override fun updateUsersPhoneAndToken(phone: String, token: String) {
        TODO("Not yet implemented")
    }

    override fun updateUserEmail(email: String) {
        TODO("Not yet implemented")
    }

    override fun updatePrenumber(e1Phone: String, timestamp: Long) {
        TODO("Not yet implemented")
    }

    override fun updateWebApiVersion(webApiVer: String) {
        TODO("Not yet implemented")
    }

    override fun updateE1EnabledCountries(e1EnabledCountries: String) {
        TODO("Not yet implemented")
    }

    override fun updateCallVerificationEnabledCountries(callVerificationEnabledCountries: String) {
        TODO("Not yet implemented")
    }

    override fun getCountriesWhereVerificationByCallIsEnabled(): String {
        return "Not yet implemented"
    }

    override suspend fun logStateOrErrorToOurServer(
        phoneNumber: String,
        myoptions: Map<String, String>
    ) {
        TODO("Not yet implemented")
    }
}





