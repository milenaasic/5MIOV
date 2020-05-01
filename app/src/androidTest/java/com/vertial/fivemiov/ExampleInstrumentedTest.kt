package com.vertial.fivemiov

import android.telephony.PhoneNumberUtils
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vertial.fivemiov.utils.NIGERIAN_PREFIX
import com.vertial.fivemiov.utils.removeDoubleZeroAtBegining
import com.vertial.fivemiov.utils.removeFirstZeroAddPrefix
import com.vertial.fivemiov.utils.removePlus

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.vertial.fivemiov", appContext.packageName)
    }



}
