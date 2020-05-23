package com.vertial.fivemiov.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
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
import java.util.*

private const val MY_TAG="MY_ContactsRepository"
class RepoContacts (val contentResolver: ContentResolver,val myDatabaseDao: MyDatabaseDao, val myAPIService: MyAPIService) {

    //User Live Data
    fun getUserData() = myDatabaseDao.getUser()

    // Live Data
    fun getPremunber() = myDatabaseDao.getPrenumber()

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

    //getCredit DialPad fragment
    suspend fun getCredit(phone:String,token:String){

        val deferredRes = myAPIService.getCurrentCredit(
            request = NetRequest_GetCurrentCredit(authToken= token,phoneNumber= phone)
        )
        try {
            val result = deferredRes.await()
            if (result.authTokenMismatch == true) {
                coroutineScope {
                    withContext(Dispatchers.IO) {
                    Log.i(MY_TAG," usao u funkciju mismatch je true")
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
                    SendErrorrToServer( myAPIService,"getCredit $phone, $token",t.message.toString()).sendError()
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



    fun getAllContacts(uri: Uri):List<ContactItem>{

        val CURSOR_ID = 0
        val CURSOR_LOOKUP_KEY = 1
        val CURSOR_NAME = 2
        val CURSOSR_PHOTO_THUMBNAIL_URI = 3
        val CURSOR_HAS_PHONE_NUMBER = 4

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        val SELECTION: String =
            ("((${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} NOTNULL) AND (${ContactsContract.Contacts.HAS_PHONE_NUMBER} =1) " +
                    "AND (${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY}!= '' ))")

        val cursor = contentResolver.query(
            uri,
            PROJECTION,
            SELECTION,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        if (cursor == null) return emptyList()
        else{
            val list = mutableListOf<ContactItem>()

            try {
                while (cursor.moveToNext()) {
                list.add(
                    ContactItem(
                        cursor.getLong(CURSOR_ID),
                        cursor.getString(CURSOR_LOOKUP_KEY),
                        cursor.getString(CURSOR_NAME),
                        cursor.getString(CURSOSR_PHOTO_THUMBNAIL_URI),
                        cursor.getString(CURSOR_HAS_PHONE_NUMBER)
                    )
                )
            }
            } finally {
                cursor.close();
            }

        Log.i(MY_TAG, "convert cursor u listu contacts $list")


            return list
        }

    }


     fun getPhoneNumbersForContact(lookUpKey:String):List<PhoneItem>{

        val CURSOR_ID=0
        val CURSOR_PHONE=1
        val CURSOR_PHONE_TYPE=2
        val CURSOR_PHOTO_URI=3
        val CURSOR_PHOTO_FILE_ID=4

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID
        )

        val SELECTION: String = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ?"

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
                        cursor.getString(CURSOR_PHONE),
                        cursor.getInt(CURSOR_PHONE_TYPE),
                        cursor.getString(CURSOR_PHOTO_URI),
                        cursor.getString(CURSOR_PHOTO_FILE_ID)
                    )

                )
            }

        } finally {
            cursor.close();
        }
        Log.i(MY_TAG,"convert cursor u listu get phones $list")

        return list

    }

    suspend fun exportPhoneBook(token:String, phoneNumber:String,phoneBook:List<PhoneBookItem>,initialExport:Boolean=false){
        Log.i(MY_TAG, "EXPORTING PHONEBOOK")

            val deferredRes = myAPIService.exportPhoneBook(
                request = NetRequest_ExportPhonebook(
                    token,
                    phoneNumber,
                    phoneBook.toTypedArray()
                )
            )
            try {
                val result = deferredRes.await()

                if (result.authTokenMismatch == true) logoutAll(myDatabaseDao)
                else {
                    if (initialExport) _initialexportPhoneBookNetworkSuccess.value = true
                    else _exportPhoneBookWebViewNetworkSuccess.value = true
                    Log.i(MY_TAG, "EXPORTING PHONEBOOK SUCCESS")
                }

            } catch (t: Throwable) {
                    Log.i(MY_TAG, "EXPORTING PHONEBOOK FAILURE")
                    Log.i(MY_TAG, "network greska je ${t.message}")
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

            val myE1Job = UncancelableJob(phone=phoneNumber,resultAuthorization = null, resultSetAccountEmailAndPass = null, myDatabaseDao = myDatabaseDao, myAPI = myAPIService)

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
        val CURSOR_PHOTO_FILE_ID=4

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID
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
                        cursor.getString(CURSOR_PHONE),
                        cursor.getInt(CURSOR_PHONE_TYPE),
                        cursor.getString(CURSOR_PHOTO_URI),
                        cursor.getString(CURSOR_PHOTO_FILE_ID)
                    )

                )
            }

        } finally {
            cursor.close();
        }
        Log.i(MY_TAG,"convert cursor u listu international phones $list")

        return list

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


        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            SELECTION,
            null,
            null
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
        Log.i(MY_TAG," broj raw kontakata pre prebacivanja u set je ${resultList.size}, a posle je ${resultList.toSet().toList().size}")
        Log.i(MY_TAG, "result lista je ${resultList}")
        val noDuplicatesList=resultList.toSet().toList()
        Collections.sort(noDuplicatesList,Comparator { t, t2 -> t.name.toLowerCase().compareTo(t2.name.toLowerCase()) })
        return noDuplicatesList
    }

}



