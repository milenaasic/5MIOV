package com.adinfinitum.hello

import android.telephony.PhoneNumberUtils
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun phoneNumberForPhoneBook() {

        val number="064111222"

        /*val resultPhoneNumber=number.removePlus().removeDoubleZeroAtBegining().removeFirstZeroAddPrefix(
            NIGERIAN_PREFIX
        )
        println(resultPhoneNumber)
        assertEquals("238564564", resultPhoneNumber)*/
    }

    @Test
    fun checkNormalization() {

        val number="9000"

        val resultPhoneNumber=PhoneNumberUtils.normalizeNumber(number)
        println(resultPhoneNumber)
        //assertEquals("null", resultPhoneNumber)
    }
}
