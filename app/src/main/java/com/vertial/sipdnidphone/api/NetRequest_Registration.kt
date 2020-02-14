package com.vertial.sipdnidphone.api

import com.squareup.moshi.Json

data class NetRequest_Registration(

    @Json(name="phone_number")
    val phoneNumber:String

)


data class NetResponse_Registration(

    @Json(name="token")
    val token:String

)