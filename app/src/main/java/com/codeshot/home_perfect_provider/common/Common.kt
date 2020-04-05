package com.codeshot.home_perfect_provider.common

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import cc.cloudist.acplibrary.ACProgressBaseDialog
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
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
    val NOTIFICATIONS_REF = ROOT_REF.collection("Notifications")


    private const val FCM_URL = "https://fcm.googleapis.com/"

    val TOKENS_REF = ROOT_REF.collection("Tokens")

    val FCM_SERVICE: IFCMService
        get() = FCMClient.getClient(FCM_URL)!!.create(IFCMService::class.java)

    fun LOADING_DIALOG(context: Context): ACProgressBaseDialog {
        val acProgressBaseDialog = ACProgressFlower.Builder(context)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Please Wait ....!")
            .fadeColor(Color.DKGRAY).build()
        return acProgressBaseDialog
    }

    fun SHARED_PREF(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            "com.codeshot.home_perfect",
            Context.MODE_PRIVATE
        )
    }

    fun getSharedPref(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            "com.codeshot.home_perfect_provider",
            Context.MODE_PRIVATE
        )
    }
}