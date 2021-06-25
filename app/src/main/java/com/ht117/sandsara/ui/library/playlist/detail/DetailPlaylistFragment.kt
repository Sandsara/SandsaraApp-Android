package com.ht117.sandsara.ui.library.playlist.detail

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.*
import com.ht117.sandsara.createHolder
import com.ht117.sandsara.databinding.FragmentDetailPlaylistBinding
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.*
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.sync.SyncFragment.Companion.KEY_NAME
import com.ht117.sandsara.ui.sync.SyncFragment.Companion.KEY_REPLACE
import com.ht117.sandsara.ui.sync.SyncFragment.Companion.KEY_TRACK
import com.ht117.sandsara.ui.track.TrackFragment
import com.maxkeppeler.bottomsheets.info.InfoSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

data class DetailPlaylistState(val tracksRes: Resource<List<Track>> = Resource.Idle(),
                               val playlist: Resource<Playlist> = Resource.Idle(),
                               val isDeleted: Boolean = false,
                               val downloadingTrack: TrackUiModel? = null,
                               val syncingTrack: TrackUiModel? = null,
                               val isDownloaded: Boolean = false,
                               val isSynced: Boolean = false): IState

sealed class DetailPlaylistAction: IAction {
    object LoadFavorite: DetailPlaylistAction()
    data class LoadPlaylistDetail(val playlistId: String): DetailPlaylistAction()
    data class DownloadTrack(val playlist: Playlist, val tracks: List<TrackUiModel>): DetailPlaylistAction()
    data class UpdateTrack(val track: Track): DetailPlaylistAction()
    data class DeleteTrackFromPlaylist(val playlistId: String, val track: Track): DetailPlaylistAction()
    data class DeletePlaylist(val playlist: Playlist): DetailPlaylistAction()
    data class UpdateDownload(val playlist: Playlist, val isDownloaded: Boolean): DetailPlaylistAction()
}

class DetailPlaylistFragment: BaseFragment(R.layout.fragment_detail_playlist), IView<DetailPlaylistState> {

    private lateinit var binding: FragmentDetailPlaylistBinding
    private val viewModel: DetailPlaylistViewModel by viewModel()
    private val playlistId by lazy { arguments?.get(KEY) as String}
    private lateinit var curPlaylist: Playlist
    private val swipeCallback = object: (Int) -> Unit {
        override fun invoke(pos: Int) {
            lifecycleScope.launch {
                curPlaylist.run {
                    val item = adapter.getItem(pos)
                    viewModel.actions.emit(
                        DetailPlaylistAction.DeleteTrackFromPlaylist(
                            playlistId,
                            item.track
                        )
                    )
                }
            }
        }

    }

    private val adapter by lazy {
        TrackDetailAdapter(requireContext(), object: ITrackListener {
            override fun onClickItem(track: TrackUiModel) {
                navigateTo(R.id.detail_playlist_to_track, bundleOf(TrackFragment.KEY to track))
            }

        })
    }

    override fun initView(view: View) {
        super.initView(view)

        binding = FragmentDetailPlaylistBinding.bind(view)

        binding.run {
            toolbar.run {
                ivBack.setOnClickListener { navigateBack() }
                ivDelete.setOnClickListener {
                    showAlert()
                }
            }

            btnDownload.setOnClickListener {
                val items = adapter.getUnDownloadItems()
                downloadItems(items)
            }

            ivPlay.setOnClickListener {
                lifecycleScope.launch {
                    if (BleManager.hasConnection()) {
                        val tracks = curPlaylist.toTracks()

                        val unSync = BleManager.getUnSyncFiles(tracks)
                        if (unSync.isEmpty()) {
                            if (BleManager.playPlaylist(curPlaylist.name, tracks, true)) {
                                navigateTo(R.id.navigate_to_playing)
                            } else {
                                showToast(getString(R.string.failed_to_play))
                            }
                        } else {
                            navigateTo(R.id.navigate_to_sync, bundleOf(
                                KEY_TRACK to tracks,
                                KEY_REPLACE to true,
                                KEY_NAME to curPlaylist.name))
                        }
                    } else {
                        showToast(getString(R.string.require_connection))
                    }
                }
            }

            rvTracks.itemAnimator = null
            rvTracks.addSwipeCallback(swipeCallback)
            rvTracks.setHasFixedSize(true)
            rvTracks.adapter = adapter
        }

        lifecycleScope.run {
            launchWhenCreated { viewModel.state.collect { render(it) } }
            launchWhenResumed {
                if (playlistId == getString(R.string.favorite)) {
                    viewModel.actions.emit(DetailPlaylistAction.LoadFavorite)
                } else {
                    viewModel.actions.emit(DetailPlaylistAction.LoadPlaylistDetail(playlistId))
                }
            }
        }
    }

    override fun cleanup() {
        binding.rvTracks.adapter = null
        super.cleanup()
    }

    override fun render(state: DetailPlaylistState) {
        handleDelete(state.isDeleted)
        handleDownload(state.downloadingTrack, state.isDownloaded)
        handlePlaylist(state.playlist)
    }

    private fun downloadItems(requestTracks: List<TrackUiModel>) = lifecycleScope.launch {
        adapter.mode = TrackMode_Downloading
        binding.btnDownload.text = getString(R.string.downloading)
        binding.btnDownload.isEnabled = false

        viewModel.actions.emit(DetailPlaylistAction.DownloadTrack(curPlaylist, requestTracks))
    }

    private fun handleDownload(downloadTrackState: TrackUiModel?, isDownloaded: Boolean) = lifecycleScope.launch {
        downloadTrackState?.run {
            if (!this.isDownloaded || this.downloadProgress < 100) {
                adapter.updateItem(this)
            }
        }
        if (isDownloaded) {
            binding.btnDownload.hide()
            binding.ivPlay.show()
            adapter.mode = TrackMode_Ready
            lifecycleScope.launch {
                viewModel.actions.emit(
                    DetailPlaylistAction.UpdateDownload(curPlaylist, true)
                )
            }
        }
    }

    private fun handlePlaylist(playlist: Resource<Playlist>) = lifecycleScope.launch {
        binding.run {
            when (playlist) {
                is Resource.Loading -> loader.show()
                is Resource.Stream -> {
                    loader.hide()
                    playlist.stream.collect { playlist ->
                        curPlaylist = playlist
                        handleInfo()

                        val models = Mapper.tracksToTrackUiModels(requireContext(), playlist.toTracks())
                        if (adapter.itemCount == 0) {
                            adapter.addAll(models)
                        } else {
                            if (adapter.mode != TrackMode_Downloading) {
                                adapter.updateItems(models)
                            }
                        }
                        if (playlist.trackModels.isEmpty() && adapter.itemCount == 0) {
                            ivNotFound.show()
                        } else {
                            ivNotFound.hide()
                        }

                        if (adapter.mode == TrackMode_Unknown) {
                            if (!playlist.isDownloaded) {
                                adapter.mode = TrackMode_Download
                                ivPlay.visibility = View.INVISIBLE
                                btnDownload.show()
                            } else {
                                adapter.mode = TrackMode_Ready
                                btnDownload.hide()
                                ivPlay.show()
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    loader.hide()
                    showToast(getString(R.string.failed_to_load_playlist))
                }
                else -> {}
            }
        }
    }

    private fun handleInfo() {
        binding.run {
            tvPlaylistName.text = curPlaylist.name
            tvPlaylistAuthor.text = curPlaylist.author

            toolbar.run {
                if (!curPlaylist.isDelete && curPlaylist.isDownloaded && curPlaylist.playlistId != "favorite") {
                    ivDelete.show()
                } else {
                    ivDelete.hide()
                }

                if (!curPlaylist.trackModels.isNullOrEmpty()) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        if (ivThumb.tag != "loaded") {
                            val bmp = createHolder(requireContext(), curPlaylist.toTracks())
                            withContext(Dispatchers.Main) {
                                ivThumb.setImageBitmap(bmp)
                                ivThumb.tag = "loaded"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleDelete(isDeleted: Boolean) {
        if (isDeleted) {
            showToast("Delete playlist success")
            navigateBack()
        }
    }

    private fun showAlert() {
        InfoSheet().show(requireContext()) {
            title(R.string.delete_playlist)
            content(R.string.do_you_want_to_delete)
            onNegative(R.string.cancel)
            onPositive(R.string.ok) {
                lifecycleScope.launch {
                    viewModel.actions.emit(DetailPlaylistAction.DeletePlaylist(curPlaylist))
                }
            }
        }
    }

    companion object {

        const val KEY = "playlist"
        const val FAVORITE = "favorite"
        const val FROM_REMOTE = "from_remote"
    }
}