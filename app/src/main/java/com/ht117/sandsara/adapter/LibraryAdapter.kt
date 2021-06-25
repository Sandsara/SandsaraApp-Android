package com.ht117.sandsara.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ht117.sandsara.ui.library.playlist.AllPlaylistFragment
import com.ht117.sandsara.ui.library.track.AllTrackFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

class LibraryAdapter(manager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(manager, lifecycle) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllTrackFragment()
            else -> AllPlaylistFragment()
        }
    }
}