package com.ht117.sandsara.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ht117.sandsara.ui.browse.playlist.BrowsePlaylistFragment
import com.ht117.sandsara.ui.browse.track.BrowseTrackFragment

class BrowseAdapter(manager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(manager, lifecycle) {

    private val items = listOf(BrowseTrackFragment(), BrowsePlaylistFragment())

    override fun getItemCount() = items.size

    override fun createFragment(position: Int): Fragment {
        return items[position] as Fragment
    }

    fun getItem(pos: Int): Fragment {
        return items[pos] as Fragment
    }
}