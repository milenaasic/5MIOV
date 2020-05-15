package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass



@JsonClass(generateAdapter = true)
data class NetRequest_SendErrorToServer (

    @Json(name="process")
    val process:String,

    @Json(name="message")
    val message:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_SendErrorToServer (

    @Json(name="success")
    val success:Boolean


)

