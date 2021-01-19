package app.adinfinitum.ello.data

import app.adinfinitum.ello.api.MyAPILogToServer
import app.adinfinitum.ello.database.MyDatabaseUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IRepoLogToServer {
    suspend fun logStateOrErrorToServer(phoneNumber:String="",myoptions: Map<String, String>)
}

class RepoLogToServer (
        val myDatabaseUser: MyDatabaseUser,
        val myAPILogToServer:MyAPILogToServer,
        val dispatcher: CoroutineDispatcher=Dispatchers.IO
        )

    : IRepoLogToServer {
        override suspend fun logStateOrErrorToServer(phoneNumber:String,myoptions: Map<String, String>) {
            withContext(dispatcher){
               if(phoneNumber.isEmpty()){
                    val user = myDatabaseUser.getUserNoLiveData()
                    myAPILogToServer.logStateOrErrorToServer(phoneNumber = user.userPhone, options = myoptions)
               }else {
                    myAPILogToServer.logStateOrErrorToServer(phoneNumber = phoneNumber, options = myoptions)
               }
            }

        }

    }