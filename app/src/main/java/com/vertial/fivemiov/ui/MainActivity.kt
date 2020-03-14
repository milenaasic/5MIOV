package com.vertial.fivemiov.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityMainBinding
import com.vertial.fivemiov.model.User
import com.vertial.fivemiov.ui.RegistrationAuthorization.RegistrationAuthorizationActivity
import com.vertial.fivemiov.utils.EMPTY_EMAIL
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER


private const val MY_TAG="MY_MainActivity"
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel
    private var userHasEmailAndPass: Boolean=false

    companion object{
        const val CURRENT_FRAGMENT="current_fragment"
        const val MAIN_FRAGMENT=0
        const val DIAL_PAD_FRAGMENT=1
        const val DETAIL_FRAGMENT=2
        const val SET_ACCOUNT_EMAIL_PASS_FRAGMENT0=3

        const val MAIN_ACTIVITY_SHARED_PREF_NAME="MainActivitySharedPref"
        const val PHONEBOOK_IS_EXPORTED="phone_book_is_exported"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbarMain)

        val myDatabaseDao=MyDatabase.getInstance(this).myDatabaseDao
        val myApi=MyAPI.retrofitService

        val myRepository=RepoContacts(contentResolver,myDatabaseDao,myApi)

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

        viewModel.userData.observe(this, Observer {user->

                if(user.userPhone.equals(EMPTY_PHONE_NUMBER)){
                    startActivity(Intent(this,RegistrationAuthorizationActivity::class.java))
                    finish()
                }

                userHasEmailAndPass=!user.userEmail.equals(EMPTY_EMAIL)

         })

        viewModel.phoneBook.observe(this, Observer {
            if (it != null) {
                    viewModel.exportPhoneBook(it)
                }
            Log.i(MY_TAG,"phonebook lista je $it")
         })

         viewModel.phoneBookExported.observe(this, Observer {
            if(it){
                val sharedPreferences= getSharedPreferences(MAIN_ACTIVITY_SHARED_PREF_NAME, Context.MODE_PRIVATE)
                if(sharedPreferences.contains(PHONEBOOK_IS_EXPORTED)){
                    val isExported=sharedPreferences.getBoolean(PHONEBOOK_IS_EXPORTED,false)
                    Log.i(MY_TAG," usao u ima phoneBookIsExported promenljiva i vrednost je $isExported")
                    sharedPreferences.edit().putBoolean(PHONEBOOK_IS_EXPORTED,true).commit()
                    Log.i(MY_TAG,"  phoneBookIsExported promenljiva posle promene $isExported")

                }
            }

          })
    }



    private fun setMainFragmentUI() {
        binding.toolbarMain.apply {
            elevation=(4 * resources.displayMetrics.density)
            title=resources.getString(R.string.app_name)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility= 0
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor= Color.TRANSPARENT
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

    private fun setEmailAndAccountFragmentUI() {
        binding.toolbarMain.apply {
            navigationIcon = resources.getDrawable(R.drawable.ic_back_black, null)
            elevation = 0f
            title=resources.getString(R.string.empty_string)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
             window.statusBarColor= Color.TRANSPARENT
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_item_logout-> {
                                    handleLogOut()
                                    return true
                                    }
            else->return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

        }

    }

    private fun handleLogOut() {
       if(userHasEmailAndPass) logout()
        else showAlertDialog()

    }

    private fun logout() {
        Log.i(MY_TAG,"   log out funkcija")
        viewModel.logout()
    }

    private fun showAlertDialog(){
        val alertDialog=AlertDialog.Builder(this)
            .setMessage("please set account email or your credit will be lost after log out")
            .setPositiveButton("LOG OUT ANYWAY",object:DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    logout()
                }
            })
            .setNeutralButton("CANCEL",object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.dismiss()}
                })

        alertDialog.show()

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


    fun exportPhoneBook(){
        viewModel.getPhoneBook()
    }


}
