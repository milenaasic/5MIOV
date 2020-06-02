package com.vertial.fivemiov.data

import android.util.Log
import com.vertial.fivemiov.database.MyDatabaseDao
import com.vertial.fivemiov.model.PhoneBookItem
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


private const val MY_TAG="MY_REPO_HELPERS"
suspend fun logoutAll( myDatabaseDao: MyDatabaseDao){

    coroutineScope {
        Log.i(MY_TAG, "e1 tabele je ${myDatabaseDao.getAllE1()}, sip ${myDatabaseDao.getAllSip()}, webapi ${myDatabaseDao.getWebApiVersion()}")

        val deferreds = listOf(
            async(Dispatchers.IO) { myDatabaseDao.logoutE1Table() },
            async(Dispatchers.IO) {  myDatabaseDao.logoutSipAccount() },
            async(Dispatchers.IO) { myDatabaseDao.logoutWebApiVersion() },
            async(Dispatchers.IO) { myDatabaseDao.logoutRecentCalls() }
        )

        try {
            val result=deferreds.awaitAll()
            Log.i(MY_TAG, "e1 tabele je ${myDatabaseDao.getAllE1()}, sip ${myDatabaseDao.getAllSip()}, webapi ${myDatabaseDao.getWebApiVersion()}")
            Log.i(MY_TAG, "logour tri tabele je $result")
            Log.i(MY_TAG,"pre user logouta ${myDatabaseDao.getUserNoLiveData()}")
            val defa=async (Dispatchers.IO) {  myDatabaseDao.logoutUser()}
            defa.await()
            Log.i(MY_TAG,"posle user logouta ${myDatabaseDao.getUserNoLiveData()}")

        }catch(t:Throwable){
            Log.i(MY_TAG, "greska prilikom logaouta 3 tabele je ${t.message}")
        }

    }
}


enum class Claim (val myClaim:String){
        NUMBER("number"),
        PHONE("phone"),
        PHONENUMBER("phoneNumber"),
        EMAIL("email"),
        TOKEN (myClaim = "token"),
        AUTH_TOKEN (myClaim = "authToken"),
        PASSWORD("password"),
        SIGN_IN("signIn"),
        FORCE_RESET("forceReset"),
        PHONEBOOK("phoneBook")
}
val CLAIM_VALUE_1=1
val CLAIM_VALUE_TRUE:Boolean=true
val CLAIM_VALUE_FALSE:Boolean=false


fun produceJWtToken(vararg claimsAndValues:Pair<String,Any>):String{
    val myJwtBuilder = Jwts.builder()

    for(item in claimsAndValues){
        if(item.first.isNotEmpty() ){
                myJwtBuilder.claim(item.first,item.second)
        }
    }

    val myJWT=myJwtBuilder.signWith(SignatureAlgorithm.HS256, getMyWord().toByteArray()).compact()

    /*.claim("name","5MIOV").claim("password","tester")
        .signWith(SignatureAlgorithm.HS256, "secret".toByteArray())
        .compact()*/

    return myJWT
}

fun produceJWtTokenWithArrayInput(inputArray:Pair<String,Array<PhoneBookItem>>, claimsAndValues1:Pair<String,String>,claimsAndValues2:Pair<String,String>):String{
    val myJwtBuilder = Jwts.builder()

    myJwtBuilder.claim(claimsAndValues1.first,claimsAndValues1.second)
        .claim(claimsAndValues2.first,claimsAndValues2.second)

    myJwtBuilder.claim(inputArray.first,inputArray.second)

    val myJWT=myJwtBuilder.signWith(SignatureAlgorithm.HS256, getMyWord().toByteArray()).compact()

    return myJWT
}

private fun getMyWord():String{
    val s1="RTGavqWT"
    val s2=s1+"^b9H\\bM3"
    val s3=s2+"?2C+UuY]"
    return s3
}