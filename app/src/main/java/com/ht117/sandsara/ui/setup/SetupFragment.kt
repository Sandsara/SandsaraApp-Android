package com.ht117.sandsara.ui.setup

import android.view.View
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.FragmentSetupBinding
import com.ht117.sandsara.ui.base.BaseFragment

class SetupFragment: BaseFragment(R.layout.fragment_setup) {

    private lateinit var binding: FragmentSetupBinding

    override fun initView(view: View) {
        super.initView(view)

        binding = FragmentSetupBinding.bind(view)

        binding.run {
            tvConnect.setOnClickListener {
                navigateTo(R.id.setup_to_discovery)
            }
        }
    }
}