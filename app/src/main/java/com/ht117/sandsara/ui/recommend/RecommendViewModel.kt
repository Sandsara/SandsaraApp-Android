package com.ht117.sandsara.ui.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.repo.IPlaylistRepo
import com.ht117.sandsara.repo.ITrackRepo
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class RecommendViewModel(private val playlistRepo: IPlaylistRepo,
                         private val trackRepo: ITrackRepo): ViewModel(), IModel<RecommendState, RecommendAction> {

    override val actions = MutableSharedFlow<RecommendAction>()

    private val _state = MutableStateFlow(RecommendState(tracks = emptyList(), playlists = emptyList()))
    override val state: StateFlow<RecommendState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    RecommendAction.Init -> {
                        load()
                    }
                }
            }
        }
    }

    private fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoadingTrack = true, isLoadingPlaylist = true)
        try {
            val tracks = trackRepo.loadRecommendTracks()
            val playlists = playlistRepo.loadRecommendPlaylists()

            _state.value = _state.value.copy(isLoadingTrack = false, isLoadingPlaylist = false,
                tracks = tracks,
                playlists = playlists)
        } catch (exp: Exception) {
            Timber.d("Failed ${exp.message}")
            _state.value = _state.value.copy(isLoadingTrack = false, isLoadingPlaylist = false)
        }
    }

    private fun loadTrack() = viewModelScope.launch {
        try {
            _state.value = _state.value.copy(isLoadingTrack = true)
            _state.value = _state.value.copy(isLoadingTrack = false, tracks = trackRepo.loadRecommendTracks())
        } catch (exp: Exception) {
            _state.value = _state.value.copy(isLoadingTrack = false)
        }
    }

    private fun loadPlaylist() = viewModelScope.launch {
        try {
            _state.value = _state.value.copy(isLoadingPlaylist = true)
            _state.value = _state.value.copy(isLoadingPlaylist = false, playlists = playlistRepo.loadRecommendPlaylists())
        } catch (exp: Exception) {
            _state.value = _state.value.copy(isLoadingPlaylist = false)
        }
    }
}