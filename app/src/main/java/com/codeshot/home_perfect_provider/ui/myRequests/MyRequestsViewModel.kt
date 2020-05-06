package com.codeshot.home_perfect_provider.ui.myRequests

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.common.Common.PROVIDERS_REF
import com.codeshot.home_perfect_provider.common.Common.REQUESTS_REF
import com.codeshot.home_perfect_provider.common.Common.SHARED_PREF
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Request
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldPath

class MyRequestsViewModel : ViewModel() {
    private var sharedPreferences: SharedPreferences = Common.SHARED_PREF!!
    val bookings = MutableLiveData<List<Request>>()


    fun getInstance(context: Context) {
        if (sharedPreferences != null) return
    }

    fun getRequest(requests: ArrayList<String>) {
        val bookingsLocal = ArrayList<Request>()
        requests.forEach { id ->
            REQUESTS_REF.document(id)
                .get().addOnSuccessListener {
                    bookingsLocal.add(it.toObject(Request::class.java)!!)
                    bookings.value = bookingsLocal
                }

        }
    }

}