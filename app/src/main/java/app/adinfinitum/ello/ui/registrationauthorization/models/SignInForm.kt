package app.adinfinitum.ello.ui.registrationauthorization.models

import android.os.Parcel
import android.os.Parcelable
import android.telephony.PhoneNumberUtils
import app.adinfinitum.ello.utils.isEmailValid
import app.adinfinitum.ello.utils.isPasswordValid
import app.adinfinitum.ello.utils.isPhoneNumberValid

class SignInForm(var phoneNmb:String?=null, var email:String?=null,var password:String?=null ):Parcelable{

    var normalizedPhoneNmb:String?=null
        private set
        get()=phoneNmb?.let {
            PhoneNumberUtils.normalizeNumber(phoneNmb)
    }

    var isPhoneNumberValid=false
        private set
        get()=normalizedPhoneNmb?.isPhoneNumberValid()?:false


    var isEmailValid=false
        private set
        get() = email?.isEmailValid()?:false


    var isPasswordValid=false
        private set
        get() = password?.isPasswordValid()?:false

    var areSignInDataValid=false
        private set
        get() = isPhoneNumberValid && isEmailValid && isPasswordValid


    private constructor(source: Parcel):this(){
        phoneNmb=source.readString()
        email=source.readString()
        password=source.readString()
    }

    companion object{
        val CREATOR=object:Parcelable.Creator<SignInForm>{
            override fun createFromParcel(source: Parcel?): SignInForm? {
                return  if (source != null) SignInForm(source)
                else null
            }

            override fun newArray(size: Int): Array<SignInForm> {
                return Array<SignInForm>(size){SignInForm()}
            }


        }

    }
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.apply {
            writeString(phoneNmb)
            writeString(email)
            writeString(password)
         }
    }

}