package com.vertial.fivemiov.api

import com.squareup.moshi.Json

data class NetRequest_NmbExistsInDB_NoAccount(

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="signin")
    val signin:String="true"


)

data class NetRequest_NmbExistsInDB_UserHasAccount(

    @Json(name="number")
    val phoneNumber:String,

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String

)

data class NetResponse_NmbExistsInDB(

    @Json(name="message")
    val message:String

)