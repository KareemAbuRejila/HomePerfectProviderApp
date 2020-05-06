package com.codeshot.home_perfect_provider.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.CURRENT_TOKEN
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_KEY
import com.codeshot.home_perfect_provider.common.Common.CURRENT_USER_PHONE
import com.codeshot.home_perfect_provider.common.Common.PROVIDERS_REF
import com.codeshot.home_perfect_provider.common.Common.TOKENS_REF
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.adapters.FragmentsAdapter
import com.codeshot.home_perfect_provider.databinding.ActivityMainBinding
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


class MainActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {
    private val TAG = "HomeActivity"
    lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private var dialogUpdateUserInfo = DialogUpdateUserInfo()
    private var sharedPreferences: SharedPreferences? = null
    var provider: Provider? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        val fragmentPagerAdapter = FragmentsAdapter(this, supportFragmentManager, 1)
        activityMainBinding.mainViewPager.adapter = fragmentPagerAdapter
        sharedPreferences = Common.getSharedPref(this)
        CURRENT_USER_KEY = FirebaseAuth.getInstance().currentUser!!.uid
        CURRENT_USER_PHONE = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        mainViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(MainViewModel::class.java)
        mainViewModel.getInstance(this)

        checkIntentData()

        activityMainBinding.mainViewPager.addOnPageChangeListener(this)

        setupBottomBar()

    }

    private fun setupBottomBar() {
        activityMainBinding.bottomAppBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_Home -> activityMainBinding.mainViewPager.currentItem = 0
                R.id.nav_requests -> activityMainBinding.mainViewPager.currentItem = 1
                R.id.nav_payments -> activityMainBinding.mainViewPager.currentItem = 2
                R.id.nav_profile -> activityMainBinding.mainViewPager.currentItem = 3
            }
            it.isChecked = true
            false
        }

    }

    private fun checkIntentData() {
        mainViewModel.checkToken()
        mainViewModel.provider!!.observe(this, Observer {
            if (it == null) {
                dialogUpdateUserInfo.show(this.supportFragmentManager, "FullDialogFragment")
            } else {
                activityMainBinding.provider = it
            }
        })
    }

//    private fun showRequestDialog(requestId: String?) {
//        val requestDialog = RequestDialog(requestId!!)
//        requestDialog.show(this.supportFragmentManager, "RequestDialog")
//    }


//    fun checkNewProviderData() {
//        val acProgressBaseDialog = ACProgressFlower.Builder(this)
//            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
//            .themeColor(Color.WHITE)
//            .text("Please Wait ....!")
//            .fadeColor(Color.DKGRAY).build()
//        acProgressBaseDialog.show()
//        // Source can be CACHE, SERVER, or DEFAULT.
//        val source = Source.SERVER
//        PROVIDERS_REF.document(CURRENT_USER_KEY).get(source)
//            .addOnSuccessListener { document ->
//                if (!document!!.exists()) {
//                    dialogUpdateUserInfo.show(this.supportFragmentManager, "FullDialogFragment")
//                    acProgressBaseDialog.dismiss()
//                    return@addOnSuccessListener
//                }
//                provider = document.toObject(Provider::class.java)
//                provider!!.id = CURRENT_USER_KEY
//                val jsonProvider = Gson().toJson(provider)
//                sharedPreferences!!.edit().putString("provider", jsonProvider).apply()
//                activityMainBinding.provider = provider
//
//                acProgressBaseDialog.hide()
//            }.addOnFailureListener { e ->
//                Log.e("Error USR_DATA", e.message!!)
//            }
//    }
//
//    private fun checkProviderData() {
//        val acProgressBaseDialog = ACProgressFlower.Builder(this)
//            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
//            .themeColor(Color.WHITE)
//            .text("Please Wait ....!")
//            .fadeColor(Color.DKGRAY).build()
//        acProgressBaseDialog.show()
//        PROVIDERS_REF.document(CURRENT_USER_KEY).get().addOnSuccessListener { Doc ->
//            if (!Doc.exists()) {
//                dialogUpdateUserInfo.show(this.supportFragmentManager, "FullDialogFragment")
//                acProgressBaseDialog.dismiss()
//                return@addOnSuccessListener
//            }
//            provider = Doc.toObject(Provider::class.java)
//            provider!!.id = CURRENT_USER_KEY
//            val jsonProvider = Gson().toJson(provider)
//            sharedPreferences!!.edit().putString("provider", jsonProvider).apply()
//            activityMainBinding.provider = provider
//            acProgressBaseDialog.dismiss()
//        }.addOnFailureListener { e ->
//            val jsonProvider = sharedPreferences!!.getString("provider", "null")
//            if (jsonProvider != "null") {
//                val provider = Gson().fromJson(jsonProvider, Provider::class.java)
//                provider.online = false
//                activityMainBinding.provider = provider
//                acProgressBaseDialog.dismiss()
//            } else {
//                MainActivity().signOut()
//            }
//        }
////
////        homeViewModel.provider!!.observe(this, Observer {
////            activityHomeBinding.provider = it
////            acProgressBaseDialog.dismiss()
////        })
////
//
//    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
        sharedPreferences!!.edit().remove("provider").apply()
        finish()
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        activityMainBinding.bottomAppBar.menu.getItem(position).isChecked = true

    }

    override fun onPageSelected(position: Int) {
        activityMainBinding.bottomAppBar.menu.getItem(position).isChecked = true
    }


}
