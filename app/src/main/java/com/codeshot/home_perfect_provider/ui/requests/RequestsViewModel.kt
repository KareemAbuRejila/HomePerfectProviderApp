package com.codeshot.home_perfect_provider.ui.requests

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Request
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.MetadataChanges
import com.google.gson.Gson

class RequestsViewModel : ViewModel() {
    private var sharedPreferences: SharedPreferences = Common.SHARED_PREF!!
    val bookings = MutableLiveData<List<Request>>()


    fun getInstance(context: Context) {
        if (sharedPreferences != null) return
    }

    fun getRequest() {
        val bookingsLocal = ArrayList<Request>()
        val jsonProvider = sharedPreferences!!.getString("provider", null)
        if (jsonProvider != null) {
            val provider = Gson().fromJson<Provider>(jsonProvider, Provider::class.java)
            provider.requests.forEach { id ->
                Common.REQUESTS_REF.document(id)
                    .get().addOnSuccessListener {
                        bookingsLocal.add(it.toObject(Request::class.java)!!)
                        bookings.value = bookingsLocal
                    }
            }
        }
    }

    val connected = MutableLiveData<Boolean>().apply {
        Common.PROVIDERS_REF
            .addSnapshotListener(
                MetadataChanges.INCLUDE,
                EventListener { t, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) return@EventListener
                    value = !t!!.metadata.isFromCache
                })
    }
}
