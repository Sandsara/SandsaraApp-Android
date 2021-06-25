package com.ht117.sandsara.ui.settings.lightmode.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.repo.IDeviceRepo
import com.ht117.sandsara.ui.base.IModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CycleViewModel(private val deviceRepo: IDeviceRepo): ViewModel(), IModel<CycleState, CycleAction> {

    override val actions = MutableSharedFlow<CycleAction>()
    private val _state = MutableStateFlow(CycleState())
    override val state: StateFlow<CycleState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                when (it) {
                    CycleAction.LoadPalette -> {
                        _state.value = _state.value.copy(palettes = deviceRepo.loadPalettes(),
                            selectedPalette = deviceRepo.loadCyclePalette())
                    }
                }
            }
        }
    }
}