package com.ht117.sandsara.ui.browse

import android.view.View
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.BrowseAdapter
import com.ht117.sandsara.databinding.FragmentBrowseBinding
import com.ht117.sandsara.ui.base.BaseFragment
import com.ht117.sandsara.ui.browse.playlist.BrowsePlaylistFragment
import com.ht117.sandsara.ui.browse.track.BrowseTrackFragment
import com.ht117.sandsara.view.SearchView

class BrowseFragment: BaseFragment(R.layout.fragment_browse) {

    private lateinit var binding: FragmentBrowseBinding
    private val adapter by lazy { BrowseAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) }

    override fun initView(view: View) {
        super.initView(view)

        binding = FragmentBrowseBinding.bind(view)
        binding.apply {
            pager.adapter = adapter
            TabLayoutMediator(tabHeader, pager) { tab, pos ->
                tab.text = if (pos == 0) {
                    getString(R.string.track)
                } else {
                    getString(R.string.playlist)
                }
            }.attach()
            tabHeader.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    searchView.clearText()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
            searchView.addOnListener(object: SearchView.ISearchListener {
                override fun onTouch() {

                }

                override fun onChange(key: String) {
                    handleKey(key)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        binding.pager.adapter = adapter
    }

    override fun onPause() {
        binding.pager.adapter = null
        super.onPause()
    }

    private fun handleKey(key: String?) {
        val item = adapter.getItem(binding.pager.currentItem)
        if (item is BrowseTrackFragment) {
            item.sendKey(key)
        } else if (item is BrowsePlaylistFragment) {
            item.sendKey(key)
        }
    }
}