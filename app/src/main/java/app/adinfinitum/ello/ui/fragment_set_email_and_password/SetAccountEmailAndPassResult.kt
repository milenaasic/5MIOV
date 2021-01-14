package app.adinfinitum.ello.ui.fragment_set_email_and_password

data class SetAccountEmailAndPassResult (
    var navigateToFragment:Int=0,
    var showToastMessage:String?=null,
    var showSnackBarMessage:String?=null,
    var userMessageServerCode:Int?=null,
    var showSnackBarErrorMessage: Boolean=false

)