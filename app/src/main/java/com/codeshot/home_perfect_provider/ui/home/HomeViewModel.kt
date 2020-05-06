package com.codeshot.home_perfect_provider.ui.home

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.models.Provider
import com.google.gson.Gson

class HomeViewModel : ViewModel() {
    var sharedPreferences: SharedPreferences = Common.SHARED_PREF!!
    val provider = getProviderData()

    private fun getProviderData(): MutableLiveData<Provider> {
        val _provider = MutableLiveData<Provider>()
        Common.PROVIDERS_REF.document(CURRENT_USER_KEY).get()
            .addOnSuccessListener { document ->
                if (!document!!.exists()) {
                    return@addOnSuccessListener
                } else {
                    val provider = document.toObject(Provider::class.java)
                    provider!!.id = document.id
                    val jsonProvider = Gson().toJson(provider)
                    sharedPreferences.edit().putString("provider", jsonProvider).apply()
                    _provider.value = provider
                }
            }.addOnFailureListener { e ->
                val jsonProvider = sharedPreferences.getString("provider", "null")
                if (jsonProvider != "null") {
                    val provider = Gson().fromJson(jsonProvider, Provider::class.java)
                    provider.online = false
                    _provider.value = provider
                } else {
                    _provider.value = null
                }
            }
        return _provider
    }

}
