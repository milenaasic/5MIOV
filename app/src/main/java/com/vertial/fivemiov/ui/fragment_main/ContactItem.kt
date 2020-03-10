package com.vertial.fivemiov.ui.fragment_main

data class ContactItem(
    val id: Long=0,
    val lookUpKey: String,
    val name: String,
    val photoThumbUri: String?=null,
    val hasPhoneNunber: String="0"

)