package app.adinfinitum.ello.data

import androidx.lifecycle.LiveData
import app.adinfinitum.ello.database.MyDatabasePrenumberAndWebApiVersion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IRepoPrenumberAndWebApiVer {

    fun getPremunber(): LiveData<String>

    suspend fun getWebAppVersion(): String
    suspend fun updatePrenumber(e1Phone: String, timestamp: Long)
    suspend fun updateWebApiVersion(webApiVer: String)
}

class RepoPrenumberAndWebApiVer(val myDatabasePrenumberAndWebApiVer: MyDatabasePrenumberAndWebApiVersion,
                                val dispatcher: CoroutineDispatcher=Dispatchers.IO
                                ) : IRepoPrenumberAndWebApiVer {

    //LiveData
    override fun getPremunber() = myDatabasePrenumberAndWebApiVer.getPrenumber()

    override suspend fun getWebAppVersion():String{
        return withContext(dispatcher) {
                    myDatabasePrenumberAndWebApiVer.getWebApiVersion()
                }
    }


    override suspend fun updatePrenumber(e1Phone:String, timestamp:Long){
        withContext(dispatcher){
            myDatabasePrenumberAndWebApiVer.updatePrenumber(prenumber = e1Phone,timestamp = timestamp)
        }
    }

    override suspend fun updateWebApiVersion(webApiVer:String){
        withContext(dispatcher){
            myDatabasePrenumberAndWebApiVer.updateWebApiVersion(webApiVer =webApiVer)
        }
    }

}