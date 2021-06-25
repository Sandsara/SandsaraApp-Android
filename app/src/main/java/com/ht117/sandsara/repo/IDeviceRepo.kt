package com.ht117.sandsara.repo

import com.ht117.sandsara.model.FirmwareModel
import com.ht117.sandsara.model.Gradient
import com.ht117.sandsara.model.Palette
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IDeviceRepo {

    suspend fun loadPalettes(): List<Palette>

    suspend fun loadCyclePalette(): Gradient?

    suspend fun loadFirmware()

    suspend fun downloadFirmware(file: File, firmwareModel: FirmwareModel): Flow<Int>
}