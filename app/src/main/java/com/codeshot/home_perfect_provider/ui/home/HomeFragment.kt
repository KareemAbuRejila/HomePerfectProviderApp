package com.codeshot.home_perfect_provider.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codeshot.home_perfect_provider.common.Common

import com.codeshot.home_perfect_provider.databinding.FragmentHomeBinding
import com.codeshot.home_perfect_provider.ui.dialogs.DialogUpdateUserInfo
import com.codeshot.home_perfect_provider.ui.main.MainActivity
import io.ghyeok.stickyswitch.widget.StickySwitch

class HomeFragment : Fragment() {
    private lateinit var fragmentHomeFragmentBinding: FragmentHomeBinding
    private var dialogUpdateUserInfo = DialogUpdateUserInfo()


    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentHomeFragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return fragmentHomeFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider.NewInstanceFactory().create(HomeViewModel::class.java)

        viewModel.provider.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                dialogUpdateUserInfo.show(
                    (activity as MainActivity).supportFragmentManager,
                    "FullDialogFragment"
                )
            } else {
                fragmentHomeFragmentBinding.provider = it
            }
        })

        fragmentHomeFragmentBinding.imgUImage.setOnClickListener {
            dialogUpdateUserInfo.show(
                (activity as MainActivity).supportFragmentManager,
                "FullDialogFragment"
            )
        }
        // Set Selected Change Listener
        fragmentHomeFragmentBinding.switchStatus.onSelectedChangeListener =
            object : StickySwitch.OnSelectedChangeListener {
                override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                    if (text == "online")
                        Common.PROVIDERS_REF.document(Common.CURRENT_USER_KEY)
                            .update("online", true)
                    else
                        Common.PROVIDERS_REF.document(Common.CURRENT_USER_KEY)
                            .update("online", false)
                }
            }


    }

}
