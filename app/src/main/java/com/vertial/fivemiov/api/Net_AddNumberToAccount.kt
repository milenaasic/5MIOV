package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_AddNumberToAccount (

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_AddNumberToAccount (

    @Json(name="message")
    val message:String
)

