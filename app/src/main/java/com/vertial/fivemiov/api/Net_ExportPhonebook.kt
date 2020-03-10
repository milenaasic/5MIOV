package com.vertial.fivemiov.api

import com.squareup.moshi.Json
import com.vertial.fivemiov.data.PhoneBookItem

data class NetRequest_ExportPhonebook(

    @Json(name="phonebook")
    val phonebook: Array<PhoneBookItem>

)


data class NetResponse_ExportPhonebook(

    @Json(name="message")
    val message:String
)