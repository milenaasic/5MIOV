package com.vertial.fivemiov.ui.fragment_recent_calls

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentRecentCallsBinding
import com.vertial.fivemiov.model.RecentCall
import com.vertial.fivemiov.ui.fragment_dial_pad.DialPadFragment

private val MYTAG="MY_RecentCallsFragment"
class RecentCallsFragment : Fragment() {

    private lateinit var binding: FragmentRecentCallsBinding
    private lateinit var viewModel: RecentCallsViewModel
    private lateinit var recentCallsAdapter:RecentCallsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_recent_calls,container,false)

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService= MyAPI.retrofitService
        val repo= RepoContacts(requireActivity().contentResolver,database,apiService)


        viewModel = ViewModelProvider(this, RecentCallsViewModelFactory(repo,requireActivity().application))
            .get(RecentCallsViewModel::class.java)

        binding.recentCallsRecView.addItemDecoration(
            DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL)
        )

        initalizeAdapter()

        return binding.root
    }


    private fun initalizeAdapter(){

        recentCallsAdapter= RecentCallsAdapter(
            RecentCallClickListener {phoneNumber->
                val parent =parentFragment as DialPadFragment
                parent?.pastePhoneNumber(phoneNumber )
            }
        )

        binding.recentCallsRecView.setHasFixedSize(true)
        binding.recentCallsRecView.adapter=recentCallsAdapter

        /*recentCallsAdapter.dataList=listOf(RecentCall(0, "milena","+1 56 896 523",System.currentTimeMillis()),
            RecentCall(0, "milena2","+1 56 896 523-2",System.currentTimeMillis())
        )*/

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recentCalls.observe(viewLifecycleOwner, Observer {list->
            Log.i(MYTAG," recent call lista iz baze je $list")
            if(list!=null){
                //if(list.isEmpty()) showSnackBar(getString(R.string.no_recent_calls))
                //else recentCallsAdapter.dataList=list.reversed()
                recentCallsAdapter.dataList=list.reversed()
            }
        })
    }

    private fun showSnackBar(s:String) {
        Snackbar.make(binding.recentCallsCoordLayout,s, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }
}
