package com.ht117.sandsara.ui.browse.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.repo.IPlaylistRepo
import com.ht117.sandsara.repo.PlaylistPaging
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BrowsePlaylistViewModel(private val playlistRepo: IPlaylistRepo): ViewModel(),
    IModel<BrowsePlaylistState, BrowsePlaylistAction> {
    override val actions = MutableSharedFlow<BrowsePlaylistAction>()

    private val _state = MutableStateFlow(BrowsePlaylistState(emptyFlow()))
    override val state: StateFlow<BrowsePlaylistState>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    BrowsePlaylistAction.BrowseAll -> {
                        browseAll()
                    }
                    is BrowsePlaylistAction.BrowseKey -> {
                        if (it.key.isNullOrEmpty()) {
                            browseAll()
                        } else {
                            browseKey(it.key)
                        }
                    }
                }
            }
        }
    }

    private fun browseAll() {
        viewModelScope.launch {
            val flow = Pager(PagingConfig(pageSize = 20)) { PlaylistPaging("", playlistRepo) }
                .flow.cachedIn(viewModelScope)
            _state.value = BrowsePlaylistState(flow)
        }
    }

    private fun browseKey(key: String) {
        viewModelScope.launch {
            val flow = Pager(PagingConfig(pageSize = 20)) { PlaylistPaging(key, playlistRepo) }
                .flow.cachedIn(viewModelScope)
            _state.value = BrowsePlaylistState(flow)
        }
    }
}