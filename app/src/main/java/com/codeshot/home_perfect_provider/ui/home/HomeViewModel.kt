package com.codeshot.home_perfect_provider.ui.home

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.models.Provider
import com.google.firebase.firestore.Source
import com.google.gson.Gson


class HomeViewModel : ViewModel() {
    var sharedPreferences: SharedPreferences? = null
    var provider: LiveData<Provider>? = null

    fun getInstance(context: Context) {
        if (provider != null) {
            return
        }
        sharedPreferences = Common.getSharedPref(context)
        provider = getProviderData()
    }

    fun getProviderData(): MutableLiveData<Provider> {
        val _provider = MutableLiveData<Provider>()
        // Source can be CACHE, SERVER, or DEFAULT.
        val source = Source.SERVER
        val jsonProvider = sharedPreferences!!.getString("provider", "null")
        Common.PROVIDERS_REF.document(Common.CURRENT_USER_KEY).get(source)
            .addOnSuccessListener { document ->
                if (!document!!.exists()) {
                    return@addOnSuccessListener
                } else {
                    val provider = document.toObject(Provider::class.java)
                    _provider.value = provider

                }
            }.addOnFailureListener { e ->
                if (jsonProvider != "null") {
                    val provider = Gson().fromJson(jsonProvider, Provider::class.java)
                    provider.online = false
                    _provider.value = provider
                }else{
                    HomeActivity().signOut()
                }
            }
        return _provider
    }
//    val provider: LiveData<Provider> = _provider

}