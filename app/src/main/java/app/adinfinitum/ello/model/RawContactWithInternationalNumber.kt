package app.adinfinitum.ello.model

data class RawContactWithInternationalNumber (

val name: String,
val lookUpKey: String,
val normalizePhoneNumber:String,
val photoThumbUri: String?=null

)