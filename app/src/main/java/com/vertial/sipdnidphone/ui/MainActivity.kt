package com.vertial.sipdnidphone.ui

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.api.MyAPI
import com.vertial.sipdnidphone.api.MyAPIService
import com.vertial.sipdnidphone.data.Repo
import com.vertial.sipdnidphone.database.MyDatabase
import com.vertial.sipdnidphone.databinding.ActivityMainBinding
import com.vertial.sipdnidphone.ui.RegistrationAuthorization.RegistrationAuthorizationActivity
import com.vertial.sipdnidphone.ui.emty_logo_fragment.EmptyLogoFragment
import com.vertial.sipdnidphone.ui.emty_logo_fragment.EmptyLogoFragmentDirections
import com.vertial.sipdnidphone.ui.webview_activity.WebViewActivity
import com.vertial.sipdnidphone.utils.EMPTY_TOKEN


private const val MY_TAG="MY_MainActivity"
class MainActivity : AppCompatActivity() {

     lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbarMain)

        val myDatabaseDao=MyDatabase.getInstance(this).myDatabaseDao
        val myApi=MyAPI.retrofitService

        val myRepository=Repo(myDatabaseDao,myApi)
       // val viewModel = ViewModelProvider(this, YourViewModelFactory).get(YourViewModel::class.java)


        viewModel = ViewModelProvider(this, MainActivityViewModelFactory(myRepository,application))
            .get(MainActivityViewModel::class.java)


        navController=findNavController(R.id.navfragment_main)
        NavigationUI.setupWithNavController(binding.toolbarMain,navController)


       navController.addOnDestinationChangedListener { controller, destination, arguments ->

               when(destination.id){

                    R.id.mainFragment->{
                        binding.toolbarMain.elevation=(3 * resources.displayMetrics.density)
                    }

                    R.id.dialPadFragment->{
                        binding.toolbarMain.navigationIcon=resources.getDrawable(R.drawable.ic_close_button,null)
                        binding.toolbarMain.elevation=0f

                    }

               }

        }



        //PROBA WEB ACTIVITY
        //val intent= Intent(this,WebViewActivity::class.java)
        //startActivity(intent)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }




}
