package com.codeshot.home_perfect_provider.ui.myRequests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cc.cloudist.acplibrary.ACProgressBaseDialog
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.LOADING_DIALOG
import com.codeshot.home_perfect_provider.databinding.RequestsDialogBinding
import com.codeshot.home_perfect_provider.models.*
import com.codeshot.home_perfect_provider.remote.IFCMService
import com.codeshot.home_perfect_provider.util.TimeUtil
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MyRequestsDialog(val requests: ArrayList<String>) : SuperBottomSheetFragment() {
    private lateinit var dialogRequestsDialogBinding: RequestsDialogBinding
    private lateinit var myRequestsViewModel: MyRequestsViewModel
    private lateinit var loadingDialog: ACProgressBaseDialog
    private lateinit var fcmService: IFCMService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myRequestsViewModel =
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
                .create(MyRequestsViewModel::class.java)
        myRequestsViewModel.getInstance(requireContext())
        fcmService = Common.FCM_SERVICE
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

    private fun accept(request: Request?) {
        loadingDialog.show()
        Common.TOKENS_REF.orderBy(FieldPath.documentId())
            .whereEqualTo(FieldPath.documentId(), request!!.from)
            .get().addOnSuccessListener {
                it.forEach { tokenDoc ->
                    if (tokenDoc.id == request.from) {
                        val userToken: Token = tokenDoc.toObject(Token::class.java)
                        val msgContent: MutableMap<String, String> = HashMap()
                        msgContent["title"] = "Booking"
                        msgContent["requestId"] = request.id!!
                        msgContent["requestStatus"] = "accepted"
                        val dataMsg = DataMessage(userToken.token, msgContent)
                        fcmService.sendMessage(dataMsg)
                            .enqueue(object : Callback<FCMResponse> {
                                override fun onResponse(
                                    call: Call<FCMResponse>,
                                    response: Response<FCMResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        val notification = Notification(
                                            customerId = request.from,
                                            providerId = request.to,
                                            requestId = request.id!!,
                                            notiType = "accepted"
                                        )
                                        notification.time = TimeUtil.getDefaultTimeText(
                                            System.currentTimeMillis(),
                                            TimeZone.getDefault()
                                        )
                                        Common.USERS_REF.document(notification.customerId!!)
                                            .update(
                                                "notifications",
                                                FieldValue.arrayUnion(notification)
                                            )
                                            .addOnSuccessListener {
                                                Common.REQUESTS_REF.document(notification.requestId!!)
                                                    .update("status", "accepted")
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Canceled",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        loadingDialog.dismiss()
                                                    }
                                                    .addOnFailureListener { loadingDialog.dismiss() }
                                            }
                                            .addOnFailureListener { loadingDialog.dismiss() }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Accepted Failed sent!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadingDialog.dismiss()
                                    }
                                }

                                override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                                    Toast.makeText(
                                        requireContext(),
                                        "ERROR Accepted SENT! " + t.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadingDialog.dismiss()
                                }
                            })
                    }
                }

            }

    }


}