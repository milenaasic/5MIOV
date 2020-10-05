package app.adinfinitum.ello.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_SetSipCallerId(

    @Json(name="token")
    val authToken:String,

    @Json(name="username")
    val email:String,

    @Json(name="password")
    val password:String,

    @Json(name="sipCallerId")
    val sipCallerId:String,

    @Json(name="phone")
    val phoneNumber:String

)


@JsonClass(generateAdapter = true)
data class NetResponse_SetSipCallerId(

    @Json(name="success")
    val success:Boolean,

    @Json(name="sipCallerId")
    val sipCallerIds:String?,

    @Json(name="message")
    val message:String,

    @Json(name="version")
    val appVersion:String?,

    @Json(name="authTokenMismatch")
    val authTokenMismatch:Boolean

)


