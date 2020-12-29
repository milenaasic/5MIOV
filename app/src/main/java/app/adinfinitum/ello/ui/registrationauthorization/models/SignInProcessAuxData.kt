package app.adinfinitum.ello.ui.registrationauthorization.models

import android.os.Parcel
import android.os.Parcelable
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_CALL
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_SMS

class SignInProcessAuxData() :Parcelable {

    var signInValue_whenPhoneNumberAlreadyExists: Boolean = false

    //this information is defined remotely (server controlled)
    var verificationByCallEnabled: Boolean = false
    var verificationMethod: String=VERIFICATION_METHOD_SMS
        private set
        get() {
          return if(verificationByCallEnabled) VERIFICATION_METHOD_CALL else VERIFICATION_METHOD_SMS

        }


    var callerId: String? = null

    //var smsResend=false

    private constructor(parcel: Parcel) : this() {
        signInValue_whenPhoneNumberAlreadyExists = parcel.readByte() != 0.toByte()
        verificationMethod = parcel.readString()?: VERIFICATION_METHOD_SMS
        callerId = parcel.readString()
        //smsResend = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (signInValue_whenPhoneNumberAlreadyExists) 1 else 0)
        parcel.writeString(verificationMethod)
        parcel.writeString(callerId)
        //parcel.writeByte(if (smsResend) 1 else 0)
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