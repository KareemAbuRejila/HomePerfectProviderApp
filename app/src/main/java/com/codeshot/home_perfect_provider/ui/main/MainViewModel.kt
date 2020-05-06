package com.codeshot.home_perfect_provider.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cc.cloudist.acplibrary.ACProgressBaseDialog
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Token
import com.codeshot.home_perfect_provider.ui.dialogs.DialogUpdateUserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson


class MainViewModel : ViewModel() {
    var sharedPreferences: SharedPreferences = Common.SHARED_PREF!!
    var provider: MutableLiveData<Provider>? = MutableLiveData()
    private lateinit var acProgressBaseDialog: ACProgressBaseDialog


    fun getInstance(context: Context) {
        acProgressBaseDialog = Common.LOADING_DIALOG(context)
        provider = getProviderData()
    }

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

    fun checkToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instantResult ->
                val token = instantResult.token
                updateTokenToServer(token)
                Common.CURRENT_TOKEN = token
                sharedPreferences.edit().putString("token", token).apply()
            }
    }

    private fun updateTokenToServer(newToken: String) {
        val token = Token(newToken)
        if (FirebaseAuth.getInstance()
                .currentUser != null
        ) //if already login, must update Token
        {
            Common.TOKENS_REF.document(CURRENT_USER_KEY)
                .set(token)
                .addOnSuccessListener { Log.i("Saved Token", "Token Uploaded") }
                .addOnFailureListener { e -> Log.i("ERROR TOKEN", e.message!!) }

        }
    }

    val connected = MutableLiveData<Boolean>().apply {
        Common.PROVIDERS_REF
            .addSnapshotListener(MetadataChanges.INCLUDE,
                EventListener { t, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) return@EventListener
                    value = !t!!.metadata.isFromCache
                })
    }


//    val provider: LiveData<Provider> = _provider

}