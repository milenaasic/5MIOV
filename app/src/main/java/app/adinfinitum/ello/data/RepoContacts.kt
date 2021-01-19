package app.adinfinitum.ello.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.database.MyDatabaseDao
import app.adinfinitum.ello.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

private const val MY_TAG="MY_CONTACTS_REPOSITORY"
class RepoContacts (val contentResolver: ContentResolver,
                    val myDatabaseDao: MyDatabaseDao,
                    val myAPIService: MyAPIDataService):LogStateOrErrorToServer {

    //User Live Data
    fun getUserData() = myDatabaseDao.getUser()

    // Live Data
    fun getPremunber() = myDatabaseDao.getPrenumber()


    fun getUser()= myDatabaseDao.getUserNoLiveData()

    // About Fragment
    fun getWebAppVersion()=myDatabaseDao.getWebApiVersion()

    //DialPad fragment
    suspend fun getCredit(phone:String,token:String):Result<NetResponse_GetCurrentCredit>{

        try {
                val result = myAPIService.getCurrentCredit(
                    phoneNumber = phone,
                    signature = produceJWtToken(
                            Pair(Claim.TOKEN.myClaim,token),
                            Pair(Claim.PHONE.myClaim,phone)
                    ),
                    request = NetRequest_GetCurrentCredit(authToken= token,phoneNumber= phone)
                ).await()

                return Result.Success(result)

            } catch (e: Exception) {
                return Result.Error(e)
        }
    }

    fun updatePrenumber(e1Phone:String, timestamp:Long){
        myDatabaseDao.updatePrenumber(prenumber = e1Phone,timestamp = timestamp)
    }

    fun updateWebApiVersion(webApiVer:String){
        myDatabaseDao.updateWebApiVersion(webApiVer =webApiVer )
    }




    suspend fun exportPhoneBook(token:String, phoneNumber:String,phoneBook:List<PhoneBookItem>):Result<NetResponse_ExportPhonebook>{
        Log.i(MY_TAG, "EXPORTING PHONEBOOK")

            try {

                val result = myAPIService.exportPhoneBook(
                    phoneNumber=phoneNumber,
                    signature = produceJWtTokenWithArrayInput(
                        inputArray=Pair(Claim.PHONEBOOK.myClaim,phoneBook.toTypedArray()),
                        claimsAndValues1 = Pair(Claim.TOKEN.myClaim,token),
                        claimsAndValues2 = Pair(Claim.PHONENUMBER.myClaim,phoneNumber)
                    ),
                    request = NetRequest_ExportPhonebook(
                        token,
                        phoneNumber,
                        phoneBook.toTypedArray()
                    )
                ).await()

                  return Result.Success(result)

            } catch (e: Exception) {
                Log.i(MY_TAG, "EXPORTING PHONEBOOK FAILURE")
                return Result.Error(e)
            }
    }



    //Detail fragment
    fun getPhoneNumbersForContact(lookUpKey:String):List<PhoneItem>{

        val CURSOR_ID=0
        val CURSOR_PHONE=1
        val CURSOR_PHONE_TYPE=2
        val CURSOR_PHOTO_URI=3

        val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
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
                        PhoneNumberUtils.normalizeNumber(cursor.getString(CURSOR_PHONE))?:(cursor.getString(CURSOR_PHONE)),
                        cursor.getInt(CURSOR_PHONE_TYPE),
                        cursor.getString(CURSOR_PHOTO_URI),

                    )

                )
            }
        } finally {
            cursor.close();
        }
        Log.i(MY_TAG,"convert cursor u listu $list")

        return list.distinctBy {it.phoneNumber}

    }

    // ovo mi treba za detail fragment
     /*fun getInternationalPhoneNumbersForContact(lookUpKey:String):List<PhoneItem>{

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

        return list.distinctBy {it.phoneNumber}
    }*/


    //Retreive all contacts from phonebook, no filters
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

            Log.i(MY_TAG, "convert cursor u listu $list")


            return list
        }

    }


    //all contacts with international numbers
    /*fun getAllRawContactWithInternPhoneNumber():List<ContactItem>{

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

        //querry for specific phone numbers
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

                }
            }

        } finally {
            cursor?.close();
        }
        Log.i(MY_TAG, "contacts list  ${resultList}")
        val noDuplicatesList=resultList.toSet().toList()
        Log.i(MY_TAG, "no duplicates contacts list ${noDuplicatesList}")
        return noDuplicatesList
    }*/


    // Called from WebViewViewModel and MainFragment when exporting phonebook to server
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

        /*val SELECTION: String = "(${ContactsContract.CommonDataKinds.Phone.NUMBER} != '') AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '+234%') " +
                "AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '234%') AND (${ContactsContract.CommonDataKinds.Phone.NUMBER} NOT LIKE '0%') "*/

        val SELECTION: String =
            ("((${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} NOTNULL) AND (${ContactsContract.Contacts.HAS_PHONE_NUMBER} =1) " +
                    "AND (${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY}!= '' ))")


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

                    rawListOfContactItemWithNumber.add(
                        ContactItemWithNumber(
                            name=cursor.getString(CURSOR_DISPLAY_NAME_PRIMARY),
                            lookUpKey = cursor.getString(CURSOR_LOOKUP_KEY),
                            internationalNumber = cursor.getString(CURSOR_NUMBER),
                            normalizedInternationalNumber =cursor.getString(CURSOR_NORMALIZED_NUMBER)?:""
                        )
                    )


                }
                Log.i(
                    MY_TAG,
                    " phonebook raw contact list with duplicates $rawListOfContactItemWithNumber}"
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

                    phoneBookItemsList.add(
                                PhoneBookItem(
                                element.name,
                                makePhoneArrayForSameContact(phoneList))
            )
        }

        return phoneBookItemsList.apply {
                    filter { item: PhoneBookItem? -> item != null }
                    distinctBy { it.name }
                }
    }

    private fun makePhoneArrayForSameContact(list:List<ContactItemWithNumber>):Array<String>{
        val resultList= mutableListOf<String>()
        for(item in list){
            if(item.normalizedInternationalNumber.isEmpty()) resultList.add(item.internationalNumber)
            else resultList.add(item.normalizedInternationalNumber)
        }
        return resultList.toTypedArray()

    }

    //Recent calls Fragment
    fun getAllRecentCalls()=myDatabaseDao.getAllRecentCalls()

    fun insertRecentCall(call:RecentCall)=myDatabaseDao.insertRecentCall(call)


    //log errors and states
    suspend fun logStateOrErrorToOurServer(phoneNumber: String="",myoptions:Map<String,String>){
        logStateOrErrorToOurServer(phoneNumber=phoneNumber,myDatabaseDao=myDatabaseDao,myAPIService = myAPIService,myoptions = myoptions)

    }

}



