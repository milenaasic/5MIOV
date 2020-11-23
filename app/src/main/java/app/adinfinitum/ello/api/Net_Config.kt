package app.adinfinitum.ello.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*@JsonClass(generateAdapter = true)
data class NetRequest_Config(

    @Json(name="phoneNumber")
    val phoneNumber:String,

)*/

@JsonClass(generateAdapter = true)
data class NetResponse_Config(

    @Json(name="success")
    val success:Boolean,

    @Json(name="e1EnabledCountryList")
    val e1EnabledCountryList:String,

    @Json(name="callVerificationEnabledCountryList")
    val callVerificationEnabledCountryList:String
)