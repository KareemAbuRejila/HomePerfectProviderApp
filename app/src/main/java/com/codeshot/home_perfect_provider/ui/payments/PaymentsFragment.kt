package com.codeshot.home_perfect_provider.ui.payments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.codeshot.home_perfect_provider.R

class PaymentsFragment : Fragment() {

    companion object {
        fun newInstance() = PaymentsFragment()
    }

    private lateinit var viewModel: PaymentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payments, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PaymentsViewModel::class.java)
        // TODO: Use the ViewModel

    }

}
