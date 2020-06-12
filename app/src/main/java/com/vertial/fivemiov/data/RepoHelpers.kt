package com.vertial.fivemiov.data

import android.util.Base64
import android.util.Log
import com.vertial.fivemiov.database.MyDatabaseDao
import com.vertial.fivemiov.model.PhoneBookItem
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.TextCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


private const val MY_TAG="MY_REPO_HELPERS"
suspend fun logoutAll( myDatabaseDao: MyDatabaseDao){

    coroutineScope {

        val deferreds = listOf(
            async(Dispatchers.IO) { myDatabaseDao.logoutE1Table() },
            async(Dispatchers.IO) {  myDatabaseDao.logoutSipAccount() },
            async(Dispatchers.IO) { myDatabaseDao.logoutWebApiVersion() },
            async(Dispatchers.IO) { myDatabaseDao.logoutRecentCalls() }
        )

        try {
            val result=deferreds.awaitAll()

            Log.i(MY_TAG,"user data before logging out ${myDatabaseDao.getUserNoLiveData()}")
            val defa=async (Dispatchers.IO) {  myDatabaseDao.logoutUser()}
            defa.await()
            Log.i(MY_TAG,"user data after logging out ${myDatabaseDao.getUserNoLiveData()}")

        }catch(t:Throwable){
            Log.i(MY_TAG, "Error logging out ${t.message}")
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

    return myJWT
}

fun produceJWtTokenWithArrayInput(inputArray:Pair<String,Array<PhoneBookItem>>, claimsAndValues1:Pair<String,String>,claimsAndValues2:Pair<String,String>):String{
    val myJwtBuilder2 = Jwts.builder()

    myJwtBuilder2.claim(claimsAndValues1.first,claimsAndValues1.second)
        .claim(claimsAndValues2.first,claimsAndValues2.second)

    myJwtBuilder2.claim(inputArray.first,inputArray.second)

    val myJWT2=myJwtBuilder2.signWith(SignatureAlgorithm.HS256, getMyWord().toByteArray()).compact()

    return myJWT2
}

private fun getMyWord():String{
    val s1="RTGavqWT"
    val s2=s1+"^b9H\\bM3"
    val s3=s2+"?2C+UuY]"
    return s3
}