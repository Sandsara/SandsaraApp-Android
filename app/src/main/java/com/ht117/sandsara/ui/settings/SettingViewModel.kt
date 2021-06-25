package com.ht117.sandsara.ui.settings

import androidx.lifecycle.ViewModel
import com.ht117.sandsara.ui.base.IModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingViewModel: ViewModel(), IModel<SettingState, SettingAction> {

    override val actions = MutableSharedFlow<SettingAction>()

    private val _state = MutableStateFlow<SettingState>(SettingState.Initialize)
    override val state: StateFlow<SettingState> = _state.asStateFlow()

}