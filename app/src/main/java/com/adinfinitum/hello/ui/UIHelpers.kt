package com.adinfinitum.hello.ui

import android.app.Application
import android.content.Context
import com.adinfinitum.hello.utils.DEFAULT_SHARED_PREFERENCES
import com.adinfinitum.hello.utils.DISCLAIMER_WAS_SHOWN
import com.adinfinitum.hello.utils.PHONEBOOK_IS_EXPORTED

fun initializeSharedPrefToFalse(application: Application) {

    val sharedPreferences=application.getSharedPreferences(DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)

    sharedPreferences.edit().putBoolean(DISCLAIMER_WAS_SHOWN,false).apply()
    sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED,false).apply()

}