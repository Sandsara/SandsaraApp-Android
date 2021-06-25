package com.ht117.sandsara.ui.sync

import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.FragmentSyncBinding
import com.ht117.sandsara.ext.SyncTrack
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.ui.base.BaseFragment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SyncFragment: BaseFragment(R.layout.fragment_sync) {

    private val allTracks by lazy { arguments?.get(KEY_TRACK) as List<Track> }
    private val isReplace by lazy { (arguments?.get(KEY_REPLACE) as Boolean?)?: false }
    private val isAppend by lazy { (arguments?.get(KEY_APPEND) as Boolean?)?: false }
    private val playlistName by lazy { (arguments?.get(KEY_NAME) as String?)?: "temporary"  }
    private lateinit var binding: FragmentSyncBinding
    private var count = 0
    private var unSync = -1

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentSyncBinding.bind(view)
        addFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        lifecycleScope.launch {
            try {
                val unSyncTracks = BleManager.getUnSyncFiles(allTracks)
                unSync = unSyncTracks.size
                binding.tvInfo.text = getString(R.string.sync_info, count, unSyncTracks.size)

                unSyncTracks.map {
                    BleManager.sendTrack(it)
                }.asFlow().flattenConcat()
                    .onCompletion {
                        syncDone()
                    }
                    .catch {
                        navigateBack()
                    }
                    .collect {
                    when (it) {
                        is SyncTrack.Starting -> {
                            binding.tvSyncTrack.text = it.track.name
                            binding.syncProgress.setProgress(0)
                        }
                        is SyncTrack.Progress -> {
                            binding.syncProgress.setProgress(it.progress)
                        }
                        is SyncTrack.Completed -> {
                            binding.syncProgress.setProgress(100)
                            count += 1
                            binding.tvInfo.text = getString(R.string.sync_info, count, unSyncTracks.size)
                        }
                        is SyncTrack.Error -> {
                            Timber.d("Failed to sync")
                        }
                    }
                }
            } catch (exp: Exception) {
                Timber.d("Something unexpected occurred...${exp.message}")
                navigateBack()
            }
        }
    }

    override fun initLogic() {
        super.initLogic()
        if (count == unSync) {
            syncDone()
        }
    }

    private fun syncDone() {
        lifecycleScope.launch {
            if (isAppend) {
                if (BleManager.addPath(allTracks)) {
                    navigateTo(R.id.sync_to_playing)
                } else {
                    Timber.d("Failed")
                    navigateBack()
                }
            } else {
                if (BleManager.playPlaylist(playlistName, allTracks, isReplace)) {
                    navigateTo(R.id.sync_to_playing)
                } else {
                    Timber.d("Play playlist failed")
                    navigateBack()
                }
            }
        }
    }

    companion object {
        const val KEY_TRACK = "track"
        const val KEY_APPEND = "append"
        const val KEY_REPLACE = "is_replace"
        const val KEY_NAME = "playlist_name"
    }
}