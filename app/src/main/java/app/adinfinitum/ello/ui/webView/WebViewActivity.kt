package app.adinfinitum.ello.ui.webView

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.*
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.data.produceJWtToken
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.ActivityWebViewBinding
import app.adinfinitum.ello.model.PhoneBookItem
import app.adinfinitum.ello.ui.initializeSharedPrefToFalse
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.DEFAULT_SHARED_PREFERENCES
import app.adinfinitum.ello.utils.PHONEBOOK_IS_EXPORTED
import kotlinx.android.synthetic.main.fragment_detail_contact.*
import org.acra.ACRA


private const val MY_TAG="MY_WEBVIEW_ACTIVITY"
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var viewModel: WebViewViewModel

    private val WEB_VIEW_COOKIE="webView=webView"

    companion object{
         const val HEADER_AUTH_TOKEN_KEY="X-Wvtk"
         const val DASHBOARD_URL= BASE_URL+"dashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        val myDatabaseDao = MyDatabase.getInstance(this).myDatabaseDao
        val myApi = MyAPI.retrofitService
        val mobileAppVersion = (application as MyApplication).mobileAppVersion
        val myRepository = RepoContacts(
            contentResolver,
            myDatabaseDao,
            myApi,
            resources.getString(R.string.mobile_app_version_header, mobileAppVersion)
        )



        viewModel = ViewModelProvider(this, WebViewViewModelFactory(myRepository, application))
            .get(WebViewViewModel::class.java)



        binding.myWebView.apply {
            webViewClient = MyWebWievClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

        }

        val cookieManager = CookieManager.getInstance()
        Log.i(MY_TAG, "pre setovanja get cookies ${cookieManager.getCookie(DASHBOARD_URL)}")

        cookieManager.setCookie(DASHBOARD_URL,WEB_VIEW_COOKIE )
        Log.i(MY_TAG, "posle setovanja get cookies ${cookieManager.getCookie(DASHBOARD_URL)}")

        viewModel.user.observe(this, Observer {

            ACRA.getErrorReporter()
                .putCustomData("WEBVIEW_ACTIVITY_observe_user_data_phone", it.userPhone)
            ACRA.getErrorReporter()
                .putCustomData("WEBVIEW_ACTIVITY_observe_user_data_token", it.userToken)

            if (it != null) binding.myWebView.loadUrl(
                DASHBOARD_URL,
                getCustomHeaders(token = it.userToken, phone = it.userPhone)
            )

        })

        viewModel.startGetingPhoneBook.observe(this, Observer {
            if (it != null) {
                if (it == true) {
                    if(checkForPermissions()){
                        viewModel.getPhoneBook()
                        viewModel.resetStartGetingPhoneBook()
                    }

                }

            }

        })

        viewModel.phoneBook.observe(this, Observer {
            if (it != null) {
                    val mylist = it.filter { item: PhoneBookItem? -> item != null }
                    viewModel.exportPhoneBook(mylist)

            }
        })


        viewModel.phoneBookExported.observe(this, Observer {
            if (it) {

                viewModel.phoneBookExportFinished()

                val sharedPreferences = application.getSharedPreferences(
                    DEFAULT_SHARED_PREFERENCES,
                    Context.MODE_PRIVATE
                )
                if (sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)) {

                    sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED, true)
                        .apply()

                }
            }
        })

        viewModel.loggingOut.observe(this, Observer {
            if (it != null) {
                if (it) {
                    initializeSharedPrefToFalse(application)
                    viewModel.resetLoggingOutToFalse()
                }

            }
        })


    }

    private fun checkForPermissions():Boolean{

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else return checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun getCustomHeaders(token:String, phone:String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val mobileAppVer=resources.getString(R.string.mobile_app_version_header,(application as MyApplication).mobileAppVersion)
        map.put(HEADER_AUTH_TOKEN_KEY, token)
        map.put(HEADER_PHONE_KEY,phone)
        map.put(HEADER_MOBILE_APP_VERSION,mobileAppVer)
        map.put(HEADER_SIGNATURE, produceJWtToken(
                                                Pair(HEADER_AUTH_TOKEN_KEY,token),
                                                Pair(HEADER_PHONE_KEY,phone)
                                )
        )
        return map
    }




    inner class MyWebWievClient() : WebViewClient() {


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)


        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            //progress bar gone
            binding.webViewLinLayout.visibility= View.GONE

        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.i(MY_TAG," WebView onReceivedError $error, $detail_contact_rec_view, $request")
            super.onReceivedError(view, request, error)
        }


    }
}

