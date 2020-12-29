package app.adinfinitum.ello.ui.registrationauthorization.models

data class NumberExistsInDBResult (
    var navigateToFragment:Int=0,
    var showToastMessage:String?=null,
    var showSnackBarMessage:String?=null,
    var userMessageServerCode:Int?=null,
    var showSnackBarErrorMessage: Boolean=false,
    var enteredEmailError:Int?=null,
    var enteredPasswordError: Int?=null

)