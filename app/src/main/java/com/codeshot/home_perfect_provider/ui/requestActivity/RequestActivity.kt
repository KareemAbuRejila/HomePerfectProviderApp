package com.codeshot.home_perfect_provider.ui.requestActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import cc.cloudist.acplibrary.ACProgressBaseDialog
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.REQUESTS_REF
import com.codeshot.home_perfect_provider.common.Common.TOKENS_REF
import com.codeshot.home_perfect_provider.common.Common.USERS_REF
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.adapters.AdditionsAdapter
import com.codeshot.home_perfect_provider.common.Common.LOADING_DIALOG
import com.codeshot.home_perfect_provider.databinding.ActivityRequestBinding
import com.codeshot.home_perfect_provider.models.*
import com.codeshot.home_perfect_provider.remote.IFCMService
import com.codeshot.home_perfect_provider.ui.main.MainActivity
import com.codeshot.home_perfect_provider.util.TimeUtil
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RequestActivity : AppCompatActivity() {
    private lateinit var activityRequest: ActivityRequestBinding
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var loadingDialog: ACProgressBaseDialog

    private var request: Request? = Request()
    private lateinit var fcmService: IFCMService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRequest = DataBindingUtil
            .setContentView(this, R.layout.activity_request)
        requestViewModel = ViewModelProvider.AndroidViewModelFactory(application)
            .create(RequestViewModel::class.java)
        fcmService = Common.FCM_SERVICE
        loadingDialog = LOADING_DIALOG(this)
        getRequestFromServer()

        activityRequest.btnAccept.setOnClickListener {
            accept()
        }
        activityRequest.btnCancel.setOnClickListener {
            cancel()
        }

    }

    private fun getRequestFromServer() {
        loadingDialog.show()
        if (getRequestId() != null) {
            REQUESTS_REF.document(getRequestId()!!)
                .get().addOnSuccessListener { requsetDoc ->
                    if (requsetDoc.exists()) {
                        request = requsetDoc.toObject(Request::class.java)
                        if (request!!.status != "waiting") sendToHome()
                        request!!.id = requsetDoc.id
                        USERS_REF.document(request!!.from!!)
                            .get().addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val user = userDoc.toObject(User::class.java)
                                    activityRequest.user = user
                                    activityRequest.request
                                    Picasso.get().load(user!!.personalImageUri)
                                        .placeholder(R.drawable.ic_person_black_24dp)
                                        .into(activityRequest.userImage)
                                    setUpAdditionsList(request!!.additions)
                                }
                                loadingDialog.dismiss()
                            }.addOnFailureListener { finish() }
                    }
                }
        }
    }

    private fun sendToHome() {
        startActivity(Intent(this@RequestActivity, MainActivity::class.java))
        finish()
    }

    private fun setUpAdditionsList(additions: ArrayList<Addition>) {
        if (additions.size != 0 && additions != null) {
            val adapter = AdditionsAdapter()
            adapter.setType(0)
            adapter.setList(request!!.additions)
            activityRequest.addictionsAdapter = adapter

        }
    }

    private fun getRequestId(): String? {
        val request: String?
        if (intent != null) {
            if (intent.extras!!.getString("requestId") != null) {
                request = intent.extras!!.getString("requestId").toString()
                return request
            }
        }
        return null
    }

    private fun accept() {
        loadingDialog.show()
        TOKENS_REF.orderBy(FieldPath.documentId())
            .whereEqualTo(FieldPath.documentId(), request!!.from)
            .get().addOnSuccessListener {
                it.forEach { tokenDoc ->
                    if (tokenDoc.id == request!!.from) {
                        val userToken: Token = tokenDoc.toObject(Token::class.java)
                        val msgContent: MutableMap<String, String> = HashMap()
                        msgContent["title"] = "Booking"
                        msgContent["requestId"] = request!!.id!!
                        msgContent["requestStatus"] = "accepted"
                        val dataMsg = DataMessage(userToken.token, msgContent)
                        fcmService.sendMessage(dataMsg)
                            .enqueue(object : Callback<FCMResponse> {
                                override fun onResponse(
                                    call: Call<FCMResponse>,
                                    response: Response<FCMResponse>
                                ) {
                                    if (response.isSuccessful) {
//                                        REQUESTS_REF.document(request!!.id!!)
//                                            .update("status", "accepted")
//                                            .addOnSuccessListener {
//                                                Toast.makeText(
//                                                    this@RequestActivity,
//                                                    "Accepted",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                                loadingDialog.dismiss()
//                                                onDestroy()
//                                            }.addOnFailureListener { loadingDialog.dismiss() }
                                        val notification = Notification(
                                            customerId = request!!.from,
                                            providerId = request!!.to,
                                            requestId = request!!.id!!,
                                            notiType = "accepted"
                                        )
                                        notification.time = TimeUtil.getDefaultTimeText(
                                            System.currentTimeMillis(),
                                            TimeZone.getDefault()
                                        )
                                        USERS_REF.document(notification.customerId!!)
                                            .update(
                                                "notifications",
                                                FieldValue.arrayUnion(notification)
                                            )
                                            .addOnSuccessListener {
                                                REQUESTS_REF.document(notification.requestId!!)
                                                    .update("status", "accepted")
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            this@RequestActivity,
                                                            "Accepted",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        loadingDialog.dismiss()
                                                        finish()
                                                    }
                                                    .addOnFailureListener { loadingDialog.dismiss() }
                                            }
                                            .addOnFailureListener { loadingDialog.dismiss() }
                                    } else {
                                        Toast.makeText(
                                            this@RequestActivity,
                                            "Accepted Failed sent!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadingDialog.dismiss()
                                    }
                                }

                                override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                                    Toast.makeText(
                                        this@RequestActivity,
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

    private fun cancel() {
        TOKENS_REF.orderBy(FieldPath.documentId())
            .whereEqualTo(FieldPath.documentId(), request!!.from)
            .get().addOnSuccessListener {
                it.forEach { tokenDoc ->
                    if (tokenDoc.id == request!!.from) {
                        val userToken: Token = tokenDoc.toObject(Token::class.java)
                        val msgContent: MutableMap<String, String> = HashMap()
                        msgContent["title"] = "Booking"
                        msgContent["requestId"] = request!!.id!!
                        msgContent["requestStatus"] = "canceled"
                        val dataMsg = DataMessage(userToken.token, msgContent)
                        fcmService.sendMessage(dataMsg)
                            .enqueue(object : Callback<FCMResponse> {
                                override fun onResponse(
                                    call: Call<FCMResponse>,
                                    response: Response<FCMResponse>
                                ) {
                                    if (response.isSuccessful) {
//                                        REQUESTS_REF.document(request!!.id!!)
//                                            .update("status", "accepted")
//                                            .addOnSuccessListener {
//                                                Toast.makeText(
//                                                    this@RequestActivity,
//                                                    "Accepted",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                                loadingDialog.dismiss()
//                                                onDestroy()
//                                            }.addOnFailureListener { loadingDialog.dismiss() }
                                        val notification = Notification(
                                            customerId = request!!.from,
                                            providerId = request!!.to,
                                            requestId = request!!.id!!,
                                            notiType = "canceled"
                                        )
                                        notification.time = TimeUtil.getDefaultTimeText(
                                            System.currentTimeMillis(),
                                            TimeZone.getDefault()
                                        )
                                        USERS_REF.document(notification.customerId!!)
                                            .update(
                                                "notifications",
                                                FieldValue.arrayUnion(notification)
                                            )
                                            .addOnSuccessListener {
                                                REQUESTS_REF.document(notification.requestId!!)
                                                    .update("status", "canceled")
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            this@RequestActivity,
                                                            "Canceled",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        loadingDialog.dismiss()
                                                        finish()
                                                    }
                                                    .addOnFailureListener { loadingDialog.dismiss() }
                                            }
                                            .addOnFailureListener { loadingDialog.dismiss() }
                                    } else {
                                        Toast.makeText(
                                            this@RequestActivity,
                                            "Canceled Failed sent!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                                    Toast.makeText(
                                        this@RequestActivity,
                                        "ERROR Canceled SENT! " + t.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                }

            }

    }


}
