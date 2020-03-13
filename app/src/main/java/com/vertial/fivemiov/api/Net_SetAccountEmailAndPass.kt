package com.vertial.fivemiov.api

import com.squareup.moshi.Json

data class NetRequest_SetAccountEmailAndPass(

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String,

    @Json(name="token")
    val token:String

)


data class NetResponse_SetAccountEmailAndPass(

    @Json(name="message")
    val message:String
)