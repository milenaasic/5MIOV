package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_GetSipAccessCredentials(

    @Json(name="token")
    val authToken:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_GetSipAccessCredentials(

    @Json(name="success")
    val success:Boolean,

    @Json(name="sipServer")
    val sipServer:String,

    @Json(name="sipPassword")
    val sipPassword:String,

    @Json(name="sipUserName")
    val sipUserName:String,

    @Json(name="sipCallerId")
    val sipCallerId:String,

    @Json(name="e1phone")
    val e1prenumber:String?,

    @Json(name="message")
    val message:String,

    @Json(name="version")
    val appVersion:String?

)

