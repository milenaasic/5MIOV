package com.vertial.sipdnidphone.api

import com.squareup.moshi.Json

data class NetRequest_Authorization(

    @Json(name="useralias")
    val phoneNumber:String,

    @Json(name="token")
    val smstoken:String
)


data class NetResponse_Authorization(

    @Json(name="message")
    val message:String,

    @Json(name="authToken")
    val authToken:String

)
