package com.vertial.fivemiov.ui.fragment_dial_pad

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.model.RecentCall
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import javax.xml.transform.sax.TemplatesHandler

private val MYTAG="MY_DialPadVIewMOdel"
class DialpadFragmViewModel(val myRepository: RepoContacts, application: Application) : AndroidViewModel(application) {


    val myPrenumber=myRepository.getPremunber()

    //live data from database
    val userData=myRepository.getUserData()

    val recentCallList=myRepository.getAllRecentCalls()


    val getCreditNetSuccess=myRepository.getCredit_NetSuccess
    val getCreditNetworkError=myRepository.getCredit_NetError

    //logging out zbog token mismatch
    val loggingOut=myRepository.loggingOut
    fun resetLoggingOutToFalse(){
        myRepository.resetLoggingOutToFalse()
    }

    fun getCredit() {

        var myToken = ""
        var myPhoneNumber = ""
        viewModelScope.launch {
            val defUser = async(Dispatchers.IO) {
                myRepository.getUser()
            }
            try {
                val user = defUser.await()
                myToken = user.userToken
                myPhoneNumber = user.userPhone
            } catch (t: Throwable) {
                Log.i(MYTAG, "DB error ${t.message} ")
            }


            if (myToken.isNotEmpty() && myPhoneNumber.isNotEmpty()) {

                viewModelScope.launch {
                        myRepository.getCredit(phone = myPhoneNumber, token = myToken)

                }


            }

        }
    }

    fun resetGetCreditNetSuccess(){
        myRepository.resetGetCreditNetSuccess()
    }

    fun resetGetCreditNetErrorr(){
        myRepository.resetGetCreditNetError()
    }

    fun insertCallIntoDB(call: RecentCall){
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                myRepository.insertRecentCall(call)
            }
        }


    }


}