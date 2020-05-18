package com.vertial.fivemiov.api

import android.annotation.TargetApi
import android.content.Context
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

private const val NAME="MY_API"

//@TargetApi (26)
//val coded=java.util.Base64.getEncoder().encodeToString("5miov:tester".toByteArray())
val coded="NW1pb3Y6dGVzdGVy"
val basicCoded="Basic NW1pb3Y6dGVzdGVy"


const val BASE_URL ="https://5miov.vertial.net/"

private val moshi= Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/*val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(23, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .build()*/

val logging=HttpLoggingInterceptor().apply {
    level=HttpLoggingInterceptor.Level.BODY

 }
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()


private val retrofit= Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

object MyAPI {
    val retrofitService : MyAPIService by lazy {
        retrofit.create(MyAPIService::class.java)
    }
}

 interface MyAPIService {

    //Registrationa and Authorization Process


    @POST("api/user/signup")
    fun sendRegistrationToServer(
        @Header("Authorization") authorization:String="Basic $coded",
       // @Header("MobileAppVersion") mobileAppVersion: String ="1.2",
        @Body request: NetRequest_Registration): Deferred<NetResponse_Registration>

    /*fun sendRegistrationToServer(
   @Header("Authorization") authorization:String="Basic $coded",
   @Body request: NetRequest_Registration): Call<NetResponse_Registration>*/


     @POST("api/user/signup")
     fun sendAddNumberToAccount(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_AddNumberToAccount): Deferred<NetResponse_AddNumberToAccount>

     @POST("api/user/signup")
     fun numberExistsInDBVerifyAccount(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_NmbExistsInDB_UserHasAccount): Deferred<NetResponse_NmbExistsInDB>

     @POST("api/user/signup")
     fun numberExistsInDB_NOAccount(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_NmbExistsInDB_NoAccount): Deferred<NetResponse_NmbExistsInDB>

     @POST("api/user/create")
     fun authorizeUser(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_Authorization): Deferred<NetResponse_Authorization>




     //registrovan korisnik , net pozivi u okviru glavnog deo app-a
     @POST("api/user/setCredentials")
     fun setAccountEmailAndPasswordForUser(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_SetAccountEmailAndPass): Deferred<NetResponse_SetAccountEmailAndPass>

     @POST("api/phoneBook/update")
     fun exportPhoneBook(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_ExportPhonebook): Deferred<NetResponse_ExportPhonebook>

     @GET("dashboard")
     fun loadDashboard(
         @Header("wvtk") authorization:String="7893c5c1781811ea9614839453911717"): Deferred<String>


    //E1 prenumber rute
    @POST("/api/sip/getE1")
    fun getE1(
        @Header("Authorization") authorization:String="Basic $coded",
        @Body request: NetRequest_GetE1): Deferred<NetResponse_GetE1>

     @POST("/api/sip/setNewE1")
     fun setNewE1(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_SetE1Prenumber): Deferred<NetResponse_SetE1Prenumber>



     //Sip rute

     @POST("api/sip/getSipCallerIds")
     fun getSipCallerIds(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_GetSipCallerIds): Deferred<NetResponse_GetSipCallerIds>


     @POST("api/sip/setSipCallerId")
     fun setSipCallerId(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_SetSipCallerId): Deferred<NetResponse_SetSipCallerId>


     @POST("api/sip/resetSipAccess")
     fun resetSipAccess(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_ResetSipAccess): Deferred<NetResponse_ResetSipAccess>

     @POST("api/sip/getSipAccess")
     fun getSipAccess(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_GetSipAccessCredentials): Deferred<NetResponse_GetSipAccessCredentials>


    // get Credit ruta
    @POST("api/account/credit")
    fun getCurrentCredit(
        @Header("Authorization") authorization:String="Basic $coded",
        @Body request: NetRequest_GetCurrentCredit): Deferred<NetResponse_GetCurrentCredit>


     // posalji gresku na server

     @PUT("api/mobileLog")
     fun sendErrorToServer(
         @Header("Authorization") authorization:String="Basic $coded",
         @Query("process") process:String, @Query("message") errorMsg:String):Deferred<Response<Unit>>
}

