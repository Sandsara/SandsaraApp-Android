package com.ht117.sandsara.ui.playing

import android.view.View
import androidx.lifecycle.lifecycleScope
import coil.load
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.PlayingTrackAdapter
import com.ht117.sandsara.adapter.PlayingTrackCallback
import com.ht117.sandsara.databinding.FragmentNowPlayingBinding
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.*
import com.ht117.sandsara.ui.base.BaseFragment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class PlayingFragment: BaseFragment(R.layout.fragment_now_playing) {

    private lateinit var binding: FragmentNowPlayingBinding
    private var status: DeviceStatus = DeviceStatus()
    private val adapter by lazy {
        PlayingTrackAdapter(object : PlayingTrackCallback {
            override fun invoke(track: Track, clickPos: Int) {
                lifecycleScope.launch {
                    val index = BleManager.playingTracks.indexOf(track)
                    BleManager.playPosition(index + 1)
                }
            }
        })
    }

    override fun initView(view: View) {
        super.initView(view)

        binding = FragmentNowPlayingBinding.bind(view)

        binding.run {
            toolbar.run {
                ivBack.setOnClickListener {
                    navigateBack()
                }
                tvTitle.show()
            }
            sliderProgress.isEnabled = false
            rvTracks.adapter = adapter
            ivNext.setOnClickListener {
                lifecycleScope.launch {
                    when (val pos = status.pos) {
                        BleManager.playingTracks.size -> BleManager.playPosition(1)
                        else -> BleManager.playPosition(pos + 1)
                    }
                }
            }
            ivPrev.setOnClickListener {
                lifecycleScope.launch {
                    when (val pos = status.pos) {
                        1 -> BleManager.playPosition(BleManager.playingTracks.size)
                        else -> BleManager.playPosition(pos - 1)
                    }
                }
            }
            ivPlay.setOnClickListener {
                lifecycleScope.launch {
                    if (ivPlay.tag == State_Pause) {
                        BleManager.playOrResume()
                    } else if (ivPlay.tag == State_Play) {
                        BleManager.pauseDevice()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            flowOf(BleManager.localStatus, BleManager.deviceStateFlow).flattenMerge().collect {
                when (it) {
                    is DeviceStatus -> {
                        handleState(it)
                        handleProgress(it.progress)
                    }
                    is ProgressStatus -> handleProgress(it.progress)
                }
            }
        }
    }

    private fun handleState(newState: DeviceStatus) {
        status = newState
        renderTracks()
        when (newState.state) {
            State.Playing -> {
                binding.run {
                    if (ivPlay.tag != State_Play) {
                        ivPlay.setImageResource(R.drawable.ic_play)
                        ivPlay.tag = State_Play
                    }
                }
            }
            State.Paused -> {
                binding.run {
                    if (ivPlay.tag != State_Pause) {
                        ivPlay.setImageResource(R.drawable.ic_pause)
                        ivPlay.tag = State_Pause
                    }
                }
            }
            else -> {}
        }
    }

    private fun handleProgress(progress: Int) {
        binding.run {
            sliderProgress.value = progress.toFloat()
        }
    }

    private fun renderTracks() {
        lifecycleScope.launch {
            try {
                val tracks = BleManager.playingTracks
                val playingTrack = tracks[status.pos - 1]
                Timber.d("Playing ${playingTrack.name} in ${BleManager.playingTracks.map { it.name }}")

                binding.run {
                    toolbar.ivThumb.load(playingTrack.images.firstOrNull()?.url) {
                        error(R.drawable.ic_playlist)
                    }
                    tvPlaylistName.text = playingTrack.name
                    tvPlaylistAuthor.text = playingTrack.author
                }

                when (status.pos) {
                    1 -> adapter.update(tracks.subList(1, tracks.size))
                    tracks.size -> adapter.update(tracks.subList(0, tracks.size - 1))
                    else -> {
                        val first = tracks.subList(status.pos, tracks.size)
                        val second = tracks.subList(0, status.pos - 1)
                        val result = first.plus(second)
                        adapter.update(result)
                    }
                }

            } catch (exp: Exception) {
                Timber.d("Err ${exp.message}")
            }
        }
    }
}