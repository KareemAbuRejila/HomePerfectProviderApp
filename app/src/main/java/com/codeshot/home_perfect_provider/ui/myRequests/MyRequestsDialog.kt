package com.codeshot.home_perfect_provider.ui.myRequests

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.databinding.RequestsDialogBinding

class MyRequestsDialog: SuperBottomSheetFragment() {
    private lateinit var dialogRequestsDialogBinding: RequestsDialogBinding
    private lateinit var myRequestsViewModel: MyRequestsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myRequestsViewModel=ViewModelProvider.AndroidViewModelFactory(activity!!.application).create(MyRequestsViewModel::class.java)
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
        myRequestsViewModel.bookings.observe(viewLifecycleOwner, Observer {
            val acProgressBaseDialog = ACProgressFlower.Builder(requireContext())
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Please Wait ....!")
                .fadeColor(Color.DKGRAY).build()
            acProgressBaseDialog.show()
            bookingsAdapter.setListRequests(it)
            acProgressBaseDialog.dismiss()
        })
        dialogRequestsDialogBinding.requestsAdapter=bookingsAdapter
    }
}