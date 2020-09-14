package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.vertial.fivemiov.ui.main_activity.MainActivity

@JsonClass(generateAdapter = true)
data class NetRequest_Registration(

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="verificationMethod")
    val verificationMethod:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_Registration(

    @Json(name="success")
    val success:Boolean,

    @Json(name="message")
    val message:String,

    @Json(name="userMsg")
    val userMessage:String,

    @Json(name="code")
    val code:Int,

    @Json(name="phoneNumberAlreadyAssigned")
    val phoneNumberAlreadyAssigned:Boolean,

    @Json(name="verificationCallerId")
    val verificationCallerId:String


)