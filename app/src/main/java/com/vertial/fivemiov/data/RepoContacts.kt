package com.vertial.fivemiov.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vertial.fivemiov.api.MyAPIService
import com.vertial.fivemiov.api.NetRequest_ExportPhonebook
import com.vertial.fivemiov.api.NetRequest_GetCurrentCredit
import com.vertial.fivemiov.api.NetResponse_GetCurrentCredit
import com.vertial.fivemiov.database.MyDatabaseDao
import com.vertial.fivemiov.model.*
import com.vertial.fivemiov.utils.EMPTY_CONTACT_ITEM
import com.vertial.fivemiov.utils.EMPTY_EMAIL
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER
import com.vertial.fivemiov.utils.EMPTY_TOKEN
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.conscrypt.OpenSSLCipherRSA
import org.json.JSONObject
import java.util.*

private const val MY_TAG="MY_ContactsRepository"
class RepoContacts (val contentResolver: ContentResolver,
                    val myDatabaseDao: MyDatabaseDao,
                    val myAPIService: MyAPIService,
                    val mobileAppVer:String="0.0") {

    //User Live Data
    fun getUserData() = myDatabaseDao.getUser()

    // Live Data
    fun getPremunber() = myDatabaseDao.getPrenumber()

    //set sharedPref TO false because of Log out
    private val _loggingOut= MutableLiveData<Boolean>()
    val loggingOut: LiveData<Boolean>
        get() = _loggingOut

    //getCredit DialPad fragment network response
    private val _getCredit_NetSuccess= MutableLiveData<NetResponse_GetCurrentCredit?>()
    val getCredit_NetSuccess: LiveData<NetResponse_GetCurrentCredit?>
        get() = _getCredit_NetSuccess

    private val _getCredit_NetError= MutableLiveData<String?>()
    val getCredit_NetError: LiveData<String?>
        get() = _getCredit_NetError


    //initial export phonebook network response
    private val _initialexportPhoneBookNetworkSuccess = MutableLiveData<Boolean>()
    val initialexportPhoneBookNetworkSuccess: LiveData<Boolean>
        get() = _initialexportPhoneBookNetworkSuccess

    // export phonebook from webview network response
    private val _exportPhoneBookWebViewNetworkSuccess = MutableLiveData<Boolean>()
    val exportPhoneBookWebViewNetworkSuccess: LiveData<Boolean>
        get() = _exportPhoneBookWebViewNetworkSuccess



    suspend fun getUser()=withContext(Dispatchers.IO){
            myDatabaseDao.getUserNoLiveData()

    }

    //reset Logging Out
    fun resetLoggingOutToFalse(){
        _loggingOut.value=false
    }

    //getCredit DialPad fragment
    suspend fun getCredit(phone:String,token:String){


        val deferredRes = myAPIService.getCurrentCredit(
            phoneNumber = phone,
            signature = produceJWtToken(
                    Pair(Claim.TOKEN.myClaim,token),
                    Pair(Claim.PHONE.myClaim,phone)
            ),
            mobileAppVersion = mobileAppVer,
            request = NetRequest_GetCurrentCredit(authToken= token,phoneNumber= phone)
        )
        try {
            val result = deferredRes.await()
            if (result.authTokenMismatch == true) {
                _loggingOut.value=true
                coroutineScope {
                    withContext(Dispatchers.IO) {
                        logoutAll(myDatabaseDao)
                    }
                }
            } else {
                if(result.success==true && !result.e1phone.isNullOrEmpty()){
                   coroutineScope {
                       withContext(Dispatchers.IO){
                                myDatabaseDao.updatePrenumber(result.e1phone, System.currentTimeMillis())
                       }
                   }
                }
                _getCredit_NetSuccess.value=result
            }

        } catch (t: Throwable) {
            val errorMessage:String?=t.message
            GlobalScope.launch {
                withContext(IO){
                    SendErrorrToServer( myAPIService,phone,"getCredit $phone, $token",t.message.toString()).sendError()
                } }
            _getCredit_NetError.value=t.toString()
            Log.i(MY_TAG, "network greska je ${t.message}")
        }
    }

    //getCredit DialPadFragment funkcije resetovanja stanja
    fun resetGetCreditNetSuccess(){
        _getCredit_NetSuccess.value=null
    }

    fun resetGetCreditNetError(){
        _getCredit_NetError.value=null
    }



    suspend fun exportPhoneBook(token:String, phoneNumber:String,phoneBook:List<PhoneBookItem>,initialExport:Boolean=false){
        Log.i(MY_TAG, "EXPORTING PHONEBOOK")

            val deferredRes = myAPIService.exportPhoneBook(
                phoneNumber=phoneNumber,
                signature = produceJWtTokenWithArrayInput(
                    inputArray=Pair(Claim.PHONEBOOK.myClaim,phoneBook.toTypedArray()),
                    claimsAndValues1 = Pair(Claim.TOKEN.myClaim,token),
                    claimsAndValues2 = Pair(Claim.PHONENUMBER.myClaim,phoneNumber)
                ),
                mobileAppVersion = mobileAppVer,
                request = NetRequest_ExportPhonebook(
                    token,
                    phoneNumber,
                    phoneBook.toTypedArray()
                )
            )
            try {
                val result = deferredRes.await()

                if (result.authTokenMismatch == true) {
                    _loggingOut.value=true
                    coroutineScope {
                        withContext(Dispatchers.IO) {
                            logoutAll(myDatabaseDao)
                        }
                    }
                }
                else {
                    if (initialExport) _initialexportPhoneBookNetworkSuccess.value = true
                    else _exportPhoneBookWebViewNetworkSuccess.value = true
                    Log.i(MY_TAG, "EXPORTING PHONEBOOK SUCCESS")
                }

            } catch (t: Throwable) {
                    Log.i(MY_TAG, "EXPORTING PHONEBOOK FAILURE")
                    Log.i(MY_TAG, "network greska je ${t.message}")
                GlobalScope.launch {
                    withContext(IO){
                        SendErrorrToServer(myAPIService,phoneNumber,"exportPhoneBook $phoneNumber, $token, $phoneBook, $initialExport",t.message.toString()).sendError()
                    }
                }
            }
    }

    fun initialPhoneBookExportFinished(){
        _initialexportPhoneBookNetworkSuccess.value=false
    }

    fun phoneBookExportFinishedFromWebView(){
        _exportPhoneBookWebViewNetworkSuccess.value=false
    }


     fun getE1Timestamp()= myDatabaseDao.getE1Timestamp()

    fun refreshE1(phoneNumber: String,token:String){
        if(token.isNotEmpty() && phoneNumber.isNotEmpty()) {

            val myE1Job = UncancelableJob(  phone=phoneNumber,
                                            resultAuthorization = null,
                                            resultSetAccountEmailAndPass = null,
                                            myDatabaseDao = myDatabaseDao,
                                            myAPI = myAPIService,
                                            mobileAppVer = mobileAppVer)

            GlobalScope.launch {
                withContext(IO) {
                    //delay(3000)
                    myE1Job.startRefreshE124HPassed(token)
                }
            }
        }else{
            Log.i(MY_TAG," token ili phone je prazan string ruta refresh E1")
        }
    }

    // ovo mi treba za detail fragment
     fun getInternationalPhoneNumbersForContact(lookUpKey:String):List<PhoneItem>{

        val CURSOR_ID=0
        val CURSOR_PHONE=1
        val CURSOR_PHONE_TYPE=2
        val CURSOR_PHOTO_URI=3
        val CURSOR_NORMALIZED_PHONE=4

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        )

        val SELECTION: String = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ? AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} != '') AND" +
                "(${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '+234%') " +
                "AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '234%') AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '0%') "

        val selectionArguments=arrayOf<String>(lookUpKey)

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            SELECTION,
            selectionArguments,
            null
        )

        if (cursor == null) return emptyList()

        val list = mutableListOf<PhoneItem>()

        try {
            while (cursor.moveToNext()) {
                list.add(
                    PhoneItem(
                        PhoneNumberUtils.normalizeNumber(cursor.getString(CURSOR_PHONE)),
                        cursor.getInt(CURSOR_PHONE_TYPE),
                        cursor.getString(CURSOR_PHOTO_URI)
                    )

                )
            }

        } finally {
            cursor.close();
        }
        Log.i(MY_TAG,"convert cursor u listu international phones $list")

        return list.distinctBy {it.phoneNumber}
    }

    //povlaci sve kontakte koji imaju interncionalni broj
    fun getAllRawContactWithInternPhoneNumber():List<ContactItem>{

        var resultList= mutableListOf<ContactItem>()

        val CURSOR_ID=0
        val CURSOR_DISPLAY_NAME_PRIMARY=1
        val CURSOR_LOOKUP_KEY=2
        val CURSOR_NUMBER=3
        val CURSOR_NORMALIZED_NUMBER=4
        val CURSOR_PHOTO_THUMBNAIL_URI=5

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI

        )

        val SELECTION: String = "(${ContactsContract.CommonDataKinds.Phone.NUMBER} != '') AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '+234%') " +
                "AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '234%') AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '0%') "

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            SELECTION,
            null,
            sortOrder
        )

        try {
            if(cursor!=null) {
                while (cursor.moveToNext()) {

                    resultList.add(
                                    ContactItem(
                                            name=cursor.getString(CURSOR_DISPLAY_NAME_PRIMARY),
                                            lookUpKey = cursor.getString(CURSOR_LOOKUP_KEY),
                                            photoThumbUri = cursor.getString(CURSOR_PHOTO_THUMBNAIL_URI)
                                            )
                    )
                    Log.i(
                        MY_TAG,
                        "svi telefoni u phone redovima, id je ${cursor.getString(0)}, name ${cursor.getString(
                            1
                        )}, key ${cursor.getString(2)}, number ${cursor.getString(3)}, " +
                                " normalizes number ${cursor.getString(4)}, photo ${cursor.getString(
                                    5
                                )}"
                    )

                }
            }

        } finally {
            cursor?.close();
        }
        Log.i(MY_TAG, "result lista je ${resultList}")
        val noDuplicatesList=resultList.toSet().toList()
        Log.i(MY_TAG, "no duplicates lista je ${noDuplicatesList}")
        return noDuplicatesList
    }


    //povlaci sve kontakte koji imaju interncionalni broj
    suspend fun getRawContactsPhonebook():List<PhoneBookItem>{

        var resultList= listOf<PhoneBookItem>()
        val rawListOfContactItemWithNumber= mutableListOf<ContactItemWithNumber>()

        val CURSOR_ID=0
        val CURSOR_DISPLAY_NAME_PRIMARY=1
        val CURSOR_LOOKUP_KEY=2
        val CURSOR_NORMALIZED_NUMBER=3
        val CURSOR_NUMBER=4

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val SELECTION: String = "(${ContactsContract.CommonDataKinds.Phone.NUMBER} != '') AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '+234%') " +
                "AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '234%') AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '0%') "

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            SELECTION,
            null,
            sortOrder
        )

        try {
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    //
                    rawListOfContactItemWithNumber.add(
                        ContactItemWithNumber(
                            name=cursor.getString(CURSOR_DISPLAY_NAME_PRIMARY),
                            lookUpKey = cursor.getString(CURSOR_LOOKUP_KEY),
                            internationalNumber = cursor.getString(CURSOR_NUMBER),
                            normalizedInternationalNumber =cursor.getString(CURSOR_NORMALIZED_NUMBER)?:""
                        )
                    )
                    Log.i(
                        MY_TAG,
                        "za phonebook raw contact  name ${cursor.getString(1)}, key ${cursor.getString(2)}, " +
                                "normalized number ${(cursor.getString(CURSOR_NORMALIZED_NUMBER))}"
                    )

                }
                Log.i(
                    MY_TAG,
                    "za phonebook raw contact list je $rawListOfContactItemWithNumber}"
                )
                if (!rawListOfContactItemWithNumber.isNullOrEmpty()) resultList=createPhoneBookList(rawListOfContactItemWithNumber.toSet().toList())

            }

        } finally {
            cursor?.close();
        }

        return resultList
    }

    private fun createPhoneBookList(rawList:List<ContactItemWithNumber>): List<PhoneBookItem> {

        val phoneBookItemsList= mutableListOf<PhoneBookItem>()

        for(element in rawList){
            val lookUpKey:String=element.lookUpKey
            val phoneList=rawList.filter {
                    it.lookUpKey==lookUpKey }
                    Log.i(MY_TAG," lista telefona za ${element.name} je $phoneList")
                    phoneBookItemsList.add(
                                PhoneBookItem(
                                element.name,
                                makePhoneArrayForSameContact(phoneList))
            )
        }

        Log.i(MY_TAG," lista phonebook telefona pre izbacivanja duuplih za ${phoneBookItemsList}")
        Log.i(MY_TAG," lista phonebook telefona POSLE izbacivanja duuplih za ${phoneBookItemsList.distinctBy { it.name }}")
        return phoneBookItemsList.distinctBy { it.name }
    }

    private fun makePhoneArrayForSameContact(list:List<ContactItemWithNumber>):Array<String>{
        val resultList= mutableListOf<String>()
        for(item in list){
            if(item.normalizedInternationalNumber.isEmpty()) resultList.add(item.internationalNumber)
            else resultList.add(item.normalizedInternationalNumber)
        }
        return resultList.toTypedArray()

    }

    //Recent calls
    fun getAllRecentCalls()=myDatabaseDao.getAllRecentCalls()

    fun insertRecentCall(call:RecentCall)=myDatabaseDao.insertRecentCall(call)

}



