package com.adinfinitum.hello.ui.fragment_main

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import java.util.*


private val MY_TAG="MY_helper_func"

fun String.collorLetters(substringToColor:String,mycolor:Int):SpannableString?{

    //find substring and color it
        val spannableString = SpannableString(this)

        val colorSpan = ForegroundColorSpan(mycolor)
        val startIndex: Int = this.toLowerCase(Locale.getDefault()).indexOf(substringToColor.toLowerCase(
            Locale.getDefault()))
        if (startIndex != -1) {
            val lastIndex: Int = startIndex + substringToColor.length
            spannableString.setSpan(colorSpan, startIndex, lastIndex, 0)
            return spannableString
        } else return null
}