package com.vertial.fivemiov.ui.webView

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityWebViewBinding
import com.vertial.fivemiov.ui.MainActivity

private const val MY_TAG="MY_WebVIewActivity"
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding:ActivityWebViewBinding
    private lateinit var viewModel: WebViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_web_view )

        val myDatabaseDao= MyDatabase.getInstance(this).myDatabaseDao
        val myApi= MyAPI.retrofitService

        val myRepository= RepoContacts(contentResolver,myDatabaseDao,myApi)

        viewModel = ViewModelProvider(this, WebViewViewModelFactory(myRepository,application))
            .get(WebViewViewModel::class.java)


        binding.myWebView.apply {
            loadUrl("http://www.example.com")
            settings.javaScriptEnabled = true

        }

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
}
