package com.ht117.sandsara.ui.library.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.repo.IPlaylistRepo
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AllPlaylistViewModel(private val playlistRepo: IPlaylistRepo)
    : ViewModel(), IModel<PlaylistState, PlaylistAction> {

    override val actions = MutableSharedFlow<PlaylistAction>()

    private val _state = MutableStateFlow(PlaylistState())
    override val state: StateFlow<PlaylistState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    PlaylistAction.LoadPlaylist -> loadPlaylist()
                }
            }
        }
    }

    private suspend fun loadPlaylist() {
        try {
            _state.value = _state.value.copy(playlist = Resource.Loading())
            _state.value = _state.value.copy(playlist = Resource.Stream(playlistRepo.loadDownloadedPlaylist()))
        } catch (exp: Exception) {
            _state.value = _state.value.copy(playlist = Resource.Error(exp.message?: ""))
        }
    }
}