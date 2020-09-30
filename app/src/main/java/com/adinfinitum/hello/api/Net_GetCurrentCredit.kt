package com.adinfinitum.hello.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_GetCurrentCredit(

    @Json(name="token")
    val authToken:String,

    @Json(name="phone")
    val phoneNumber:String
)

@JsonClass(generateAdapter = true)
data class NetResponse_GetCurrentCredit(

    @Json(name="success")
    val success:Boolean,

    @Json(name="credit")
    val credit:String,

    @Json(name="currency")
    val currency:String,

    @Json(name="e1phone")
    val e1phone:String?,

    @Json(name="version")
    val appVersion:String?,

    @Json(name="authTokenMismatch")
    val authTokenMismatch:Boolean

)