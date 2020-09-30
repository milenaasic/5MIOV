package com.adinfinitum.hello.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_AddNumberToAccount (

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String,

    @Json(name="verificationMethod")
    val verificationMethod:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_AddNumberToAccount (

    @Json(name="success")
    val success:Boolean,

    @Json(name="message")
    val message:String,

    @Json(name="userMsg")
    val usermessage:String,

    @Json(name="code")
    val code:Int,

    @Json(name="verificationCallerId")
    val verificationCallerId:String,

    @Json(name="callVerificationEnabled")
    val callVerificationEnabled:Boolean


)

