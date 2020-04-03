package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.vertial.fivemiov.model.PhoneBookItem

@JsonClass(generateAdapter = true)
data class NetRequest_ExportPhonebook(

    @Json(name="token")
    val token:String,

    @Json(name="phoneNumber")
    val phoneNumber:String,

    @Json(name="phoneBook")
    val phonebook: Array<PhoneBookItem>

)

@JsonClass(generateAdapter = true)
data class NetResponse_ExportPhonebook(

    @Json(name="message")
    val message:String
)