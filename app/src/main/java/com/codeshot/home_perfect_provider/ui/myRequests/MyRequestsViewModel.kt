package com.codeshot.home_perfect_provider.ui.myRequests

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.common.Common.PROVIDERS_REF
import com.codeshot.home_perfect_provider.common.Common.REQUESTS_REF
import com.codeshot.home_perfect_provider.common.Common.SHARED_PREF
import com.codeshot.home_perfect_provider.common.Common.USERS_REF
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Request
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source

class MyRequestsViewModel : ViewModel() {
    private var sharedPreferences: SharedPreferences? = null
    val bookingsOption = MutableLiveData<FirestoreRecyclerOptions<Request>>()

    fun getInstance(context: Context) {
        if (sharedPreferences != null) return
        sharedPreferences = SHARED_PREF(context = context)
    }

    fun getRequest() {
        PROVIDERS_REF.document(CURRENT_USER_KEY)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) return@addSnapshotListener
                val provider = documentSnapshot!!.toObject(Provider::class.java)
                if (provider!!.requests.isNotEmpty()) {
                    val requestQuery =
                        REQUESTS_REF.whereIn(FieldPath.documentId(), provider.requests)
                    bookingsOption.value = FirestoreRecyclerOptions.Builder<Request>()
                        .setQuery(requestQuery, Request::class.java)
                        .build()
                } else bookingsOption.value = null
            }
    }

}