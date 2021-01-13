package app.adinfinitum.ello.ui.registrationauthorization

import app.adinfinitum.ello.ui.registrationauthorization.models.SignInForm
import org.junit.Test

import org.junit.Assert.*

class SignInFormTest {

    @Test
    fun getNormalizedPhoneNmb() {
        val signInForm1= SignInForm().apply { phoneNmb=null }
        val signInForm2= SignInForm().apply { phoneNmb="+381 64 640 640" }
        println(signInForm1.phoneNmb)
        println(signInForm1.normalizedPhoneNmb)
        println(signInForm2.normalizedPhoneNmb)

        assertEquals(signInForm1.normalizedPhoneNmb,null)
        assertEquals(signInForm2.normalizedPhoneNmb,"38164640640")

    }

    @Test
    fun setNormalizedPhoneNmb() {
    }
}