package com.vertial.sipdnidphone.ui.fragment_main


import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar


import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.databinding.FragmentMainBinding


private val MYTAG="MY_MAIN_FRAGMENT"



class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel:MainFragmentViewModel
    private lateinit var contactsAdapter:MainFragmentAdapter

    companion object{
        val MY_PERMISSIONS_REQUEST_READ_CONTACTS=10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_main,container,false)
        viewModel= ViewModelProvider(this).get(MainFragmentViewModel::class.java)


        contactsAdapter=MainFragmentAdapter(ContactItemClickListener {
            //otvori detail fragment za dati kontakt
         })

        binding.recViewMainFragment.adapter=contactsAdapter




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
            }

    Glide.with(binding.imageViewtest).load(R.drawable.ic_action_dialpad).into(binding.imageViewtest)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.contactList.observe(viewLifecycleOwner, Observer {
            Log.i(MYTAG,"broj kontakata u listi je ${it.size}")
            contactsAdapter.dataList=it
         })

        super.onViewCreated(view, savedInstanceState)
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
                    viewModel.populateContactList("")
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
}
