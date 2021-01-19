package app.adinfinitum.ello.data


import app.adinfinitum.ello.model.PhoneBookItem
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm

private const val MY_TAG="MY_REPO_HELPERS"

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

    return myJWT.toString().split(".")[2]
}

fun produceJWtTokenWithArrayInput(inputArray:Pair<String,Array<PhoneBookItem>>, claimsAndValues1:Pair<String,String>,claimsAndValues2:Pair<String,String>):String{
    val myJwtBuilder2 = Jwts.builder()

    myJwtBuilder2.claim(claimsAndValues1.first,claimsAndValues1.second)
        .claim(claimsAndValues2.first,claimsAndValues2.second)

    myJwtBuilder2.claim(inputArray.first,inputArray.second)

    val myJWT2=myJwtBuilder2.signWith(SignatureAlgorithm.HS256, getMyWord().toByteArray()).compact()

    return myJWT2.toString().split(".")[2]
}

private fun getMyWord():String{
    val s1="RTGavqWT"
    val s2=s1+"^b9H!bM3"
    val s3=s2+"?2C+UuY]"
    return s3
}
