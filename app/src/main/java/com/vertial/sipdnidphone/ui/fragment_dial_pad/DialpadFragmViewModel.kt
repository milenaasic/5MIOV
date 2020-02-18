package com.vertial.sipdnidphone.ui.fragment_dial_pad

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.sipdnidphone.data.Repo

private val MYTAG="MY_DialPadVIewMOdel"
class DialpadFragmViewModel(myRepository: Repo, application: Application) : AndroidViewModel(application) {


    val myPrenumber=myRepository.getPremunber()



}