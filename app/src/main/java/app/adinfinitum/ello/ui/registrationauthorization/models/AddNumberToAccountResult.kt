package app.adinfinitum.ello.ui.registrationauthorization.models

class AddNumberToAccountResult(
    var navigateToFragment:Int=0,
    var showToastMessage:String?=null,
    var showSnackBarMessage:String?=null,
    var userMessageServerCode:Int?=null,
    var showSnackBarErrorMessage: Boolean=false,
    var enteredPhoneError:Int?=null,
    var enteredEmailError:Int?=null,
    var enteredPasswordError:Int?=null,
    var showTermsOfUseDialog: Boolean =false,
    var mustAskForPermission:Boolean=false

)