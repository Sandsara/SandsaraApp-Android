
package com.ht117.sandsara.ui.library.playlist.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.ext.SyncTrackModel
import com.ht117.sandsara.ext.TrackPath
import com.ht117.sandsara.ext.createTemp
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.repo.IPlaylistRepo
import com.ht117.sandsara.repo.ITrackRepo
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailPlaylistViewModel(private val context: Context,
                              private val playlistRepo: IPlaylistRepo,
                              private val trackRepo: ITrackRepo): ViewModel(), IModel<DetailPlaylistState, DetailPlaylistAction> {
    override val actions = MutableSharedFlow<DetailPlaylistAction>()

    private val _state = MutableStateFlow(DetailPlaylistState())
    override val state: StateFlow<DetailPlaylistState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    is DetailPlaylistAction.LoadFavorite -> {
                        loadFavorites()
                    }
                    is DetailPlaylistAction.LoadPlaylistDetail -> {
                        loadPlaylistInfo(it.playlistId)
                    }
                    is DetailPlaylistAction.DeleteTrackFromPlaylist -> {
                        deleteTrack(it.playlistId, it.track)
                    }
                    is DetailPlaylistAction.DeletePlaylist -> {
                        deletePlaylist(it.playlist)
                    }
                    is DetailPlaylistAction.DownloadTrack -> {
                        downloadTrack(it.playlist, it.tracks)
                    }
                    is DetailPlaylistAction.UpdateTrack -> {
//                        updateSyncTrack(it.track)
                    }
                    is DetailPlaylistAction.UpdateDownload -> {
                        updateDownload(it.playlist, it.isDownloaded)
                    }
                }
            }
        }
    }

    private fun updateDownload(playlist: Playlist, downloaded: Boolean) = viewModelScope.launch {
        try {
            playlistRepo.updateDownload(playlist, downloaded)
        } catch (exp: Exception) {
            Timber.d("Failed ${exp.message}")
        }
    }

    private suspend fun loadFavorites() {
        try {
            _state.value = _state.value.copy(playlist = Resource.Loading())
            _state.value = _state.value.copy(playlist = Resource.Stream(playlistRepo.loadPlaylistInfo("favorite")))
        } catch (exp: Exception) {
            _state.value = _state.value.copy(playlist = Resource.Error(exp.message?: ""))
        }
    }

    private suspend fun loadPlaylistInfo(playlistId: String) {
        try {
            _state.value = _state.value.copy(playlist = Resource.Loading())
            _state.value = _state.value.copy(playlist = Resource.Stream(playlistRepo.loadPlaylistInfo(playlistId)))
        } catch (exp: Exception) {
            exp.printStackTrace()
            _state.value = _state.value.copy(playlist = Resource.Error(exp.message?: ""))
        }
    }

    private suspend fun deleteTrack(playlistId: String, track: Track) {
        playlistRepo.removeTrackFromPlaylist(playlistId, track)
        if (playlistId == "favorite") {
            trackRepo.updateTrackFavorite(track.copy(isFavorite = false))
        }
    }

    private suspend fun deletePlaylist(playlist: Playlist) {
        try {
            playlistRepo.deletePlaylist(playlist)
            _state.value = _state.value.copy(isDeleted = true, isDownloaded = false)
        } catch (exp: Exception) {
            exp.printStackTrace()
            Timber.d("Exception ${exp.message}")
        }
    }

    private suspend fun downloadTrack(playlist: Playlist, track: List<TrackUiModel>) = viewModelScope.launch {
        try {
            if (track.isNotEmpty()) {
                val multiple = track.asFlow().map {
                    val sandFile = it.track.sandFiles.first()
                    val file = context.createTemp(sandFile.filename, TrackPath)
                    trackRepo.downloadTrack(file, it)
                }

                multiple.flattenConcat().onCompletion {
                    _state.value = _state.value.copy(isDownloaded = true)
                }.catch {
                    it.printStackTrace()
                    Timber.d("Failed when downloading ${it.message}")
                }.collect {
                    when (it) {
                        is SyncTrackModel.Progress -> {
                            Timber.d("Downloading ${it.track.track.name} ${it.track.downloadProgress}")
                            _state.value = _state.value.copy(downloadingTrack = it.track)
                        }
                        is SyncTrackModel.Completed -> {
                            Timber.d("Download completed ${it.track}")
                            _state.value = _state.value.copy(downloadingTrack = it.track)
                        }
                        is SyncTrackModel.Error -> {
                            Timber.d("Failed ${it.msg}")
                        }
                        else -> {}
                    }
                }
            } else {
                _state.value = _state.value.copy(isDownloaded = true)
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
            Timber.d(exp)
        }
    }
}