package com.vertial.fivemiov.ui.fragment_main

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log


private val MY_TAG="MY_helper_func"

fun String.collorLetters(substringToColor:String,mycolor:Int):SpannableString?{

    //pronadji substring s i oboji ga
        val spannableString = SpannableString(this)

        val colorSpan = ForegroundColorSpan(mycolor)
        val startIndex: Int = this.toLowerCase().indexOf(substringToColor.toLowerCase())
        if (startIndex != -1) {
            val lastIndex: Int = startIndex + substringToColor.length
            spannableString.setSpan(colorSpan, startIndex, lastIndex, 0)
            return spannableString
        } else return null
}