package com.vertial.fivemiov.ui.fragment_main


import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentMainBinding
import kotlinx.android.synthetic.main.activity_main.*


private val MYTAG="MY_MAIN_FRAGMENT"



class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel:MainFragmentViewModel
    private lateinit var contactsAdapter:MainFragmentAdapter
    private lateinit var searchViewActionBar: SearchView

    companion object{
        val MY_PERMISSIONS_REQUEST_READ_CONTACTS=10

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_main,container,false)
        viewModel= ViewModelProvider(this).get(MainFragmentViewModel::class.java)

        if(checkForPermissions()) initalizeAdapter()

        binding.setEmailAndPassButton.setOnClickListener{

            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSetEmailAndPasswordFragment())
        }



        return binding.root
    }

    private fun initalizeAdapter(){

        contactsAdapter=MainFragmentAdapter(ContactItemClickListener {
            val action=MainFragmentDirections.actionMainFragmentToDetailContact(it.lookUpKey,it.name)
            findNavController().navigate(action)
        },getColorForHighlightLetters().toString())
        binding.recViewMainFragment.adapter=contactsAdapter
        val search:String?=null
        viewModel.populateContactList(search)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.contactList.observe(viewLifecycleOwner, Observer {list->
            //list.add()
            contactsAdapter.dataList=list
         })

        viewModel.numberOfSelectedContacts.observe(viewLifecycleOwner, Observer {
            binding.textViewNbContacts.text=String.format(resources.getString(R.string.nb_of_contacts_found,it))

         })

        viewModel.currentSearchString.observe(viewLifecycleOwner, Observer {
            contactsAdapter.stringToColor=it

        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.main_fragment_menu,menu)
        val mitem=menu.findItem(R.id.menu_item_search)
        val itemMyAccount= menu.findItem(R.id.menu_item_myaccount)
        val itemDialPad=menu.findItem(R.id.dialPadFragment)
        val itemSyncContacts=menu.findItem(R.id.menu_item_sync_contacts)
        searchViewActionBar=menu.findItem(R.id.menu_item_search).actionView as SearchView
        searchViewActionBar.setQueryHint(getString(R.string.search_hint))


        mitem.setOnActionExpandListener(object:MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                Log.i(MYTAG,"on search expand listener")
                itemMyAccount.isVisible=false
                itemDialPad.isVisible=false
                itemSyncContacts.isVisible=false
                searchViewActionBar.isIconified=false
                return true
            }
            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                Log.i(MYTAG,"on search collapse listener")
                searchViewActionBar.isIconified=true
                itemMyAccount.isVisible=true
                itemDialPad.isVisible=true
                itemSyncContacts.isVisible=true
                searchViewActionBar.clearFocus()
                return true
            }
        })


        searchViewActionBar.setOnQueryTextListener(object:SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.i(MYTAG,"on qerry text submit $p0")
                if(!p0.isNullOrBlank()) viewModel.populateContactList(p0?:"")
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                Log.i(MYTAG,"on qerry text change $p0")
                viewModel.populateContactList(p0)
                return true
            }

        })

        /*searchViewActionBar.setOnCloseListener{
            itemMyAccount.isVisible=true
            itemDialPad.isVisible=true
            Log.i(MYTAG,"on search collapse listener")
            true
        }*/

        val mSearchCloseButton = resources.getIdentifier("android:id/search_close_btn", null, null)
        searchViewActionBar.findViewById<ImageView>(mSearchCloseButton).setOnClickListener{
            searchViewActionBar.setQuery("", false)

        }

        //super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onStart() {
        super.onStart()
        showSetAccountDialog()
    }

    private fun getColorForHighlightLetters():String{
        var color=0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            color= resources.getColor(R.color.colorAccent)
        }else{
            color= resources.getColor(R.color.colorAccent,null)

        }
        Log.i(MYTAG, "boja je ${color.toUInt().toString(16)}")
        return "#${color.toUInt().toString(16)}"

    }

    private fun checkForPermissions():Boolean{

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                /* if (.shouldShowRequestPermissionRationale(
                         thisActivity,
                         Manifest.permission.READ_CONTACTS
                     )
                 ) {
                     // Show an explanation to the user *asynchronously* -- don't block
                     // this thread waiting for the user's response! After the user
                     // sees the explanation, try again to request the permission.
                 } else {
                     // No explanation needed, we can request the permission.*/
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),MY_PERMISSIONS_REQUEST_READ_CONTACTS)
                return false
            }else return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                   initalizeAdapter()
                } else {
                    showSnackBar(resources.getString(R.string.no_permission_read_contacts))
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showSnackBar(s:String) {
        Snackbar.make(binding.root,s,Snackbar.LENGTH_LONG).show()
    }


    private fun showSetAccountDialog(){

        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }

        builder?.apply {
                setMessage(R.string.set_account_dialog)
                .setTitle("Important")
                .setNeutralButton("I Understand", { dialog, id ->
                        dialog.dismiss()
                })
                    /*.setPositiveButton("OK",{dialog,id->
                        dialog.dismiss()
                    })*/

                .setCancelable(false)

         }
         builder?.create()?.show()

    }


}
