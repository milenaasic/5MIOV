package com.vertial.sipdnidphone.ui.fragment_detail_contact


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.api.MyAPI
import com.vertial.sipdnidphone.data.Repo
import com.vertial.sipdnidphone.database.MyDatabase
import com.vertial.sipdnidphone.databinding.FragmentDetailContactBinding
import com.vertial.sipdnidphone.ui.fragment_main.ContactItemClickListener
import com.vertial.sipdnidphone.ui.fragment_main.MainFragmentAdapter
import com.vertial.sipdnidphone.ui.fragment_main.MainFragmentDirections
private val MYTAG="MY_DetailContact"

class DetailContact : Fragment() {

    private lateinit var binding: FragmentDetailContactBinding
    private lateinit var viewModel: DetailContactViewModel
    private lateinit var args:DetailContactArgs
    private lateinit var phoneAdapter:DetailContactAdapter

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
        val repo= Repo(database,apiService)

        viewModel=ViewModelProvider(this,DetailContactViewModelFactory(args.contactLookUpKey,repo,requireActivity().application)).get(DetailContactViewModel::class.java)

        phoneAdapter= DetailContactAdapter(
                SipItemClickListener{
                    Log.i(MYTAG,"sip item click listener")
                },
                PrenumberItemClickListener {
                    Log.i(MYTAG,"prenumber item click listener")
                }
        )

        binding.detailContactRecView.adapter=phoneAdapter

        binding.displayNameTextView.text=args.displayName

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.phoneList.observe(viewLifecycleOwner, Observer {
            Log.i(MYTAG,"broj kontakata u listi  ${it}")
                phoneAdapter.dataList=it
                //setLargePhoto(it[0])
         })
    }

    private fun setLargePhoto(phoneItem: PhoneItem) {
        Log.i(MYTAG,"photo uri je ${phoneItem.photoFileId}")
        if(phoneItem.photoUri!=null){

            Glide.with(this)
                .load(phoneItem.photoUri)
                .apply(RequestOptions().fallback(R.drawable.thumbnail_background))
                .into(binding.imageView)

        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.menu_item_sync_contacts).isVisible=false
    }


}
