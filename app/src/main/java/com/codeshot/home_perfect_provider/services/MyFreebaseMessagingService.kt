package com.codeshot.home_perfect_provider.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.common.Common.TOKENS_REF
import com.codeshot.home_perfect_provider.Helpers.NotificationsHelper
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.models.Token
import com.codeshot.home_perfect_provider.ui.home.HomeActivity
import com.codeshot.home_perfect_provider.ui.requestActivity.RequestActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFreebaseMessagingService : FirebaseMessagingService() {
    var notificationManager: NotificationManager? = null
    var notificationsHelper: NotificationsHelper? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        notificationManager =
            baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationsHelper = NotificationsHelper(baseContext)

        val dataMessage=remoteMessage.data
        val title=dataMessage["title"]
        if (title!! == "Booking"){
            val userId=dataMessage["userPhone"]
            val userKey=dataMessage["userKey"]
            val userName=dataMessage["userName"]
            val requestId=dataMessage["requestId"]

            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
                showBookingNotificationAPI26(requestId!!,userName!!)
            }else
                showBookingNotification(requestId!!,userName!!)
//            startBookingActivity(requestId,userName)

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showArrivedNotificationAPI26(requestId: String, userName: String) {
        val homeIntent=Intent(baseContext,
            HomeActivity::class.java)
        homeIntent.putExtra("type","booking")
        homeIntent.putExtra("requestId",requestId)
        val contentIntent = PendingIntent.getActivity(
            baseContext,
            0, homeIntent, PendingIntent.FLAG_ONE_SHOT
        )
        val defaultSound =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder: Notification.Builder = notificationsHelper!!.getCarsNotification(
            "Booking",
            "You Have Book From : "+userName,
            contentIntent,
            defaultSound
        )
        builder.setAutoCancel(true)
        notificationsHelper!!.manager!!.notify(1, builder.build())

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showBookingNotificationAPI26(requestId: String, userName: String) {
        val reauestIntent=Intent(baseContext,
            RequestActivity::class.java)
        reauestIntent.putExtra("type","booking")
        reauestIntent.putExtra("requestId",requestId)
        val contentIntent = PendingIntent.getActivity(
            baseContext,
            0, reauestIntent, PendingIntent.FLAG_ONE_SHOT
        )
        val defaultSound =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder: Notification.Builder = notificationsHelper!!.getCarsNotification(
            "Booking",
            "You Have Book From : "+userName,
            contentIntent,
            defaultSound
        )
        builder.setAutoCancel(true)
        notificationsHelper!!.manager!!.notify(1, builder.build())

    }


    private fun showArrivedNotification(body: String, userName: String) {
        //This code only work for android api 25 and below
        //from android api 26 of higher i need create notification channel
        val contentIntent = PendingIntent.getActivity(
            baseContext,
            0, Intent(), PendingIntent.FLAG_ONE_SHOT
        )
        val builder =
            NotificationCompat.Builder(baseContext)
        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Bookgin")
            .setContentText("You Have Book From : "+body)
            .setContentIntent(contentIntent)
        notificationManager!!.notify(1, builder.build())
    }
    private fun showBookingNotification(body: String, userName: String) {
        //This code only work for android api 25 and below
        //from android api 26 of higher i need create notification channel
        val contentIntent = PendingIntent.getActivity(
            baseContext,
            0, Intent(), PendingIntent.FLAG_ONE_SHOT
        )
        val builder =
            NotificationCompat.Builder(baseContext)
        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Bookgin")
            .setContentText("You Have Book From : "+body)
            .setContentIntent(contentIntent)
        notificationManager!!.notify(1, builder.build())
    }


    private fun startBookingActivity(requestId: String, userName: String){
        val requestIntent=Intent(baseContext,
            RequestActivity::class.java)
        requestIntent.putExtra("type","booking")
        requestIntent.putExtra("requestId",requestId)
        requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(requestIntent)

    }


    override fun onNewToken(s: String) {
        super.onNewToken(s)
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                val newToken = instanceIdResult.token
                updateTokenToServer(newToken) //When have new token, i need update to our realtime db
                val sharedPreferences = getSharedPreferences(
                    "com.codeshot.home_perfect",
                    Context.MODE_PRIVATE
                )
                sharedPreferences.edit().putString("token", s).apply()
            }
        Log.d("NEW_TOKEN", s)
    }
    private fun updateTokenToServer(newToken: String) {
        val token = Token(newToken)
        if (FirebaseAuth.getInstance()
                .currentUser != null
        ) //if already login, must update Token
        {
            TOKENS_REF.document(CURRENT_USER_KEY)
                .set(token)
                .addOnSuccessListener { Log.i("Saved Token", "Yesssssssssssssssssssssss") }
                .addOnFailureListener { e -> Log.i("ERROR TOKEN", e.message) }

        }
    }
}