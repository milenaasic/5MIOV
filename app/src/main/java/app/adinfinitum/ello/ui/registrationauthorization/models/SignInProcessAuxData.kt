package app.adinfinitum.ello.ui.registrationauthorization.models

import android.os.Parcel
import android.os.Parcelable
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_CALL
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_EXPENSIVE_CALL
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_SMS

class SignInProcessAuxData() :Parcelable {

    var signInValue_whenPhoneNumberAlreadyExists: Boolean = false

    //verificationByCallEnabled information and verification method can be defined remotely and locally
    //initially it is set to true and VERIFICATION_METHOD_CALL
    //locallu it is changed after ResendSMS Button is CLicked in Authorization fragment or after call auth fails/'/..m,m

    var verificationMethod: String= VERIFICATION_METHOD_CALL
    var verificationByCallEnabled: Boolean = true
        private set
        get() {
            return  if(verificationMethod==VERIFICATION_METHOD_CALL || verificationMethod== VERIFICATION_METHOD_EXPENSIVE_CALL ) true
                    else false
        }

    //var callerId: String? = null
    //test
    var callerId: String? = "381113240809,+381113240809"

    fun getCallerIdList():List<String>{
        if(callerId!=null) return (callerId as CharSequence).split(",")
        else return emptyList()
    }

    private constructor(parcel: Parcel) : this() {
        signInValue_whenPhoneNumberAlreadyExists = parcel.readByte() != 0.toByte()
        verificationMethod = parcel.readString()?: VERIFICATION_METHOD_SMS
        callerId = parcel.readString()

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (signInValue_whenPhoneNumberAlreadyExists) 1 else 0)
        parcel.writeString(verificationMethod)
        parcel.writeString(callerId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SignInProcessAuxData> {
        override fun createFromParcel(parcel: Parcel): SignInProcessAuxData {
            return SignInProcessAuxData(parcel)
        }

        override fun newArray(size: Int): Array<SignInProcessAuxData?> {
            return arrayOfNulls(size)
        }
    }

}