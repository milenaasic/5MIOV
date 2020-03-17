package com.vertial.fivemiov.api

import com.squareup.moshi.Json

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


data class NetResponse_Authorization(

    @Json(name="message")
    val message:String,

    @Json(name="authToken")
    val authToken:String

)
