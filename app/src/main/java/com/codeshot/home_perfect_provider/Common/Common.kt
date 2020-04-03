package com.codeshot.home_perfect_provider.Common

import android.content.Context
import android.content.SharedPreferences
import cc.cloudist.acplibrary.ACProgressBaseDialog
import com.codeshot.home_perfect_provider.remote.IFCMService
import com.codeshot.home_perfect_provider.services.FCMClient
import com.google.firebase.firestore.FirebaseFirestore

object Common {
    var CURRENT_USER_KEY = ""
    var CURRENT_TOKEN = ""
    var CURRENT_USER_PHONE = ""
    private val ROOT_REF = FirebaseFirestore.getInstance()
    val PROVIDERS_REF = ROOT_REF.collection("Providers")
    val USERS_REF = ROOT_REF.collection("Users")
    val SERVICES_REF = ROOT_REF.collection("Services")
    val REQUESTS_REF = ROOT_REF.collection("Requests")


    private const val FCM_URL = "https://fcm.googleapis.com/"

    val TOKENS_REF = ROOT_REF.collection("Tokens")

    val FCM_SERVICE: IFCMService
        get() = FCMClient.getClient(FCM_URL)!!.create(IFCMService::class.java)

    //    val LOADING_DIALOG:ACProgressBaseDialog
    fun getSharedPref(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            "com.codeshot.home_perfect_provider",
            Context.MODE_PRIVATE
        )
    }
}