package com.vertial.fivemiov.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhoneBookItem(
    @Json(name="name")
    val name:String,

    @Json(name="phones")
    val phoneNumbers:Array<String>
)