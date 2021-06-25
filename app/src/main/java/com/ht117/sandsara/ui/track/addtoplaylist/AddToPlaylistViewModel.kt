package com.ht117.sandsara.ui.track.addtoplaylist

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

/**
 * Add to playlist view model
 *
 * @property playlistRepo
 * @property trackRepo
 * @constructor Create empty Add to playlist view model
 */
class AddToPlaylistViewModel(private val playlistRepo: IPlaylistRepo,
                             private val trackRepo: ITrackRepo): ViewModel(), IModel<AddToPlaylistState, AddPlaylistAction> {
    override val actions = MutableSharedFlow<AddPlaylistAction>()

    private val _state = MutableStateFlow(AddToPlaylistState())
    override val state: StateFlow<AddToPlaylistState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    is AddPlaylistAction.LoadAllPlaylist -> {
                        loadAllPlaylist(it.trackId)
                    }
                    is AddPlaylistAction.AddTracksToPlaylist -> {
                        addTrackToPlaylists(it.ids, it.track)
                    }
                    is AddPlaylistAction.CreateNewPlaylist -> {
                        createNewPlaylist(it.name, it.track)
                    }
                }
            }
        }
    }

    private suspend fun createNewPlaylist(name: String, track: Track) {
        try {
            Timber.d("Create new one $name")
            val playlist = Playlist(playlistId = "${name}.${System.currentTimeMillis()}", author = "You", name = name, tracks = 0)
            playlistRepo.addNewPlaylist(playlist)
            playlistRepo.addTrackToPlaylist(playlist.playlistId, track)
            _state.value = _state.value.copy(addStatus = Resource.Success(true))
        } catch (exp: Exception) {
            Timber.d("Failed ${exp.message}")
        }
    }

    private suspend fun loadAllPlaylist(trackId: String) {
        try {
            _state.value = _state.value.copy(curPlaylist = Resource.Loading())
            _state.value = _state.value.copy(curPlaylist = Resource.Stream(playlistRepo.loadPlaylistWithoutTrack(trackId)))
        } catch (exp: Exception) {
            Timber.d(exp.message)
            _state.value = _state.value.copy(curPlaylist = Resource.Error(exp.message?: ""))
        }
    }

    private suspend fun addTrackToPlaylists(ids: String, track: Track) {
        try {
            _state.value = _state.value.copy(addStatus = Resource.Loading())
            playlistRepo.addTrackToPlaylist(ids, track)
            if (ids == "favorite") {
                trackRepo.updateTrackFavorite(track.copy(isFavorite = true))
            }
            _state.value = _state.value.copy(addStatus = Resource.Success(true))
        } catch (exp: Exception) {
            Timber.d("Failed to add")
            _state.value = _state.value.copy(addStatus = Resource.Error("${exp.message}"))
        }
    }
}