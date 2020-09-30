package com.adinfinitum.hello.utils

import com.adinfinitum.hello.model.ContactItem

const val VERIFICATION_METHOD_SMS="sms"
const val VERIFICATION_METHOD_CALL="call"
const val VERIFICATION_METHOD_EXPENSIVE_CALL="expensiveCall"

const val DEFAULT_SHARED_PREFERENCES="default_shared_pref"
const val PHONEBOOK_IS_EXPORTED = "phone_book_is_exported"
const val DISCLAIMER_WAS_SHOWN="disclaimer_was_shown"

const val EMPTY_TOKEN="empty_token"
const val EMPTY_PHONE_NUMBER="empty_phone_number"
const val EMPTY_EMAIL="empty_email"
const val EMPTY_PASSWORD="empty_password"

const val EMPTY_MAIN_SIP_CALLER_ID="empty_main_sip_caller_id"
const val EMPTY_SIP_USERNAME="empty_sip_username"
const val EMPTY_SIP_PASSWORD="empty_sip_password"
const val EMPTY_SIP_SERVER="empty_sip_server"

const val EMPTY_E1_PRENUMBER="empty_e1_prenumber"
const val HOURS_24_IN_MILLIS=24*60*60*1000L

const val EMPTY_NAME="empty_name"
 val EMPTY_CONTACT_ITEM=ContactItem(lookUpKey ="",name= EMPTY_NAME)

const val DOUBLE_ZERO="00"
const val ONE_ZERO="0"
const val NIGERIAN_PREFIX="234"
const val PLUS_NIGERIAN_PREFIX="+"+ NIGERIAN_PREFIX
const val PHONE_NUMBER_MIN_LENGHT=10

const val TERMS_OF_USE=
        "<p><strong>1. Applicability</strong></p>" +
        "<p style='text-align:justify'>" +
        "These general terms and conditions apply to every offer made by 5MIOV trading under the name: 5mIOV and to every contract that has been realized " +
        "between 5mIOV and a User using the mobile application 5mIOV, purchasing 5mIOV call credits or vouchers or with regard to the mobile " +
        "telecommunication services provided by 5mIOV." +
        "</p>" +
        "<p>" +
        "These services only include charged calls to International phone numbers. This implies that this application does not allow/support inbound calling." +
        "</p>" +
        "<p>" +
        "5mIOV may make changes to these Terms from time to time. Changes to these Terms will be effective when published. We recommend reviewing the Terms on a regular basis." +
        "</p>" +
        "<p>" +
        "By accepting our Terms and conditions, you also accept our privacy statement." +
        "</p>" +
        "<p>" +
        "<strong>1.1 Privacy Statement</strong></p>" +
        "<p>" +
        "This privacy statement is meant to help you understand what data we collect, why we collect it, and what we do with it." +
        "</p>" +
        "<p>" +
        "5mIOV shall only process the personal data (including data regarding the use of the Services) obtained from the Users for the provision of theServices. " +
        "5mIOV shall refrain from using the (personal) data for any other purpose than the provision of the Services." +
        "</p>" +
        "<p>" +
        "We collect the following data;" +
        "</p>" +
        "<ul>" +
        "<li>" +
        "\t<strong>Phone number</strong>" +
        "</li>" +
        "</ul>" +
        "<p>" +
        "We will use your phone number for the purpose of verifying your identity and setting up outgoing calls for you." +
        "</p>" +
        "<ul>" +
        "<li>" +
        "\t<strong>Address Book</strong>" +
        "</li>" +
        "</ul>" +
        "<p>" +
        "When you first install the App on your device, you will be asked to allow us access to your address book. If you consent, the 5mIOV app will have " +
        "access to contact information in your address book on the devices you use the app on in order to;" +
        "</p>" +
        "<p>" +
        "\t\t1. Correctly format the phone numbers for international calls" +
        "</p>" +
        "<p>" +
        "\t\t2. Display names and numbers of each contact as it appears in your address" +
        "book" +
        "</p>" +
        "<p>" +
        "\t\t3. Sync your contacts in the address book on your device with the app" +
        "</p>" +
        "<p>" +
        "<strong>Note</strong>" +
        ": Only the App has access to the internal Address book. We will not store it or access it on our servers." +
        "</p>" +
        "<p>" +
        "<strong>2. Call credit</strong>" +
        "</p>" +
        "<p>" +
        "In order to make use of the Services, the User is required to purchase call credit from 5mIOV and is not entitled to any interest on the purchased call credit." +
        "</p>" +
        "<p>" +
        "5mIOV makes available to the User a payment method for the purchase of call credit. 5mIOV may from time to time change the various payment methods." +
        "</p>" +
        "<p>" +
        "Additional terms and conditions may apply by using the available third party payment methods." +
        "</p>"+
        "<p><strong>3. Contract Fulfilment and Guarantees</strong></p>" +
        "<p>" +
        "5mIOV guarantees the best availability of the service and, in the event of breakdowns, that these will be rectified as quickly as possible, in order to fulfil the contract and the specifications stated in the offer." +
        "</p>" +
        "<p>" +
        "5mIOV cannot guarantee that the Software and the Services will always function without disruptions, delay or errors. Various factors outside the " +
        "control of 5mIOV may impact the quality of the connection and the use of the Software and Services and in such cases, 5mIOV is not responsible nor liable for any disruption, interruption or delay of the Services." +
        "</p>" +
        "<p>" +
        "<strong>4. Liability and indemnification</strong>" +
        "</p>" +
        "<p>" +
        "The User agrees to indemnify and hold harmless 5mIOV and its affiliates, and each of their respective, directors, shareholders and employees from " +
        "and against any and all claims, losses and/or damages arising from or relating to;" +
        "</p>" +
        "<p>" +
        "\t(i) the User's breach of any of these Terms" +
        "</p>" +
        "<p>" +
        "\t(ii) the User's breach of any applicable law of regulation or," +
        "</p>" +
        "<p>" +
        "\t(iii) the User's misuse of the Software and/or Services." +
        "</p>" +
        "<p>" +
        "<strong>5. Deactivation</strong>" +
        "</p>" +
        "<p>" +
        "If the Users do not fulfil its obligations towards 5mIOV, is in breach of these Terms and/or in the event of fraud or suspected fraud, 5mIOV is entitled to deactivate the User's account." +
        "</p>" +
        "<p>" +
        "After deactivation of the User's account, the User is not allowed to use the Software or the Services and 5mIOV shall have no obligation to provide " +
        "the Services towards the User. In the event that the User's account is deactivated, 5mIOV shall inform the User as soon as reasonably possible. " +
        "The User is not entitled to any compensation after the deactivation of its account." +
        "</p>" +
        "<p>" +
        "<strong>6. Intellectual property rights</strong>" +
        "</p>" +
        "<p>" +
        "The Software contains proprietary and confidential information that is protected by intellectual property rights owned by or licensed to 5mIOV." +
        "</p>" +
        "<p>" +
        "5mIOV and/or its licensors retain exclusive ownership of the Software and all intellectual property." +
        "</p>" +
        "<p>" +
        "The User is not allowed to use the software for any other purpose than making use of the Services. The User shall not make any changes to the " +
        "Software, decompile the Software and/or manipulate the Software. In the event of any unauthorized use of the Software by the User, the User shall be liable for all damages suffered by 5mIOV." +
        "</p>" +
        "<p>" +
        "<strong>7. Final Provisions</strong>" +
        "</p>" +
        "<p>" +
        "The rights and obligations of the User under the contract with 5mIOV cannot be assigned or transferred except with the prior written approval of 5mIOV" +
        "</p>" +
        "<p>" +
        "All contracts entered into between 5mIOV and the User are governed by Nigerian law." +
        "</p>" +
        "<p>" +
        "<strong>" +
        "NOTE: If you do not agree to the Terms of Service, you may not activate or use the Services nor Software. Once accepted the Terms of Service constitute a binding agreement between you and 5mIOV." +
        "</strong>" +
        "</p>" +
        "<p>" +
        "Last update: 15th of May 2020</p>"



