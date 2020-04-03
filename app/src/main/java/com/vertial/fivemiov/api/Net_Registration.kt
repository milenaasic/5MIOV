package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_Registration(

    @Json(name="number")
    val phoneNumber:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_Registration(

    @Json(name="message")
    val message:String,

    @Json(name="phoneNumberAlreadyAssigned")
    val phoneNumberAlreadyAssigned:Boolean


)