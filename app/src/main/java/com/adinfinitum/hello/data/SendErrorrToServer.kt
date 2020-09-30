package com.adinfinitum.hello.data

import android.util.Log
import com.adinfinitum.hello.api.MyAPIService

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