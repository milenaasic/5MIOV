package com.adinfinitum.hello.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_SetAccountEmailAndPass(

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="authToken")
    val authToken:String,

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String


)

@JsonClass(generateAdapter = true)
data class NetResponse_SetAccountEmailAndPass(

    @Json(name="success")
    val success:Boolean,

    @Json(name="message")
    val message:String,

    @Json(name="userMsg")
    val userMsg:String,

    @Json(name="email")
    val email:String,

    @Json(name="version")
    val appVersion:String?,

    @Json(name="e1phone")
    val e1phone:String?


)


