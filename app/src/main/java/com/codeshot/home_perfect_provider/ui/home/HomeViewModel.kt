package com.codeshot.home_perfect_provider.ui.home

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.ui.dialogs.DialogUpdateUserInfo
import com.google.firebase.firestore.Source
import com.google.gson.Gson


class HomeViewModel : ViewModel() {
    var sharedPreferences: SharedPreferences? = null
    var provider: MutableLiveData<Provider>? = MutableLiveData()
    private var dialogUpdateUserInfo = DialogUpdateUserInfo()
    private lateinit var context: Context


    fun getInstance(context: Context) {
        if (provider != null) {
            return
        }
        sharedPreferences = Common.getSharedPref(context)

        provider = getProviderData()
    }

    private fun getProviderData(): MutableLiveData<Provider> {
        val _provider = MutableLiveData<Provider>()
        Common.PROVIDERS_REF.document(Common.CURRENT_USER_KEY).get()
            .addOnSuccessListener { document ->
                if (!document!!.exists()) {
                    return@addOnSuccessListener
                } else {
                    val provider = document.toObject(Provider::class.java)
                    val jsonProvider = Gson().toJson(provider)
                    sharedPreferences!!.edit().putString("provider", jsonProvider).apply()
                    _provider.value = provider

                }
            }.addOnFailureListener { e ->
                val jsonProvider = sharedPreferences!!.getString("provider", "null")
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