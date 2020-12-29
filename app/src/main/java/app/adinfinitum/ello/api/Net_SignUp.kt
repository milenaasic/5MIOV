package app.adinfinitum.ello.api


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetRequest_SignUp(

    @Json(name="phoneNumber")
    val phoneNumber:String,

    @Json(name="email")
    val email:String="",

    @Json(name="password")
    val password:String="",

    @Json(name="signIn")
    val signin:String="",

    @Json(name="verificationMethod")
    val verificationMethod:String=""

)

@JsonClass(generateAdapter = true)
data class NetResponse_SignUp(

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
    val verificationCallerId:String,

    @Json(name="callVerificationEnabled")
    val callVerificationEnabled:Boolean

)