package app.adinfinitum.ello.data

import app.adinfinitum.ello.database.MyDatabasePrenumberAndWebApiVersion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepoPrenumberAndWebApiVer(val myDatabasePrenumberAndWebApiVer: MyDatabasePrenumberAndWebApiVersion,
                                val dispatcher: CoroutineDispatcher=Dispatchers.IO
                                ){

    //LiveData
    fun getPremunber() = myDatabasePrenumberAndWebApiVer.getPrenumber()

    suspend fun getWebAppVersion():String{
        return withContext(dispatcher) {
                    myDatabasePrenumberAndWebApiVer.getWebApiVersion()
                }
    }


    suspend fun updatePrenumber(e1Phone:String, timestamp:Long){
        withContext(dispatcher){
            myDatabasePrenumberAndWebApiVer.updatePrenumber(prenumber = e1Phone,timestamp = timestamp)
        }
    }

    suspend fun updateWebApiVersion(webApiVer:String){
        withContext(dispatcher){
            myDatabasePrenumberAndWebApiVer.updateWebApiVersion(webApiVer =webApiVer)
        }
    }

}