package com.vertial.fivemiov.model

data class ContactItemWithInternationalNumbers(

    val lookUpKey: String,
    val name: String,
    val photoThumbUri: String?=null,
    val internationalNumbers: List<PhoneItem>

)