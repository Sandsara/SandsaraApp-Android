package com.ht117.sandsara.ui.library.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.repo.ITrackRepo
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class AllTrackViewModel(private val trackRepo: ITrackRepo): ViewModel(), IModel<AllTrackState, AllTrackAction> {
    override val actions = MutableSharedFlow<AllTrackAction>()

    private val _state = MutableStateFlow(AllTrackState())
    override val state: StateFlow<AllTrackState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    is AllTrackAction.LoadAllTracks -> {
                        loadData()
                    }
                    is AllTrackAction.UpdateTrack -> {
//                        updateSync(it.track)
                    }
                }
            }
        }
    }

    private suspend fun loadData() {
        try {
            _state.value = _state.value.copy(trackRes = Resource.Loading())
            _state.value = _state.value.copy(trackRes = Resource.Stream(trackRepo.loadDownloadedTrack()))
        } catch (exp: Exception) {
            exp.printStackTrace()
            _state.value = _state.value.copy(trackRes = Resource.Error(exp.message?: ""))
        }
    }

//    private suspend fun updateSync(track: Track) {
//        try {
//            Timber.d("Update sync status $track")
//            trackRepo.updateTrackSync(track)
//        } catch (exp: Exception) {
//            exp.printStackTrace()
//        }
//    }
}