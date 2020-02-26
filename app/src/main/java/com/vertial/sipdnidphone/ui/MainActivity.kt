package com.vertial.sipdnidphone.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.os.bundleOf
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

    companion object{
        const val CURRENT_FRAGMENT="current_fragment"
        const val MAIN_FRAGMENT=0
        const val DIAL_PAD_FRAGMENT=1
        const val DETAIL_FRAGMENT=2

    }

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
                        setMainFragmentUI()

                    }

                    R.id.dialPadFragment->{
                        setDialPadFragmentUI()
                    }

                    R.id.detailContact->{
                        setDetailContactFragmentUI()
                    }

               }

        }

        if(savedInstanceState!=null){
            Log.i(MY_TAG,"usao u onSaveInstance nije null")

            when(savedInstanceState.get(CURRENT_FRAGMENT)){
                MAIN_FRAGMENT->{}
                DIAL_PAD_FRAGMENT->{setDialPadFragmentUI()}
                DETAIL_FRAGMENT->{setDetailContactFragmentUI()}
            }

        }

        //PROBA WEB ACTIVITY
        //val intent= Intent(this,WebViewActivity::class.java)
        //startActivity(intent)

    }

    private fun setMainFragmentUI() {
        binding.toolbarMain.apply {
            elevation=(3 * resources.displayMetrics.density)
            title=resources.getString(R.string.app_name)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setBackgroundColor(resources.getColor(android.R.color.background_light,null))
            }else{
                setBackgroundColor(resources.getColor(android.R.color.background_light))
            }

         }
    }

    private fun setDialPadFragmentUI() {
        binding.toolbarMain.apply {
            navigationIcon = resources.getDrawable(R.drawable.ic_back_black, null)
            elevation = 0f
            title=resources.getString(R.string.empty_string)
        }
    }

    private fun setDetailContactFragmentUI(){
        binding.toolbarMain.apply {
            elevation=0f
            navigationIcon=resources.getDrawable(R.drawable.ic_back_white,null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setBackgroundColor(resources.getColor(R.color.colorPrimaryDark,null))
            }else{
                setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
            }
            title=resources.getString(R.string.empty_string)

         }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var currentFragment= MAIN_FRAGMENT
        when (navController.currentDestination?.id) {
            R.id.mainFragment->{}
            R.id.dialPadFragment->currentFragment= DIAL_PAD_FRAGMENT
            R.id.detailContact->currentFragment= DETAIL_FRAGMENT
        }

        outState.putInt(CURRENT_FRAGMENT,currentFragment)
    }


}
