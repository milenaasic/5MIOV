package app.adinfinitum.ello.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_GetSipCallerIds(

    @Json(name="token")
    val authToken:String,

    @Json(name="phoneNumber")
    val phoneNumber:String

)

@JsonClass(generateAdapter = true)
data class NetResponse_GetSipCallerIds(

    @Json(name="success")
    val success:Boolean,

    @Json(name="callerIds")
    val sipCallerIds:Array<String>,

    @Json(name="message")
    val message:String,

    @Json(name="version")
    val appVersion:String,

    @Json(name="authTokenMismatch")
    val authTokenMismatch:Boolean

)


