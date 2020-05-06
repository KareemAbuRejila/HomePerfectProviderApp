package com.codeshot.home_perfect_provider

import android.app.Application
import androidx.preference.PreferenceManager
import com.codeshot.home_perfect_provider.common.Common

class HomePerfectProviderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Common.SHARED_PREF = PreferenceManager.getDefaultSharedPreferences(this)
    }
}