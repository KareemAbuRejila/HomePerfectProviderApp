package com.codeshot.home_perfect_provider.ui.requests

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cc.cloudist.acplibrary.ACProgressBaseDialog

import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.databinding.FragmentRequestsBinding
import com.codeshot.home_perfect_provider.remote.IFCMService
import com.codeshot.home_perfect_provider.ui.main.MainActivity
import com.codeshot.home_perfect_provider.ui.myRequests.MyRequestsViewModel

class RequestsFragment : Fragment() {
    private lateinit var fragmentRequestsBinding: FragmentRequestsBinding

    private lateinit var loadingDialog: ACProgressBaseDialog
    private lateinit var fcmService: IFCMService

    companion object {
        fun newInstance() = RequestsFragment()
    }

    private lateinit var viewModel: RequestsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application).create(
            RequestsViewModel::class.java
        )
        viewModel.getInstance(requireContext())
        fcmService = Common.FCM_SERVICE
        viewModel.getRequest()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentRequestsBinding = FragmentRequestsBinding.inflate(inflater, container, false)
        return fragmentRequestsBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bookingsAdapter = AdditionsAdapter()
        bookingsAdapter.setType(1)
        fragmentRequestsBinding.requestsAdapter = bookingsAdapter

        viewModel.bookings.observe(viewLifecycleOwner, Observer {
            bookingsAdapter.setListRequests(it)
        })

    }

}
