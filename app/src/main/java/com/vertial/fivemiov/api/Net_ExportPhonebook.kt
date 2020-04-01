package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.vertial.fivemiov.model.PhoneBookItem

data class NetRequest_ExportPhonebook(

    @Json(name="token")
    val token:String,

    @Json(name="phoneNumber")
    val phoneNumber:String,

    @Json(name="phoneBook")
    val phonebook: Array<PhoneBookItem>

)


data class NetResponse_ExportPhonebook(

    @Json(name="message")
    val message:String
)