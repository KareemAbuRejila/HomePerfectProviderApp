package com.codeshot.home_perfect_provider.ui.myRequests

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cc.cloudist.acplibrary.ACProgressBaseDialog
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.adapters.MyRequestsAdapter
import com.codeshot.home_perfect_provider.common.Common.LOADING_DIALOG
import com.codeshot.home_perfect_provider.common.Common.REQUESTS_REF
import com.codeshot.home_perfect_provider.databinding.RequestsDialogBinding
import com.codeshot.home_perfect_provider.models.Request
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MyRequestsDialog: SuperBottomSheetFragment() {
    private lateinit var dialogRequestsDialogBinding: RequestsDialogBinding
    private lateinit var myRequestsViewModel: MyRequestsViewModel
    private lateinit var myRequestsAdapter: MyRequestsAdapter
    private lateinit var loadingDialog: ACProgressBaseDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myRequestsViewModel=ViewModelProvider.AndroidViewModelFactory(activity!!.application).create(MyRequestsViewModel::class.java)
        loadingDialog = LOADING_DIALOG(requireContext())
        myRequestsViewModel.getInstance(requireContext())
        myRequestsViewModel.getRequest()
        loadingDialog.show()

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
        val defaultOption = FirestoreRecyclerOptions.Builder<Request>()
            .setQuery(REQUESTS_REF, Request::class.java)
            .build()
        myRequestsAdapter = MyRequestsAdapter(defaultOption)
        bookingsAdapter.setType(1)
        myRequestsViewModel.bookingsOption.observe(viewLifecycleOwner, Observer {
            myRequestsAdapter.updateOptions(it)
            loadingDialog.dismiss()
        })
        dialogRequestsDialogBinding.requestsAdapter = myRequestsAdapter
    }

    override fun onStart() {
        super.onStart()
        myRequestsAdapter.startListening()
    }
}