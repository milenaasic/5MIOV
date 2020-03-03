package com.vertial.fivemiov.ui

import android.annotation.TargetApi
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityMainBinding


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
        const val SET_ACCOUNT_EMAIL_PASS_FRAGMENT0=3

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

                    R.id.setEmailAndPasswordFragment->{
                        setEmailAndAccountFragmentUI()
                    }

               }

        }

        if(savedInstanceState!=null){
            Log.i(MY_TAG,"usao u onSaveInstance nije null")

            when(savedInstanceState.get(CURRENT_FRAGMENT)){
                MAIN_FRAGMENT->{}
                DIAL_PAD_FRAGMENT->{setDialPadFragmentUI()}
                DETAIL_FRAGMENT->{setDetailContactFragmentUI()}
                SET_ACCOUNT_EMAIL_PASS_FRAGMENT0->{setEmailAndAccountFragmentUI()}
            }

        }

        //TODO dodaj automatski update

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
                window.statusBarColor= resources.getColor(R.color.colorPrimaryDark,null)
            }else{
                setBackgroundColor(resources.getColor(android.R.color.background_light))
                window.statusBarColor= resources.getColor(R.color.colorPrimaryDark)
            }

         }
    }

    private fun setDialPadFragmentUI() {
        binding.toolbarMain.apply {
            navigationIcon = resources.getDrawable(R.drawable.ic_back_black, null)
            elevation = 2f
            title=resources.getString(R.string.empty_string)
        }

        @TargetApi (23)
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor= Color.TRANSPARENT
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

    private fun setEmailAndAccountFragmentUI() {
        binding.toolbarMain.apply {
            navigationIcon = resources.getDrawable(R.drawable.ic_back_black, null)
            elevation = 0f
            title=resources.getString(R.string.empty_string)
        }

        @TargetApi (23)
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor= Color.TRANSPARENT
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
