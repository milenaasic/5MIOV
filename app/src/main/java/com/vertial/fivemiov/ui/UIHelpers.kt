package com.vertial.fivemiov.ui

import android.app.Application
import android.content.Context
import com.vertial.fivemiov.utils.DEFAULT_SHARED_PREFERENCES
import com.vertial.fivemiov.utils.DISCLAIMER_WAS_SHOWN
import com.vertial.fivemiov.utils.PHONEBOOK_IS_EXPORTED

fun initializeSharedPrefToFalse(application: Application) {
    val sharedPreferences=application.getSharedPreferences(DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    //ako postoji promenljiva dislaimer_was_shown stavi je na false, ako ne napravi je i stavi na false
    sharedPreferences.edit().putBoolean(DISCLAIMER_WAS_SHOWN,false).apply()
    sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED,false).apply()

}