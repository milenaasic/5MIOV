package app.adinfinitum.ello.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import app.adinfinitum.ello.model.ContactItem
import app.adinfinitum.ello.model.ContactItemWithNumber
import app.adinfinitum.ello.model.PhoneBookItem
import app.adinfinitum.ello.model.PhoneItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val MY_TAG="RepoProvideContacts"

interface IRepoProvideContacts {

    suspend fun getAllContacts(uri: Uri): List<ContactItem>
    suspend fun getPhoneNumbersForContact(lookUpKey: String): List<PhoneItem>
    suspend fun getRawContactsPhonebook(): List<PhoneBookItem>
}

class RepoProvideContacts (
        val contentResolver: ContentResolver,
        val dispatcher: CoroutineDispatcher=Dispatchers.IO) : IRepoProvideContacts {

        override suspend fun getAllContacts(uri: Uri):List<ContactItem>{
            return withContext(dispatcher){
                myGetAllContacts(uri = uri)
            }

        }

        //Retreive all contacts from phonebook, no filters
        private fun myGetAllContacts(uri: Uri):List<ContactItem>{

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


        override suspend fun getPhoneNumbersForContact(lookUpKey:String):List<PhoneItem>{
            return withContext(dispatcher){
                myGetPhoneNumbersForContact(lookUpKey)
            }
        }
        //Detail fragment
        private fun myGetPhoneNumbersForContact(lookUpKey:String):List<PhoneItem>{

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

                val cursor=
                    contentResolver.query(
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

        // Called from WebViewViewModel and MainFragment when exporting phonebook to server
        override suspend fun getRawContactsPhonebook():List<PhoneBookItem>{
            return withContext(dispatcher){
                myGetRawContactsPhonebook()
            }
        }

        //todo to FIX - names and phoneNumbers repeat!
        private fun myGetRawContactsPhonebook():List<PhoneBookItem>{

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


    }