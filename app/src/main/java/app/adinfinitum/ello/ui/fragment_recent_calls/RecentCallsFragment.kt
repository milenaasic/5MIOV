package app.adinfinitum.ello.ui.fragment_recent_calls

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar

import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.data.RepoContacts
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.FragmentRecentCallsBinding
import app.adinfinitum.ello.ui.fragment_dial_pad.DialPadFragment
import app.adinfinitum.ello.ui.myapplication.MyApplication


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

        val repo= RepoContacts(requireActivity().contentResolver,
                                database,
                                apiService,
                                resources.getString(R.string.mobile_app_version_header,(requireActivity().application as MyApplication).mobileAppVersion)
                                )


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
                parent.pastePhoneNumber(phoneNumber )
            }
        )

        binding.recentCallsRecView.setHasFixedSize(true)
        binding.recentCallsRecView.adapter=recentCallsAdapter



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recentCalls.observe(viewLifecycleOwner, Observer {list->

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
