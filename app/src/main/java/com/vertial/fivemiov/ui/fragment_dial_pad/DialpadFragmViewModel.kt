package com.vertial.fivemiov.ui.fragment_dial_pad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vertial.fivemiov.data.Repo

private val MYTAG="MY_DialPadVIewMOdel"
class DialpadFragmViewModel(myRepository: Repo, application: Application) : AndroidViewModel(application) {


    val myPrenumber=myRepository.getPremunber()



}