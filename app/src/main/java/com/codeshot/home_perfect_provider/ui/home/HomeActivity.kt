package com.codeshot.home_perfect_provider.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.CURRENT_TOKEN
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_PHONE
import com.codeshot.home_perfect_provider.common.Common.PROVIDERS_REF
import com.codeshot.home_perfect_provider.common.Common.TOKENS_REF
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.databinding.ActivityHomeBinding
import com.codeshot.home_perfect_provider.models.Provider
import com.codeshot.home_perfect_provider.models.Token
import com.codeshot.home_perfect_provider.ui.LoginActivity
import com.codeshot.home_perfect_provider.ui.dialogs.AddServiceDialog
import com.codeshot.home_perfect_provider.ui.dialogs.DialogUpdateUserInfo
import com.codeshot.home_perfect_provider.ui.myRequests.MyRequestsDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import io.ghyeok.stickyswitch.widget.StickySwitch
import io.ghyeok.stickyswitch.widget.StickySwitch.OnSelectedChangeListener


class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"
    private lateinit var activityHomeBinding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private var dialogUpdateUserInfo=DialogUpdateUserInfo()
    private var sharedPreferences: SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityHomeBinding = DataBindingUtil.setContentView(this,R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        sharedPreferences =Common.getSharedPref(this)
        CURRENT_USER_KEY = FirebaseAuth.getInstance().currentUser!!.uid
        CURRENT_USER_PHONE =FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        homeViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(HomeViewModel::class.java)
        homeViewModel.getInstance(this)

        checkIntentData()
        activityHomeBinding.floatingActionButton.setOnClickListener {
            val myRequestsDialog=MyRequestsDialog()
            myRequestsDialog.show(supportFragmentManager,"MyRequestsDialog")
        }
        activityHomeBinding.imgUImage.setOnClickListener {
            dialogUpdateUserInfo.show(this.supportFragmentManager, "FullDialogFragment")
        }

        // Set Selected Change Listener
        activityHomeBinding.switchStatus.onSelectedChangeListener =
            object : OnSelectedChangeListener {
                override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                    Log.d(TAG, "Now Selected : " + direction.name + ", Current Text : " + text)
                    if (text == "online")
                        PROVIDERS_REF.document(CURRENT_USER_KEY).update("online", true)
                    else
                        PROVIDERS_REF.document(CURRENT_USER_KEY).update("online", false)
                }
            }
        setupBottomBar()

    }
    private fun setupBottomBar(){
        activityHomeBinding.bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.nav_addService->{
                    val dialogFragment = AddServiceDialog()
                    dialogFragment.show(this.supportFragmentManager, "AddServiceDialog")
                    true
                }
                R.id.nav_SignOut->{
                    signOut()
                    true
                }
                else->false
            }
        }

    }

    private fun checkIntentData() {
        if (intent != null) {
            if (intent.getStringExtra("user") != null) {
                if (intent.getStringExtra("user") == "new") {
                    checkNewProviderData()
                    checkToken()
                } else checkProviderData()

            }
        }
    }

//    private fun showRequestDialog(requestId: String?) {
//        val requestDialog = RequestDialog(requestId!!)
//        requestDialog.show(this.supportFragmentManager, "RequestDialog")
//    }

    private fun checkToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instantResult ->
                val token = instantResult.token
                updateTokenToServer(token)
                CURRENT_TOKEN = token
                val sharedPreferences = getSharedPreferences(
                    "com.codeshot.home_perfect",
                    Context.MODE_PRIVATE
                )
                sharedPreferences.edit().putString("token", token).apply()
            }
    }

    private fun updateTokenToServer(newToken: String) {
        val token = Token(newToken)
        if (FirebaseAuth.getInstance()
                .currentUser != null
        ) //if already login, must update Token
        {
            TOKENS_REF.document(CURRENT_USER_KEY)
                .set(token)
                .addOnSuccessListener { Log.i("Saved Token", "Token Uploaded") }
                .addOnFailureListener { e -> Log.i("ERROR TOKEN", e.message!!) }

        }
    }

    private fun checkNewProviderData() {
        val acProgressBaseDialog = ACProgressFlower.Builder(this)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Please Wait ....!")
            .fadeColor(Color.DKGRAY).build()
        acProgressBaseDialog.show()
        // Source can be CACHE, SERVER, or DEFAULT.
        val source = Source.SERVER
        var provider: Provider?
        PROVIDERS_REF.document(CURRENT_USER_KEY).get(source)
            .addOnSuccessListener { document ->
                if (!document!!.exists()) {
                    this.supportFragmentManager.executePendingTransactions()
                    dialogUpdateUserInfo.show(this.supportFragmentManager, "FullDialogFragment")
                } else {
                    provider = document.toObject(Provider::class.java)
                    Snackbar.make(
                        activityHomeBinding.floatingActionButton,
                        "Welcome ${provider!!.userName}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    activityHomeBinding.provider = provider
                    val gsonProvider = Gson().toJson(provider)
                    sharedPreferences!!.edit().putString("provider", gsonProvider).apply()


                }
                acProgressBaseDialog.hide()
            }.addOnFailureListener { e ->
                Log.e("Error USR_DATA", e.message!!)
            }
    }

    fun checkProviderData() {
        val acProgressBaseDialog = ACProgressFlower.Builder(this)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Please Wait ....!")
            .fadeColor(Color.DKGRAY).build()
        acProgressBaseDialog.show()
        homeViewModel.provider!!.observe(this, Observer {
            activityHomeBinding.provider = it
            acProgressBaseDialog.dismiss()
        })


    }

     fun signOut(){
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,
            LoginActivity::class.java))
        finish()
    }

}
