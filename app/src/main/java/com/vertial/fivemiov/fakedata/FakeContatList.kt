package com.vertial.fivemiov.fakedata

import com.vertial.fivemiov.model.ContactItem

fun createFakeContactList():List<ContactItem>{

    var list= mutableListOf<ContactItem>()

    for(i in 1..3000) list.add(ContactItem(lookUpKey = "qwer",name = "milena"))

    return list
}