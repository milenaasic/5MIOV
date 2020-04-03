package com.vertial.fivemiov.ui.webView

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityWebViewBinding
import com.vertial.fivemiov.ui.main_activity.MainActivity


private const val MY_TAG="MY_WebVIewActivity"
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding:ActivityWebViewBinding
    private lateinit var viewModel: WebViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=DataBindingUtil.setContentView(this, R.layout.activity_web_view )

        val myDatabaseDao= MyDatabase.getInstance(this).myDatabaseDao
        val myApi= MyAPI.retrofitService

        val myRepository= RepoContacts(contentResolver,myDatabaseDao,myApi)

        viewModel = ViewModelProvider(this, WebViewViewModelFactory(myRepository,application))
            .get(WebViewViewModel::class.java)


        binding.myWebView.apply {
            webViewClient=MyWebWievClient()
            loadUrl("https://test.find.in.rs/dashboard", getCustomHeaders())
            settings.javaScriptEnabled = true

        }

        viewModel.user.observe(this, Observer {
            Log.i(MY_TAG," user je $it")

         })

        viewModel.phoneBook.observe(this, Observer {
            if (it != null) {
                viewModel.exportPhoneBook(it)
            }
         })


        viewModel.phoneBookExported.observe(this, Observer {
            if(it){
                val sharedPreferences= getSharedPreferences(MainActivity.MAIN_ACTIVITY_SHARED_PREF_NAME, Context.MODE_PRIVATE)
                if(sharedPreferences.contains(MainActivity.PHONEBOOK_IS_EXPORTED)){
                    val isExported=sharedPreferences.getBoolean(MainActivity.PHONEBOOK_IS_EXPORTED,false)
                    Log.i(MY_TAG," usao u ima phoneBookIsExported promenljiva i vrednost je $isExported")
                    sharedPreferences.edit().putBoolean(MainActivity.PHONEBOOK_IS_EXPORTED,true).commit()
                    Log.i(MY_TAG,"  phoneBookIsExported promenljiva posle promene $isExported")

                }
            }

        })
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i(MY_TAG,"  on key down $keyCode, event je $event")

        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (binding.myWebView.canGoBack()) {
                        binding.myWebView.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

class MyWebWievClient : WebViewClient() {


    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        Log.i(MY_TAG,"shouldInterceptRequest, url je ${request?.url}")
        Log.i(MY_TAG,"${request?.method}, ${request?.requestHeaders?.values},${request?.requestHeaders?.entries},${request?.requestHeaders?.keys}")
        //view?.loadUrl(request?.getUrl().toString(), getCustomHeaders());
        //val response=WebResourceResponse(responseHeaders= getCustomHeaders())
        val response=super.shouldInterceptRequest(view, request)
        Log.i(MY_TAG,"response je ${response.toString()}")

        return super.shouldInterceptRequest(view, request)

    }

    override fun onReceivedLoginRequest(
        view: WebView?,
        realm: String?,
        account: String?,
        args: String?
    ) {
        super.onReceivedLoginRequest(view, realm, account, args)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        Log.i(MY_TAG,"shouldOverrideUrlLoading")
        return super.shouldOverrideUrlLoading(view, request)


    }

    /*private fun getNewResponse(url: String): WebResourceResponse? {
        return try {
            val httpClient = OkHttpClient()
            val request: Request = Builder()
                .url(url.trim { it <= ' ' })
                .addHeader("Authorization", "YOU_AUTH_KEY") // Example header
                .addHeader("api-key", "YOUR_API_KEY") // Example header
                .build()
            val response: Response = httpClient.newCall(request).execute()
            WebResourceResponse(
                null,
                response.header("content-encoding", "utf-8"),
                response.body().byteStream()
            )
        } catch (e: Exception) {
            null
        }
    }*/

}

fun getCustomHeaders():Map<String, String> {
    val map= mutableMapOf<String,String>()
    map.put("Authorization","Basic NW1pb3Y6dGVzdGVy")
    map.put("vwtk","8c036d91688711ea94bec1e66168a946")
    //map.put("username","milena.asic@gmail.com")
    //map.put("password","Milena1+")
    return map
}
