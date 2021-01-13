package app.adinfinitum.ello.ui.registrationauthorization

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.adinfinitum.ello.data.FakeRepo
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.VERIFICATION_METHOD_SMS
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Ordering
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class RegAuthActivityViewModelTest {


    @Test
    fun registerButtonClicked_(){
        val context=ApplicationProvider.getApplicationContext<Application>()

        val regActViewModel=RegAuthActivityViewModel(FakeRepo(),context)
            val phone="38111222333"
            val smsResend=false
            val verificationMethod= VERIFICATION_METHOD_SMS

    }

    /*@Test
    fun setSMSVerificationTokenForAuthFragment_setToken_EventHandeled() {



    }*/
}