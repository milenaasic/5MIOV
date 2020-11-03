package app.adinfinitum.ello.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_NmbExistsInDB_NoAccount(

    @Json(name="phoneNumber")
    val phoneNumber:String,

    @Json(name="signIn")
    val signin:String="false",

    @Json(name="verificationMethod")
    val verificationMethod:String


)

@JsonClass(generateAdapter = true)
data class NetRequest_NmbExistsInDB_UserHasAccount(

    @Json(name="phoneNumber")
    val phoneNumber:String,

    @Json(name="email")
    val email:String,

    @Json(name="password")
    val password:String,

    @Json(name="signIn")
    val signin:String="true",

    @Json(name="verificationMethod")
    val verificationMethod:String


)

@JsonClass(generateAdapter = true)
data class NetResponse_NmbExistsInDB(

    @Json(name="success")
    val success:Boolean,

    @Json(name="message")
    val message:String,

    @Json(name="userMsg")
    val userMessage:String,

    @Json(name="code")
    val code:Int,

    @Json(name="version")
    val appVersion:String?,

    @Json(name="verificationCallerId")
    val verificationCallerId:String

)