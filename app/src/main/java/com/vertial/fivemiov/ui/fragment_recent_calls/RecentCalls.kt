package com.vertial.fivemiov.ui.fragment_recent_calls

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.vertial.fivemiov.R

/**
 * A simple [Fragment] subclass.
 */
class RecentCalls : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recent_calls, container, false)
    }

}
