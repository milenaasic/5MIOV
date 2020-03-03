package com.vertial.fivemiov.api

import com.squareup.moshi.Json

data class NetRequest_Registration(

    @Json(name="useralias")
    val phoneNumber:String

)


data class NetResponse_Registration(

    @Json(name="message")
    val message:String
)