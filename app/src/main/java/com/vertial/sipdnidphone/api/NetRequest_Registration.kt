package com.vertial.sipdnidphone.api

import com.squareup.moshi.Json

data class NetRequest_Registration(

    @Json(name="useralias")
    val phoneNumber:String

)


data class NetResponse_Registration(

    @Json(name="status")
    val status:String,

    @Json(name="message")
    val message:String
)