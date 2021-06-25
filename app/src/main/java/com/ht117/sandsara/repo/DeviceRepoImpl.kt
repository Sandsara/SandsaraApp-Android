package com.ht117.sandsara.repo

import android.content.Context
import com.ht117.sandsara.data.NetworkService
import com.ht117.sandsara.data.SandSaraDb
import com.ht117.sandsara.ext.Prefs
import com.ht117.sandsara.ext.read
import com.ht117.sandsara.ext.write
import com.ht117.sandsara.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File

class DeviceRepoImpl(private val context: Context,
                     private val client: HttpClient,
                     private val db: SandSaraDb): IDeviceRepo {

    private val paletteDao by lazy { db.paletteDao() }

    override suspend fun loadPalettes(): List<Palette> = withContext(Dispatchers.IO) {
        try {
            val palettes = paletteDao.getAll()
            if (palettes.isEmpty()) {
                val response = client.get<BasePaletteResponse> {
                    url { encodedPath = "colorPalette" }
                }
                paletteDao.addItems(*response.records.map { it.toEntity() }.toTypedArray())
                return@withContext response.records.map { it.palettes }
            }
            return@withContext palettes.map { it.toModel() }
        } catch (exp: Exception) {
            Timber.d("Failed to load palette ${exp.message}")
            exp.printStackTrace()
            throw exp
        }
    }

    override suspend fun loadCyclePalette(): Gradient? {
        val json = context.read(Prefs.CyclePalette, "")
        return try {
            if (json.isNullOrEmpty()) {
                null
            } else {
                Json.decodeFromString<Gradient>(json)
            }
        } catch (exp: Exception) {
            Timber.d("Failed to load cycle palette")
            null
        }
    }

    override suspend fun loadFirmware() {
        withContext(Dispatchers.IO) {
            try {
                val response = client.get<BaseFirmwareResponse> {
                    url { encodedPath = "firmware" }
                }
                val firmware = response.records.first().fields
                context.write(Prefs.NewFirmware, Json.encodeToString(firmware))
            } catch (exp: Exception) {
                Timber.d("Failed to load firmware ${exp.message}")
            }
        }
    }

    override suspend fun downloadFirmware(file: File, firmwareModel: FirmwareModel) = flow {
        try {
            val rawClient = NetworkService.customClient()
            val fos = file.outputStream()

            emit(0)
            val sandFile = firmwareModel.file.first()

            val bytes = rawClient.get<ByteReadChannel>(sandFile.url)
            val bufSize = 4096
            val buffer = ByteArray(bufSize)
            var total = 0

            while (true) {
                val result = bytes.readAvailable(buffer, 0, bufSize)
                if (result == -1) {
                    break
                }
                total += result
                val progress = (total * 100 / sandFile.size).toInt()
                if (progress % 5 == 0) {
                    emit(progress)
                }
                fos.write(buffer, 0, result)
            }
            fos.close()
            emit(100)
        } catch (exp: Exception) {
            emit(-1)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

}