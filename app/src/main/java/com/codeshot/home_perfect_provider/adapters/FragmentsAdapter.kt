package com.codeshot.home_perfect_provider.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.codeshot.home_perfect_provider.ui.home.HomeFragment
import com.codeshot.home_perfect_provider.ui.payments.PaymentsFragment
import com.codeshot.home_perfect_provider.ui.profile.ProfileFragment
import com.codeshot.home_perfect_provider.ui.requests.RequestsFragment

class FragmentsAdapter(private val context: Context, fm: FragmentManager, behavior: Int) :
    FragmentPagerAdapter(fm, behavior) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> RequestsFragment()
            2 -> PaymentsFragment()
            else -> ProfileFragment()
        }

    }

    override fun getCount(): Int {
        return 4
    }

}