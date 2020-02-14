package com.vertial.sipdnidphone.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.vertial.sipdnidphone.data.Repo


private const val MY_TAG="MY_MainActivViewModel"
class MainActivityViewModel(val myRepository: Repo, application: Application) : AndroidViewModel(application) {

    //live data from database
    val userData=myRepository.getUserData()

    init {
        Log.i(MY_TAG,("init"))
    }

}