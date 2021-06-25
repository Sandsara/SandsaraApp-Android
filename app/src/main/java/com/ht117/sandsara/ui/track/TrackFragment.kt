package com.ht117.sandsara.ui.track

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import coil.load
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.databinding.FragmentTrackBinding
import com.ht117.sandsara.ext.checkTrackExisted
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.sync.SyncFragment.Companion.KEY_APPEND
import com.ht117.sandsara.ui.sync.SyncFragment.Companion.KEY_TRACK
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Track state
 *
 * @property track
 * @property isDownloading
 * @property downloadFailed
 * @property percent
 * @property isFinished
 * @constructor Create empty Track state
 */
data class TrackState(val track: Resource<Track> = Resource.Idle(),
                      val isDownloading: Boolean = false,
                      val downloadFailed: Boolean = false,
                      val percent: Int = 0,
                      val isFinished: Boolean = false): IState

/**
 * Track action
 *
 * @constructor Create empty Track action
 */
sealed class TrackAction: IAction {
    /**
     * Trace state
     *
     * @property track
     * @constructor Create empty Trace state
     */
    data class TraceState(val track: Track): TrackAction()

    /**
     * Add to favorite
     *
     * @property track
     * @constructor Create empty Add to favorite
     */
    data class AddToFavorite(val track: Track): TrackAction()

    /**
     * Download track
     *
     * @property track
     * @constructor Create empty Download track
     */
    data class DownloadTrack(val track: TrackUiModel): TrackAction()
}

/**
 * Track fragment
 *
 * @constructor Create empty Track fragment
 */
class TrackFragment: BaseFragment(R.layout.fragment_track), IView<TrackState> {

    private lateinit var binding: FragmentTrackBinding
    private val viewModel: TrackViewModel by viewModel()
    private lateinit var curTrack: TrackUiModel

    override fun initView(view: View) {
        super.initView(view)
        curTrack = (arguments?.get(KEY) as TrackUiModel?)?: throw RuntimeException("Missing track info")

        binding = FragmentTrackBinding.bind(view)

        binding.apply {
            toolbar.apply {
                ivThumb.load(curTrack.track.images.firstOrNull()?.url?: "") {
                    placeholder(R.drawable.ic_playlist)
                    error(R.drawable.ic_playlist)
                }

                ivBack.setOnClickListener { navigateBack() }
            }

            tvTrackTitle.text = curTrack.track.name
            tvTrackAuthor.text = curTrack.track.author

            btnAddToQueue.setOnClickListener { addToQueue() }
            btnPlay.setOnClickListener { playTrack() }

            btnAddToPlaylist.setOnClickListener { addToPlaylist() }
            btnFavorite.setOnClickListener { addFavorite() }
            btnDownload.setOnClickListener {
                nlvProgress.show()
                downloadTrack()
            }

            handleState()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.state.collect { render(it) }
        }
    }

    override fun initLogic() {
        super.initLogic()
        lifecycleScope.launch {
            viewModel.actions.emit(TrackAction.TraceState(curTrack.track))
        }
    }

    override fun render(state: TrackState) {
        lifecycleScope.launch {
            if (state.downloadFailed) {
                binding.nlvProgress.hide()
                binding.btnDownload.text = getString(R.string.download_to_library)
                showToast(getString(R.string.download_failed))
            }

            if (state.isFinished) {
                binding.btnDownload.hide()
                binding.nlvProgress.hide()
            }

            if (state.isDownloading) {
                binding.nlvProgress.setProgress(state.percent)
            }

            when (state.track) {
                is Resource.Stream -> {
                    state.track.stream.collect {
                        curTrack = curTrack.copy(track = it)

                        toggleFavorite()

                        handleState(state.isDownloading)
                    }
                }
                else -> {}
            }
        }
    }

    private fun addToQueue() {
        lifecycleScope.launch {
            if (BleManager.hasConnection()) {
                if (BleManager.checkTrackExist(curTrack.track)) {
                    if (BleManager.addPath(listOf(curTrack.track))) {
                        showToast(R.string.add_to_queue_success)
                        navigateTo(R.id.navigate_to_playing)
                    } else {
                        showToast(R.string.add_to_queue_failed)
                    }
                } else {
                    navigateTo(R.id.navigate_to_sync, bundleOf(KEY_TRACK to listOf(curTrack.track), KEY_APPEND to true))
                }
            } else {
                showToast(getString(R.string.require_connection))
            }
        }
    }

    private fun playTrack() {
        lifecycleScope.launch {
            if (BleManager.hasConnection()) {
                if (BleManager.checkTrackExist(curTrack.track)) {
                    if (BleManager.playTrack(curTrack.track)) {
                        navigateTo(R.id.navigate_to_playing)
                    } else {
                        showToast("Failed to play this track")
                    }
                } else {
                    navigateTo(R.id.navigate_to_sync, bundleOf(KEY_TRACK to listOf(curTrack.track), KEY_APPEND to true))
                }
            } else {
                showToast(getString(R.string.require_connection))
            }
        }
    }

    private fun addFavorite() {
        lifecycleScope.launch {
            val fileName = curTrack.track.sandFiles.firstOrNull()?.filename

            if (fileName == null || !requireContext().checkTrackExisted(fileName)) {
                showToast("You need to download the track first")
            } else {
                val isFavorite = !curTrack.track.isFavorite
                viewModel.actions.emit(TrackAction.AddToFavorite(curTrack.track.copy(isFavorite = isFavorite)))
            }
        }
    }

    private fun toggleFavorite() {
        if (curTrack.track.isFavorite) {
            binding.btnFavorite.setIconResource(R.drawable.ic_selected_favorite)
        } else {
            binding.btnFavorite.setIconResource(R.drawable.ic_favorite)
        }
    }

    private fun downloadTrack() {
        lifecycleScope.launch {
            binding.btnDownload.text = getString(R.string.downloading)
            binding.nlvProgress.show()
            viewModel.actions.emit(TrackAction.DownloadTrack(curTrack))
        }
    }

    private fun addToPlaylist() {
        navigateTo(R.id.add_track_to_playlist, bundleOf("track" to curTrack))
    }

    private fun handleState(isDownloading: Boolean = false) {
        lifecycleScope.launch {
            binding.run {
                if (!isDownloading) {
                    val fileName = curTrack.track.sandFiles.firstOrNull()?.filename ?: ""
                    if (!requireContext().checkTrackExisted(fileName)) {
                        btnDownload.show()
                    } else {
                        btnDownload.hide()
                        btnPlay.show()
                        btnAddToPlaylist.show()
                        btnAddToQueue.show()
                        btnFavorite.show()
                    }
                }
            }
        }
    }

    companion object {
        const val KEY = "track"
    }
}