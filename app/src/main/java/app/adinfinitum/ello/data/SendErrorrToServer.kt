package app.adinfinitum.ello.data

import android.util.Log
import app.adinfinitum.ello.api.MyAPIService

class SendErrorrToServer(val myAPIService: MyAPIService,val phoneNumber:String,val process:String, val errorMsg:String) {

   suspend fun sendError(){

        val def=myAPIService.sendErrorToServer(phoneNumber=phoneNumber,process = process,errorMsg= errorMsg)
        try {
            val defResponse=def.await()
        }catch (t:Throwable){
            Log.i("MY_Send Error To Server","${t.message}")

        }
    }
}