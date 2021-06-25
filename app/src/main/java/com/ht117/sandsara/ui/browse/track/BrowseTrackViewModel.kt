package com.ht117.sandsara.ui.browse.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.repo.ITrackRepo
import com.ht117.sandsara.repo.TrackPaging
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class BrowseTrackViewModel(private val trackRepo: ITrackRepo): ViewModel(), IModel<BrowseTrackState, BrowseTrackAction> {

    override val actions = MutableSharedFlow<BrowseTrackAction>()

    private val _state = MutableStateFlow(BrowseTrackState(emptyFlow()))
    override val state: StateFlow<BrowseTrackState>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    BrowseTrackAction.BrowseAll -> browseAll()
                    is BrowseTrackAction.BrowseKey -> browseKey(it.key)
                }
            }
        }
    }

    private fun browseAll() {
        viewModelScope.launch {
            val flow = Pager(PagingConfig(pageSize = 20)) { TrackPaging("", trackRepo) }.flow.cachedIn(viewModelScope)
            _state.value = BrowseTrackState(data = flow)
        }
    }

    private fun browseKey(key: String) {
        viewModelScope.launch {
            val flow = Pager(PagingConfig(pageSize = 20)) { TrackPaging(key, trackRepo) }.flow.cachedIn(viewModelScope)
            _state.value = BrowseTrackState(data = flow)
        }
    }
}