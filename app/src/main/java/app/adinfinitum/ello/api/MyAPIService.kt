package app.adinfinitum.ello.api

import android.util.Base64
import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val NAME="MY_API"

const val HEADER_PHONE_KEY="X-Phone-Number"
const val HEADER_SIGNATURE="Signature"
const val HEADER_MOBILE_APP_VERSION="X-App-Version"
const val BASE_URL ="https://5miov.vertial.net/"

object MyAPI {

    private val coded= Base64.encodeToString("5miov:tester".toByteArray(),Base64.NO_WRAP)

    private val moshi= Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging=HttpLoggingInterceptor().apply {
        level=HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(object:Interceptor{
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val requset=chain.request()
                val newRequest: Request.Builder=requset.newBuilder()
                    .addHeader("Authorization", "Basic $coded")
                return chain.proceed((newRequest.build()))
            }
        })
        .addInterceptor(logging)
        .build()


    private val retrofit= Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()


    val retrofitService : MyAPIService by lazy {
        retrofit.create(MyAPIService::class.java)
    }

}

 interface MyAPIService {

    //Config
    @POST("api/user/signup")
    fun getConfigurationInfo(): Deferred<NetResponse_Config>

     //Registration Reorganized
     @POST("api/user/signup")
     fun signUpToServer(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_SignUp): Deferred<NetResponse_SignUp>



     //Registration and Authorization Process
    @POST("api/user/signup")
    fun sendRegistrationToServer(
        @Header(HEADER_PHONE_KEY) phoneNumber:String,
        @Header(HEADER_SIGNATURE) signature:String,
        @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
        @Body request: NetRequest_Registration): Deferred<NetResponse_Registration>

     @POST("api/user/signup")
     fun sendAddNumberToAccount(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_AddNumberToAccount): Deferred<NetResponse_AddNumberToAccount>

     @POST("api/user/signup")
     fun numberExistsInDBVerifyAccount(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_NmbExistsInDB_UserHasAccount): Deferred<NetResponse_NmbExistsInDB>

     @POST("api/user/signup")
     fun numberExistsInDB_NOAccount(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_NmbExistsInDB_NoAccount): Deferred<NetResponse_NmbExistsInDB>

     @POST("api/user/create")
     fun authorizeUser(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_Authorization): Deferred<NetResponse_Authorization>




     //Main part of the app
     @POST("api/user/setCredentials")
     fun setAccountEmailAndPasswordForUser(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_SetAccountEmailAndPass): Deferred<NetResponse_SetAccountEmailAndPass>

     @POST("api/phoneBook/update")
     fun exportPhoneBook(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_ExportPhonebook): Deferred<NetResponse_ExportPhonebook>


    //E1 prenumber
    /*@POST("/api/sip/getE1")
    fun getE1(
        @Header(HEADER_PHONE_KEY) phoneNumber:String,
        @Header(HEADER_SIGNATURE) signature:String,
        @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
        @Body request: NetRequest_GetE1): Deferred<NetResponse_GetE1>*/

     @POST("/api/sip/setNewE1")
     fun setNewE1(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_SetE1Prenumber): Deferred<NetResponse_SetE1Prenumber>



     //Sip
     /*@POST("api/sip/getSipCallerIds")
     fun getSipCallerIds(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_GetSipCallerIds): Deferred<NetResponse_GetSipCallerIds>


     @POST("api/sip/setSipCallerId")
     fun setSipCallerId(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_SetSipCallerId): Deferred<NetResponse_SetSipCallerId>*/


     @POST("api/sip/resetSipAccess")
     fun resetSipAccess(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_ResetSipAccess): Deferred<NetResponse_ResetSipAccess>

     @POST("api/sip/getSipAccess")
     fun getSipAccess(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @Header(HEADER_SIGNATURE) signature:String,
         @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
         @Body request: NetRequest_GetSipAccessCredentials): Deferred<NetResponse_GetSipAccessCredentials>



    // get Credit
    @POST("api/account/credit")
    fun getCurrentCredit(
        @Header(HEADER_PHONE_KEY) phoneNumber:String,
        @Header(HEADER_SIGNATURE) signature:String,
        @Header(HEADER_MOBILE_APP_VERSION) mobileAppVersion:String,
        @Body request: NetRequest_GetCurrentCredit): Deferred<NetResponse_GetCurrentCredit>



     // log error to server
     @PUT("api/mobileLog")
     fun logStateOrErrorToServer(
         @Header(HEADER_PHONE_KEY) phoneNumber:String,
         @QueryMap options:Map<String,String>):Response<Nothing>

}

