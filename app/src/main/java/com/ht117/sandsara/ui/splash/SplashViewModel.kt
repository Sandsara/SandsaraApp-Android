package com.ht117.sandsara.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.ext.Prefs
import com.ht117.sandsara.ext.read
import com.ht117.sandsara.ext.write
import com.ht117.sandsara.repo.IDeviceRepo
import com.ht117.sandsara.repo.IPlaylistRepo
import com.ht117.sandsara.repo.ITrackRepo
import com.ht117.sandsara.ui.base.IModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Splash view model
 *
 * @property context
 * @property trackRepo
 * @property playlistRepo
 * @property deviceRepo
 * @constructor Create empty Splash view model
 */
class SplashViewModel(private val context: Context,
                      private val trackRepo: ITrackRepo,
                      private val playlistRepo: IPlaylistRepo,
                      private val deviceRepo: IDeviceRepo)
    : ViewModel(), IModel<SplashState, SplashAction> {

    private val TIME = 0
    override val actions = MutableSharedFlow<SplashAction>()

    private val _state = MutableStateFlow(SplashState())
    override val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        handleIntent()
    }

    private fun handleIntent() = viewModelScope.launch {
        actions.collect {
            try {
                val lastSync = context.read(Prefs.LastSync, Long.MAX_VALUE)
                if (System.currentTimeMillis() > lastSync + TIME) {
                    _state.value = _state.value.copy(isFinished = true)
                } else {
                    trackRepo.loadAll()
                    playlistRepo.loadAll()
                    deviceRepo.loadPalettes()
                    deviceRepo.loadFirmware()
                    context.write(Prefs.LastSync, System.currentTimeMillis())
                    _state.value = _state.value.copy(isFinished = true)
                }
            } catch (exp: Exception) {
                Timber.d("Exception ${exp.message}")
                _state.value = _state.value.copy(isFinished = true)
            }
        }
    }

}