package app.adinfinitum.ello.ui.fragment_set_email_and_password

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adinfinitum.ello.data.*
import app.adinfinitum.ello.ui.registrationauthorization.Event
import app.adinfinitum.ello.utils.EMPTY_PHONE_NUMBER
import app.adinfinitum.ello.utils.EMPTY_TOKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.Exception

private val MYTAG="MY_SetEmailPass_VIEWMOD"

class SetEmailPassFragmentViewModel(val myrepository: Repo,
                                    val myRepoUser: IRepoUser,
                                    val myRepoPrenumberAndWebApiVer: IRepoPrenumberAndWebApiVer,
                                    val myRepoRemoteDataSource: IRepoRemoteDataSource,
                                    val myRepoLogToServer:IRepoLogToServer,
                                    application: Application) : AndroidViewModel(application) {

    val NAVIGATE_TO_DIALPAD_FRAGMENT = 1

    private val _setAccountEmailAndPassResult= MutableLiveData<Event<SetAccountEmailAndPassResult>>()
    val setAccountEmailAndPassResult: LiveData<Event<SetAccountEmailAndPassResult>>
        get() = _setAccountEmailAndPassResult

    fun setAccountAndEmailForUser(email:String,password:String){

            viewModelScope.launch {
                try {
                    val myUser = myRepoUser.getUser()
                    if( myUser.userPhone.isNotEmpty() && myUser.userPhone!= EMPTY_PHONE_NUMBER
                        && myUser.userToken.isNotEmpty() && myUser.userToken!= EMPTY_TOKEN) {

                        try {
                            val result =
                                myRepoRemoteDataSource.setAccountEmailAndPasswordForUser(
                                    phoneNumber = myUser.userPhone,
                                    token = myUser.userToken,
                                    email = email,
                                    password = password
                                )

                            when(result){
                                is Result.Success->{
                                   try {

                                       if (result.data.email.isNotEmpty() && result.data.email.isNotBlank()) myRepoUser.updateUserEmail(result.data.email)
                                       if (!result.data.appVersion.isNullOrEmpty() || !result.data.appVersion.isNullOrBlank())
                                                    myRepoPrenumberAndWebApiVer.updateWebApiVersion(result.data.appVersion)

                                       when(result.data.success){
                                            true->{
                                                _setAccountEmailAndPassResult.value=Event(SetAccountEmailAndPassResult(
                                                    navigateToFragment = NAVIGATE_TO_DIALPAD_FRAGMENT,
                                                    showToastMessage = result.data.userMsg)
                                                    )
                                            }
                                            false->{
                                                _setAccountEmailAndPassResult.value=Event(SetAccountEmailAndPassResult(
                                                    showSnackBarMessage = result.data.userMsg
                                                   )
                                                )
                                            }
                                       }

                                   }catch (e:Exception){
                                        Log.e(MYTAG,"insertion into DB failed, ${e.message}")
                                   }

                                }
                                is Result.Error->{
                                            _setAccountEmailAndPassResult.value=Event(SetAccountEmailAndPassResult(showSnackBarErrorMessage = true))

                                            myRepoLogToServer.logStateOrErrorToServer(myoptions =
                                                    mapOf(
                                                        Pair("process","setAccountEmailAndPasswordForUser"),
                                                        Pair("error",result.exception.message?:"")
                                                    )
                                            )

                                }
                            }

                        }catch (e:Exception){
                            Log.i(MYTAG,"set email and account for user ${e.message}")
                        }
                    }

                } catch (e: Exception) {
                    Log.i(MYTAG,"DB error ${e.message}")
                }

            }
    }



}
