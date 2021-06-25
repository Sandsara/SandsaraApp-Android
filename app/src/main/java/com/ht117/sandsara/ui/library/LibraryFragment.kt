package com.ht117.sandsara.ui.library

import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.LibraryAdapter
import com.ht117.sandsara.databinding.FragmentLibraryBinding
import com.ht117.sandsara.ui.base.BaseFragment

class LibraryFragment: BaseFragment(R.layout.fragment_library) {

    private lateinit var binding: FragmentLibraryBinding

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentLibraryBinding.bind(view)

        binding.apply {
            pager.adapter = LibraryAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            TabLayoutMediator(tabHeader, pager) { tab, pos ->
                tab.text = if (pos == 0) {
                    getString(R.string.track)
                } else {
                    getString(R.string.playlist)
                }
            }.attach()
        }

    }
}