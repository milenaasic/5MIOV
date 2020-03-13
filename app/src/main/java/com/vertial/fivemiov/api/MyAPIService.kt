package com.vertial.fivemiov.api

import android.annotation.TargetApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val NAME="MY_API"

//@TargetApi (26)
//val coded=java.util.Base64.getEncoder().encodeToString("5miov:tester".toByteArray())
val coded="NW1pb3Y6dGVzdGVy"

private const val BASE_URL ="https://test.find.in.rs/"

private val moshi= Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/*val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(23, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .build()*/

private val retrofit= Retrofit.Builder()
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
        @Body request: NetRequest_Registration): Deferred<NetResponse_Registration>

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



     //Registered User
     @POST("user/setAccountAndEmail")
     fun setAccountEmailAndPasswordForUser(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_SetAccountEmailAndPass): Deferred<NetResponse_SetAccountEmailAndPass>

     @POST("user/exportphonebook")
     fun exportPhoneBook(
         @Header("Authorization") authorization:String="Basic $coded",
         @Body request: NetRequest_ExportPhonebook): Deferred<NetResponse_ExportPhonebook>

}

