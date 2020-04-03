package com.codeshot.home_perfect_provider.ui.myRequests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.Common.Common
import com.codeshot.home_perfect_provider.Common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.Common.Common.PROVIDERS_REF
import com.codeshot.home_perfect_provider.Common.Common.REQUESTS_REF
import com.codeshot.home_perfect_provider.Common.Common.USERS_REF
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Request
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source

class MyRequestsViewModel : ViewModel() {


    private val _bookings= MutableLiveData<List<Request>>().apply {
        val reqests=ArrayList<Request>()
        PROVIDERS_REF.document(CURRENT_USER_KEY)
            .get(Source.SERVER).addOnSuccessListener { providerDoc ->
                val provider=providerDoc.toObject(Provider::class.java)
                REQUESTS_REF
                    .orderBy("time", Query.Direction.DESCENDING)
                    .get().addOnSuccessListener {requestsQueryDoc->
                        requestsQueryDoc.forEach {requestDoc->
                            if (provider!!.requests.contains(requestDoc.id)){
                                val request=requestDoc.toObject(Request::class.java)
                                reqests.add(request)
                            }
                        }
                        value=reqests
                    }
            }
    }
    val bookings: LiveData<List<Request>> =_bookings
}