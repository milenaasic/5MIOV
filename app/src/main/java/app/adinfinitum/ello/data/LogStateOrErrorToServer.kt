package app.adinfinitum.ello.data

import app.adinfinitum.ello.api.MyAPIDataService

import app.adinfinitum.ello.database.MyDatabaseDao

interface LogStateOrErrorToServer {

    suspend fun logStateOrErrorToOurServer(
        phoneNumber:String="",
        myDatabaseDao: MyDatabaseDao,
        myAPIService: MyAPIDataService,
        myoptions: Map<String, String>
    ) {

        /*if(phoneNumber.isEmpty()) {
            val user = myDatabaseDao.getUserNoLiveData()
            myAPIService.logStateOrErrorToServer(phoneNumber = user.userPhone, options = myoptions)

        }else myAPIService.logStateOrErrorToServer(phoneNumber =phoneNumber, options = myoptions)*/


    }
}