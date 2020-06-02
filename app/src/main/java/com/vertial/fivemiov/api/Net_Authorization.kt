 package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_Authorization(

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="token")
    val smstoken:String,

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_Authorization(

    @Json(name="success")
    val success:Boolean,

    @Json(name="userMsg")
    val userMessage:String,

    @Json(name="version")
    val appVersion:String?,

    @Json(name="message")
    val message:String,

    @Json(name="email")
    val email:String,

    @Json(name="authToken")
    val authToken:String,

    @Json(name="sipServer")
    val sipServer:String,

    @Json(name="sipPassword")
    val sipPassword:String,

    @Json(name="sipUserName")
    val sipUserName:String,

    @Json(name="sipReady")
    val sipReady:Boolean,

    @Json(name="sipCallerId")
    val sipCallerId:String?,

    @Json(name="e1phone")
    val e1phone:String?

)
