package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_NmbExistsInDB_NoAccount(

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="signIn")
    val signin:String="false"


)

@JsonClass(generateAdapter = true)
data class NetRequest_NmbExistsInDB_UserHasAccount(

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String,

    @Json(name="signIn")
    val signin:String="true"

)

@JsonClass(generateAdapter = true)
data class NetResponse_NmbExistsInDB(

    @Json(name="message")
    val message:String

)