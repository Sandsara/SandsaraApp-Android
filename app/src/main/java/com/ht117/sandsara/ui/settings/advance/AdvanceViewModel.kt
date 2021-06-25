package com.ht117.sandsara.ui.settings.advance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ht117.sandsara.ext.FirmwarePath
import com.ht117.sandsara.ext.createTemp
import com.ht117.sandsara.model.FirmwareModel
import com.ht117.sandsara.repo.IDeviceRepo
import com.ht117.sandsara.ui.base.IModel
import com.ht117.sandsara.ui.base.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class AdvanceViewModel(private val context: Context,
                       private val deviceRepo: IDeviceRepo): ViewModel(), IModel<AdvanceState, AdvanceAction> {

    override val actions = MutableSharedFlow<AdvanceAction>()
    private val _state = MutableStateFlow(AdvanceState())
    override val state: StateFlow<AdvanceState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actions.collect {
                handleDownload(it.firmware)
            }
        }
    }

    private suspend fun handleDownload(firmwareModel: FirmwareModel) {
        try {
            val sandFile = firmwareModel.file.first()
            val file = context.createTemp(sandFile.filename, FirmwarePath)
            _state.value = _state.value.copy(
                isDownloading = true,
                progress = Resource.Stream(deviceRepo.downloadFirmware(file, firmwareModel))
            )
        } catch (exp: Exception) {
            Timber.d("Failed ${exp.message}")
        }
    }
}