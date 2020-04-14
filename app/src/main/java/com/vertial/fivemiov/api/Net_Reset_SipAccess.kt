package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_ResetSipAccess(

    @Json(name="token")
    val authToken:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_ResetSipAccess(

    @Json(name="success")
    val success:Boolean,

    @Json(name="message")
    val message:String


)