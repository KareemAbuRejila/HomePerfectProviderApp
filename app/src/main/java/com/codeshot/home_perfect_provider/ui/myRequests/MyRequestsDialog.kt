package com.codeshot.home_perfect_provider.ui.myRequests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cc.cloudist.acplibrary.ACProgressBaseDialog
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.common.Common.LOADING_DIALOG
import com.codeshot.home_perfect_provider.databinding.RequestsDialogBinding

class MyRequestsDialog(val requests: ArrayList<String>) : SuperBottomSheetFragment() {
    private lateinit var dialogRequestsDialogBinding: RequestsDialogBinding
    private lateinit var myRequestsViewModel: MyRequestsViewModel
    private lateinit var loadingDialog: ACProgressBaseDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myRequestsViewModel=ViewModelProvider.AndroidViewModelFactory(activity!!.application).create(MyRequestsViewModel::class.java)
        myRequestsViewModel.getInstance(requireContext())
        myRequestsViewModel.getRequest(requests)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialogRequestsDialogBinding=RequestsDialogBinding.inflate(inflater,container,false)
        return dialogRequestsDialogBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bookingsAdapter=AdditionsAdapter()
        bookingsAdapter.setType(1)
        dialogRequestsDialogBinding.requestsAdapter = bookingsAdapter

        myRequestsViewModel.bookings.observe(viewLifecycleOwner, Observer {
            bookingsAdapter.setListRequests(it)
        })
    }

}