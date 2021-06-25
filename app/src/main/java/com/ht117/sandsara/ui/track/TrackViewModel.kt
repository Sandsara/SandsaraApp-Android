package com.ht117.sandsara.ui.track

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.ext.SyncTrackModel
import com.ht117.sandsara.ext.TrackPath
import com.ht117.sandsara.ext.createTemp
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.repo.IPlaylistRepo
import com.ht117.sandsara.repo.ITrackRepo
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Track view model
 *
 * @property context
 * @property playlistRepo
 * @property trackRepo
 * @constructor Create empty Track view model
 */
class TrackViewModel(private val context: Context,
                     private val playlistRepo: IPlaylistRepo,
                     private val trackRepo: ITrackRepo): ViewModel(), IModel<TrackState, TrackAction> {

    override val actions = MutableSharedFlow<TrackAction>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val _state = MutableStateFlow(TrackState())
    override val state: StateFlow<TrackState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    is TrackAction.TraceState -> queryTrack(it.track)
                    is TrackAction.AddToFavorite -> addToFavorite(it.track)
                    is TrackAction.DownloadTrack -> downloadTrack(it.track)
                }
            }
        }
    }

    private suspend fun queryTrack(track: Track) = viewModelScope.launch {
        try {
            _state.value = _state.value.copy(track = Resource.Stream(trackRepo.getTrack(track)))
        } catch (exp: Exception) {
            Timber.d("Issue get tracing ${exp.message}")
        }
    }

    private suspend fun downloadTrack(track: TrackUiModel) = viewModelScope.launch(Dispatchers.IO) {
        val sandFile = track.track.sandFiles.firstOrNull()?: return@launch
        if (!sandFile.url.isNullOrEmpty()) {
            try {
                _state.value = _state.value.copy(isDownloading = true)
                val file = context.createTemp(sandFile.filename, TrackPath)
                trackRepo.downloadTrack(file, track).collect {
                    when (it) {
                        is SyncTrackModel.Error -> {
                            _state.value = _state.value.copy(downloadFailed = true)
                        }
                        is SyncTrackModel.Starting -> {
                            _state.value = _state.value.copy(downloadFailed = false)
                        }
                        is SyncTrackModel.Progress -> {
                            _state.value = _state.value.copy(percent = it.track.downloadProgress)
                        }
                        is SyncTrackModel.Completed -> {
                            _state.value = _state.value.copy(isDownloading = false, isFinished = true)
                        }
                    }
                }
            } catch (exp: Exception) {
                _state.value = _state.value.copy(isDownloading = false, downloadFailed = true)
                Timber.d("Exception ${exp.message}")
            }
        }
    }

    private suspend fun addToFavorite(track: Track) = viewModelScope.launch {
        if (track.isFavorite) {
            playlistRepo.addTrackToPlaylist("favorite", track)
        } else {
            playlistRepo.removeTrackFromPlaylist("favorite", track)
        }
        trackRepo.updateTrackFavorite(track)
    }
}