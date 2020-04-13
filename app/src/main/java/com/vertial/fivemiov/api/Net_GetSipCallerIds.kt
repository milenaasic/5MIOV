package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_GetSipCallerIds(

    @Json(name="token")
    val authToken:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_GetSipCallerIds(

    @Json(name="success")
    val success:Boolean,

    @Json(name="callerIds")
    val sipCallerIds:Array<String>,

    @Json(name="message")
    val message:String,

    @Json(name="version")
    val appVersion:String

)


