package com.vertial.fivemiov.ui.fragment_detail_contact


import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentDetailContactBinding
import com.vertial.fivemiov.ui.fragment_dial_pad.DialPadFragment
import com.vertial.fivemiov.ui.fragment_main.MainFragment
import com.vertial.fivemiov.utils.isValidPhoneNumber

private val MYTAG="MY_DetailContact"

class DetailContact : Fragment() {

    private lateinit var binding: FragmentDetailContactBinding
    private lateinit var viewModel: DetailContactViewModel
    private lateinit var args:DetailContactArgs
    private lateinit var phoneAdapter:DetailContactAdapter

    private lateinit var myPrefixNumber:String


    companion object{
        val MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL=15

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = DetailContactArgs.fromBundle(arguments!!)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_detail_contact,container,false)

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService= MyAPI.retrofitService
        val repo= RepoContacts(requireActivity().contentResolver,database,apiService)

        viewModel=ViewModelProvider(this,DetailContactViewModelFactory(args.contactLookUpKey,repo,requireActivity().application)).get(DetailContactViewModel::class.java)

        phoneAdapter= DetailContactAdapter(
                PhoneNumberClickListener (requireActivity(),resources.displayMetrics.density),
                SipItemClickListener{
                    findNavController().navigate(DetailContactDirections.actionDetailContactToSipFragment(args.displayName))

                },
                PrenumberItemClickListener(requireActivity()) {activity, phone ->
                        if(checkForPermissions()) makePrenumberPhoneCall(activity,phone)
                    }

                )



        binding.detailContactRecView.adapter=phoneAdapter
        //binding.detailContactRecView.addItemDecoration(DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL))

        binding.displayNameTextView.text=args.displayName

        return binding.root

    }

   fun makePrenumberPhoneCall(activity: Activity, myphone:String){


       val phone = PhoneNumberUtils.normalizeNumber(myphone)

       if (phone.isValidPhoneNumber()) {
           val intentToCall = Intent(Intent.ACTION_CALL).apply {
               setData(Uri.parse(resources.getString(R.string.prenumber_call,myPrefixNumber,phone)))

           }

           if (intentToCall.resolveActivity(requireActivity().packageManager) != null) {
               startActivity(intentToCall)
           } else showSnackBar(resources.getString(R.string.unable_to_make_call))

       } else showSnackBar(resources.getString(R.string.not_valid_phone_number))


   }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.phoneList.observe(viewLifecycleOwner, Observer {

                phoneAdapter.dataList=it.toSet().toList()

         })

         viewModel.prefixNumber.observe(viewLifecycleOwner, Observer {
            myPrefixNumber=it
          })
    }

    private fun noDuplicatesList(list:List<String>) {

        list.toSet()

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.menu_item_logout).isVisible=false
    }

    private fun showSnackBar(s:String) {
        Snackbar.make(binding.root,s, Snackbar.LENGTH_LONG).show()
    }


    private fun checkForPermissions():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),
                    DialPadFragment.MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL
                )
                return false
            } else return true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            MainFragment.MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //makePhoneCall()
                } else {
                    showSnackBar(resources.getString(R.string.no_permission_make_phone_call))
                }
                return
            }

            else -> { }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}
