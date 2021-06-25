package com.ht117.sandsara

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.model.*
import com.ht117.sandsara.repo.ITrackRepo
import com.juul.able.android.connectGatt
import com.juul.able.device.ConnectGattResult
import com.juul.able.gatt.Gatt
import com.juul.able.gatt.OnCharacteristicRead
import com.juul.able.gatt.OnCharacteristicWrite
import com.juul.able.gatt.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Ble manager
 * Handle interaction with Ble device
 */
object BleManager : KoinComponent {

    private val appContext: Context by inject()
    private var gatt: Gatt? = null

    var isSyncing = false
    var isFirstTime = true
    var loadPalette = false
    private val trackRepo: ITrackRepo by inject()
    val playingTracks = mutableListOf<Track>()
    var playlistName: String = ""

    val generalConfigFlow by lazy { MutableStateFlow(appContext.getGeneralConfig()) }
    val cycleConfigFlow by lazy { MutableStateFlow(appContext.getCycleConfig()) }
    val localStatus = MutableStateFlow(DeviceStatus())

    /**
     * Flow of device status and progress
     */
    val deviceStateFlow by lazy {
        gatt!!.onCharacteristicChanged.map {
            val progressField = getField(BleServices.PlaylistConfig.Id, BleServices.PlaylistConfig.ProgressPath)

            if (it.characteristic == progressField) {
                val progress = it.value.toString(Charsets.US_ASCII).convertToInt(0)
                ProgressStatus(progress)
            } else {
                it.value.toString(Charsets.US_ASCII).convertToInt(-1)
            }
        }
    }

    private var updateMtu = false
    private var adapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner = adapter?.bluetoothLeScanner
    private var leCallback: ScanCallback? = null

    /**
     * Scan devices
     * Scanning devices then return flow of device @see#BleDevice
     */
    suspend fun scanDevices() = callbackFlow<BleDevice> {
        try {
            leCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    result?.run {
                        offer(
                            BleDevice(
                                this.device.name ?: "Unknown",
                                this.device.address,
                                this.rssi.toShort()
                            )
                        )
                    }
                }
            }

            adapter.startDiscovery()
            scanner?.startScan(leCallback)

        } catch (exp: Exception) {
            Timber.d("Failed scan")
        } finally {
            awaitClose { stopScan() }
        }
    }

    /**
     * Stop scanning devices
     */
    fun stopScan() {
        adapter.cancelDiscovery()
        scanner?.stopScan(leCallback)
        leCallback = null
    }

    /**
     * Connect: connect to device with a mac address
     * @param address
     * @return
     */
    suspend fun connect(address: String): Boolean {
        try {
            stopScan()
            val bleMan = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val device = bleMan.adapter.getRemoteDevice(address)
            val result = device.connectGatt(appContext)
            return if (result is ConnectGattResult.Success) {
                gatt = result.gatt

                updateMtu = gatt?.requestMtu(MTU_SIZE)?.isSuccess == true
                if (gatt?.discoverServices() == GATT_SUCCESS) {
                    if (notifyField(BleServices.PlaylistConfig.Id, BleServices.PlaylistConfig.ProgressPath,
                            BleServices.ClientConfig)) {
                        Timber.d("Notify progress success")
                    } else {
                        Timber.d("Notify progress failed")
                    }

                    if (notifyField(BleServices.GeneralConfig.Id, BleServices.GeneralConfig.Status,
                            BleServices.ClientConfig)) {
                        Timber.d("Notify status success")
                    } else {
                        Timber.d("Notify status failed")
                    }

                    localStatus.value = localStatus.value.copy(state = State.Connected)

                    getDeviceStatus()
                    readConfigs(appContext)
                    true
                } else {
                    false
                }
            } else {
                localStatus.value = localStatus.value.copy(state = State.ConnectFailed)
                false
            }
        } catch (exp: Exception) {
            localStatus.value = localStatus.value.copy(state = State.ConnectFailed)
            return false
        }
    }

    /**
     * Disconnect with current ble device
     */
    suspend fun disconnect() {
        gatt?.disconnect()
        gatt = null
        isFirstTime = true
        localStatus.value = localStatus.value.copy(state = State.Disconnect)
    }

    /**
     * Read characteristic value from ble device
     *
     * @param service
     * @param fieldId
     * @return
     */
    private suspend fun readField(service: String, fieldId: String): OnCharacteristicRead? {
        try {
            val field = getField(service, fieldId)
            if (field != null) {
                return gatt?.readCharacteristic(field)
            }
            return null
        } catch (exp: Exception) {
            Timber.d("Read field $fieldId failed ${exp.message}")
            return null
        }
    }

    /**
     * Write characteristic value of ble
     *
     * @param service
     * @param fieldId
     * @param data
     * @param type
     * @return
     */
    private suspend fun writeField(
        service: String,
        fieldId: String,
        data: ByteArray,
        type: Int = WRITE_TYPE_DEFAULT
    ): OnCharacteristicWrite? {
        try {
            val field = getField(service, fieldId)
            if (field != null) {
                return gatt?.writeCharacteristic(field, data, type)
            }
            return null
        } catch (exp: Exception) {
            Timber.d("Write field $fieldId failed ${exp.message}")
            return null
        }
    }

    /**
     * Setup notify field
     *
     * @param service
     * @param fieldId
     * @param descriptorId
     * @return
     */
    private suspend fun notifyField(service: String, fieldId: String, descriptorId: String): Boolean {
        val field = getField(service, fieldId)
        return if (field != null) {
            val result = gatt?.setCharacteristicNotification(field, true) ?: false
            if (result) {
                val descriptor = field.getDescriptor(UUID.fromString(descriptorId))
                gatt?.writeDescriptor(descriptor!!, ENABLE_NOTIFICATION_VALUE)?.isSuccess?: false
            } else {
                false
            }
        } else false
    }

    /**
     * Read configurations from ble device
     * @param context
     */
    private suspend fun readConfigs(context: Context) {
        try {
            val firmware = readField(BleServices.GeneralConfig.Id, BleServices.GeneralConfig.FirmwareVersion)
            if (firmware?.isSuccess == true) {
                context.write(Prefs.Version, firmware.value.toString(Charsets.US_ASCII))
            }

            val brightness = readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.Brightness)
            if (brightness?.isSuccess == true) {
                val value = brightness.value.toString(Charsets.US_ASCII)
                context.write(Prefs.Brightness, value)
                generalConfigFlow.value = generalConfigFlow.value.copy(brightness = value.convertToInt(0))
//                configFlow.value = configFlow.value.copy(brightness = value.convertToInt(0))
            }

            val stripSpeed = readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.StripSpeed)
            if (stripSpeed?.status == GATT_SUCCESS) {
                val value = stripSpeed.value.toString(Charsets.US_ASCII)

                context.write(Prefs.StripSpeed, value)
                cycleConfigFlow.value = cycleConfigFlow.value.copy(stripSpeed = value.convertToInt(0))
//                configFlow.value = configFlow.value.copy(stripSpeed = value.convertToInt(0))
            }

            val ballSpeed = readField(BleServices.GeneralConfig.Id, BleServices.GeneralConfig.BallSpeed)
            if (ballSpeed?.isSuccess == true) {
                val value = ballSpeed.value.toString(Charsets.US_ASCII)
                context.write(Prefs.BallSpeed, value)
                generalConfigFlow.value = generalConfigFlow.value.copy(ballSpeed = value.convertToInt(0))
            }

            val cycleMode = readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.CycleMode)
            if (cycleMode != null && cycleMode.status == GATT_SUCCESS) {
                val value = cycleMode.value.toString(Charsets.US_ASCII)
                context.write(Prefs.CycleMode, value)
                cycleConfigFlow.value = cycleConfigFlow.value.copy(cycleMode = value.convertToInt(0))
//                configFlow.value = configFlow.value.copy(cycleMode = value.convertToInt(0))
            }

            val flip = readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.StripDirection)
            if (flip?.isSuccess == true) {
                val value = flip.value.toString(Charsets.US_ASCII)
                context.write(Prefs.StripDirection, value)
                cycleConfigFlow.value = cycleConfigFlow.value.copy(flipDirection = value.convertToInt(0))
//                configFlow.value = configFlow.value.copy(flipDirection = value.convertToInt(0))
            }

            val amount =
                readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.AmountColor)
            val amountValue = if (amount?.isSuccess == true) {
                amount.value.toString(Charsets.US_ASCII).toInt()
            } else {
                0
            }

            val poss =
                readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.Positions)
            val posValue = if (poss?.isSuccess == true) {
                poss.value.toString(Charsets.US_ASCII).split(",")
            } else {
                emptyList()
            }

            val reds = readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.Reds)
            val redValues = if (reds?.isSuccess == true) {
                reds.value.toString(Charsets.US_ASCII).split(",")
            } else {
                emptyList()
            }

            val greens =
                readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.Greens)
            val greenValues = if (greens?.isSuccess == true) {
                greens.value.toString(Charsets.US_ASCII).split(",")
            } else {
                emptyList()
            }

            val blues =
                readField(BleServices.LedStripConfig.Id, BleServices.LedStripConfig.Blues)
            val blueValues = if (blues?.isSuccess == true) {
                blues.value.toString(Charsets.US_ASCII).split(",")
            } else {
                emptyList()
            }

            val hsls = (0 until amountValue).map {
                HslColor(
                    pos = posValue[it].convertToInt(),
                    red = redValues[it].convertToInt(),
                    green = greenValues[it].convertToInt(),
                    blue = blueValues[it].convertToInt()
                )
            }
            val gradient = Gradient(hslColors = hsls)
            context.write(Prefs.SelectedPalette, Json.encodeToString(gradient))
            generalConfigFlow.value = generalConfigFlow.value.copy(paletteJson = Json.encodeToString(gradient))
        } catch (exp: Exception) {
            Timber.d("Failed to read config from board ${exp.message}")
        }
    }

    /**
     * Get characteristic from ble
     *
     * @param service
     * @param field
     * @return
     */
    private fun getField(service: String, field: String): BluetoothGattCharacteristic? {
        return gatt?.getService(UUID.fromString(service))?.getCharacteristic(UUID.fromString(field))
    }

    /**
     * Update ball speed of ble
     * @param value
     */
    suspend fun updateBallSpeed(value: Int) {
        val curValue = appContext.read(Prefs.BallSpeed, "0").toInt()

        if (value != curValue) {
            val result = writeField(
                BleServices.GeneralConfig.Id,
                BleServices.GeneralConfig.BallSpeed,
                value.toByteArray()
            )
            if (checkResult(result)) {
                appContext.write(Prefs.BallSpeed, value.toString())
                generalConfigFlow.value = generalConfigFlow.value.copy(ballSpeed = value)
            }
        }
    }

    /**
     * Toggle sleep
     * @param value
     */
    suspend fun toggleSleep(value: Int) {
        writeField(
            BleServices.GeneralConfig.Id,
            BleServices.GeneralConfig.Sleep,
            value.toByteArray()
        )
    }

    /**
     * Check result whether write field successfully
     * @param result
     * @return
     */
    private fun checkResult(result: OnCharacteristicWrite?): Boolean {
        return result?.isSuccess == true
    }

    /**
     * Update brightness
     * @param value
     */
    suspend fun updateBrightness(value: Int) {
        val brightness = appContext.read(Prefs.Brightness, "0").toInt()

        if (value != brightness) {
            val result = writeField(
                BleServices.LedStripConfig.Id,
                BleServices.LedStripConfig.Brightness,
                value.toByteArray()
            )
            val isSuccess = checkResult(result)
            if (isSuccess) {
                appContext.write(Prefs.Brightness, value.toString())
                generalConfigFlow.value = generalConfigFlow.value.copy(brightness = value)
            }
        }
    }

    /**
     * Send palette
     * @param gradient @see #Gradient
     * @param mode @see #PaletteMode
     * @return
     */
    suspend fun sendPalette(gradient: Gradient) {
        val value = gradient.toBytes()

        val result = writeField(
            BleServices.LedStripConfig.Id,
            BleServices.LedStripConfig.UploadCustomPalette,
            value.toByteArray()
        )

        if (checkResult(result)) {
            val json = Json.encodeToString(gradient)
            appContext.write(Prefs.SelectedPalette, json)
        }
    }

    /**
     * Update flip direction
     * @param value
     */
    suspend fun updateFlipDirection(value: Int) {
        val result = writeField(
            BleServices.LedStripConfig.Id,
            BleServices.LedStripConfig.StripDirection,
            value.toByteArray()
        )
        if (checkResult(result)) {
            appContext.write(Prefs.StripDirection, value.toString())
        }
    }

    /**
     * Update cycle mode
     * @param value
     */
    suspend fun updateCycleMode(value: Int) {
        val result = writeField(
            BleServices.LedStripConfig.Id,
            BleServices.LedStripConfig.CycleMode,
            value.toByteArray()
        )
        if (checkResult(result)) {
            appContext.write(Prefs.CycleMode, value.toString())
        }
    }

    /**
     * Reset factory
     * @return
     */
    suspend fun resetFactory(): Boolean {
        val result = writeField(
            BleServices.GeneralConfig.Id,
            BleServices.GeneralConfig.FactoryReset,
            1.toByteArray()
        )
        return checkResult(result)
    }

    /**
     * Change ble device's name
     * @param value
     * @return
     */
    suspend fun changeName(value: String): Boolean {
        val result = writeField(
            BleServices.GeneralConfig.Id,
            BleServices.GeneralConfig.NameOfSandSara,
            value.toByteArray(Charsets.US_ASCII)
        )
        return checkResult(result)
    }

    /**
     * Get device status when it change
     * @param receiveStatus: -1 meant first time connect with ble
     */
    suspend fun getDeviceStatus(receiveStatus: Int = -1) {
        if (!isSyncing) {
            if (receiveStatus == -1) {
                val result = readField(BleServices.GeneralConfig.Id, BleServices.GeneralConfig.Status)

                try {
                    if (result?.isSuccess == true) {
                        val status = result.value.toString(Charsets.US_ASCII).toInt()
                        handleStatus(status)
                    }
                } catch (exp: Exception) {
                    Timber.d("Failed to get status ${exp.message}")
                }
            } else {
                handleStatus(receiveStatus)
            }
        }
    }

    /**
     * Handle device's status state
     * @param status
     */
    private suspend fun handleStatus(status: Int) {
        appContext.write(Prefs.Status, status.toString())
        when (status) {
            1 -> localStatus.value = localStatus.value.copy(state = State.Calibrate)
            2 -> localStatus.value = localStatus.value.copy(state = State.Playing)
            3 -> localStatus.value = localStatus.value.copy(state = State.Paused)
            4 -> localStatus.value = localStatus.value.copy(state = State.Sleep)
            5 -> localStatus.value = localStatus.value.copy(state = State.Busy)
        }
        if (status in 1..5) {
            generalConfigFlow.value = generalConfigFlow.value.copy(status = status)
        }
        if (status in listOf(2, 3) && isFirstTime) {
            isFirstTime = false
            getFullPlayerInfo()
        }
    }

    /**
     * Get full player info include playlist, position, progress
     */
    private suspend fun getFullPlayerInfo() {
        if (playlistName.isNullOrEmpty()) {
            val playingField = readField(BleServices.PlaylistConfig.Id, BleServices.PlaylistConfig.Name)
            if (playingField?.isSuccess == true) {
                playlistName = playingField.value.toString(Charsets.US_ASCII)
            }
        }

        if (!playlistName.isNullOrEmpty()) {
            val playlistFile = appContext.createTemp("${playlistName}.playlist", PlaylistPath)
            val playingPlaylist = readFile("${playlistName}.playlist", playlistFile)

            if (playingPlaylist) {
                val files = playlistFile.readLines(Charsets.US_ASCII)
                playingTracks.clear()
                playingTracks.addAll(files.map { trackRepo.getTrackFromFile(it) })
            }
        }

        if (!playingTracks.isNullOrEmpty()) {
            val playingPosField = readField(BleServices.PlaylistConfig.Id, BleServices.PlaylistConfig.PathPosition)
            val pos = if (playingPosField?.isSuccess == true) {
                playingPosField.value.toString(Charsets.US_ASCII).toInt()
            } else { 1 }

            val newPos = clamp(pos, 1, playingTracks.size)
            val progressField = readField(BleServices.PlaylistConfig.Id, BleServices.PlaylistConfig.ProgressPath)
            val progress = if (progressField?.isSuccess == true) {
                progressField.value.toString(Charsets.US_ASCII).toInt()
            } else {
                -1
            }
            localStatus.value = localStatus.value.copy(pos = newPos, progress = progress)
        }
    }

    /**
     * Check track exist in ble device
     * @param track
     * @return
     */
    suspend fun checkTrackExist(track: Track): Boolean {
        val file = track.sandFiles.firstOrNull()?: return false
        return checkFileInDevice(file.filename)
    }

    /**
     * Check file in device
     *
     * @param fullFileName
     * @return
     */
    suspend fun checkFileInDevice(fullFileName: String): Boolean {
        Timber.d("Checking $fullFileName existence")
        val checkFile = writeField(
            BleServices.FileConfig.Id,
            BleServices.FileConfig.Existed, fullFileName.toByteArray(Charsets.US_ASCII)
        )

        if (checkFile?.isSuccess == true) {
            val response = readField(BleServices.FileConfig.Id, BleServices.FileConfig.Response)

            return if (response?.isSuccess == true) {
                response.value.toString(Charsets.US_ASCII) == "1"
            } else {
                false
            }
        }
        return false
    }

    /**
     * Sync track to ble device
     * @param track
     * @return flow of @see #SyncTrack
     */
    suspend fun sendTrack(track: Track) = flow {
        val file = appContext.getFileWithName(track.sandFiles.first().filename, TrackPath)

        try {
            isSyncing = true
            emit(SyncTrack.Starting(track))

            writeField(BleServices.FileConfig.Id, BleServices.FileConfig.SendFileFlag, file!!.name.toByteArray(Charsets.US_ASCII))

            val input = file.readBytes()
            var read = 0
            while (read < input.size) {
                val length = if (read + MTU_SIZE - 3 >= input.size) {
                    input.size - read
                } else {
                    MTU_SIZE - 3
                }
                val chunk = input.copyOfRange(read, read + length)

                read += length

                val result = writeField(BleServices.FileConfig.Id, BleServices.FileConfig.SendBytes, chunk)
                if (result == null || !result.isSuccess) {
                    throw Exception("Sync failed at $read / ${input.size}")
                }
                emit(SyncTrack.Progress(track, read * 100 / input.size))
                Timber.d("Synced $read / ${input.size}")
            }
            writeField(BleServices.FileConfig.Id, BleServices.FileConfig.SendFileFlag,
                "ok".toByteArray(Charsets.US_ASCII)
            )
            isSyncing = false
            emit(SyncTrack.Completed(track))
        } catch (exp: Exception) {
            isSyncing = false
            file?.run { deleteFile(this) }
            emit(SyncTrack.Error("${exp.message}"))
        }
    }

    /**
     * Sync file with progress flow
     * @param file
     */
    suspend fun sendFileWithProgress(file: File) = flow {
        try {
            isSyncing = true
            emit(SyncFileProgress.Starting)

            writeField(
                BleServices.FileConfig.Id,
                BleServices.FileConfig.SendFileFlag,
                file.name.toByteArray(Charsets.US_ASCII)
            )

            val input = file.readBytes()
            var read = 0
            while (read < input.size) {

                val length = if (read + MTU_SIZE - 3 >= input.size) {
                    input.size - read
                } else {
                    MTU_SIZE - 3
                }
                val chunk = input.copyOfRange(read, read + length)

                read += length

                val result =
                    writeField(BleServices.FileConfig.Id, BleServices.FileConfig.SendBytes, chunk)
                if (result == null || !result.isSuccess) {
                    throw Exception("Sync failed at $read / ${input.size}")
                }
                emit(SyncFileProgress.Progress(read * 100 / input.size))
                Timber.d("Synced $read / ${input.size}")
            }

            writeField(
                BleServices.FileConfig.Id,
                BleServices.FileConfig.SendFileFlag,
                "ok".toByteArray(Charsets.US_ASCII)
            )
            isSyncing = false
            emit(SyncFileProgress.Finished)
        } catch (exp: Exception) {
            isSyncing = false
            deleteFile(file)
            emit(SyncFileProgress.Error)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Sync file without flow
     *
     * @param file
     * @return
     */
    suspend fun sendFile(file: File): Boolean {
        Timber.d("Sending file ${file.name}")
        isSyncing = true
        try {
            val result = writeField(
                BleServices.FileConfig.Id,
                BleServices.FileConfig.SendFileFlag,
                file.name.toByteArray(Charsets.US_ASCII)
            )
            if (!checkResult(result)) {
                isSyncing = false
                return false
            }

            val input = file.readBytes()
            var read = 0
            while (read < input.size) {
                val length = if (read + MTU_SIZE - 3 > input.size) {
                    input.size - read
                } else {
                    MTU_SIZE - 3
                }
                val chunk = input.copyOfRange(read, read + length)
                read += length

                val result =
                    writeField(BleServices.FileConfig.Id, BleServices.FileConfig.SendBytes, chunk)
                if (result == null || !result.isSuccess) {
                    throw Exception("Failed sync at $read / ${input.size}")
                }
                Timber.d("Write progress $read / ${input.size}")
            }
            val finished = writeField(
                BleServices.FileConfig.Id,
                BleServices.FileConfig.SendFileFlag,
                "ok".toByteArray(Charsets.US_ASCII)
            )

            isSyncing = false
            return if (finished?.isSuccess == true) {
                true
            } else {
                deleteFile(file)
                false
            }
        } catch (exp: Exception) {
            isSyncing = false
            deleteFile(file)
            Timber.d("Failed to sent file ${exp.message}")
            return false
        }
        return false
    }

    /**
     * Read file
     *
     * @param fileName
     * @param target
     * @return
     */
    private suspend fun readFile(fileName: String, target: File): Boolean {
        Timber.d("Reading file $fileName")
        val readFlag = writeField(
            BleServices.FileConfig.Id,
            BleServices.FileConfig.ReadFileFlag,
            fileName.toByteArray(Charsets.US_ASCII)
        )

        if (readFlag?.isSuccess == true) {
            val isReadable = readField(BleServices.FileConfig.Id, BleServices.FileConfig.Response)
            if (isReadable?.isSuccess == true) {
                when (val value = isReadable.value.toString(Charsets.US_ASCII)) {
                    "ok" -> {
                        val fos = target.outputStream()
                        while (true) {
                            val bytes = readField(
                                BleServices.FileConfig.Id,
                                BleServices.FileConfig.ReadFiles
                            )

                            if (bytes?.isSuccess == true) {
                                fos.write(bytes.value, 0, bytes.value.size)
                                if (bytes.value.size < 512) {
                                    break
                                }
                            } else {
                                break
                            }
                        }
                        fos.close()
                        return true
                    }
                    else -> {
                        Timber.d("Error reading file code $value")
                    }
                }
            } else {
                Timber.d("Failed to read file")
            }
        }
        return false
    }

    /**
     * Pause device
     */
    suspend fun pauseDevice() {
        val result = writeField(
            BleServices.GeneralConfig.Id,
            BleServices.GeneralConfig.Pause,
            1.toByteArray()
        )
        val writeResult = checkResult(result)

        if (writeResult) {
            localStatus.value = localStatus.value.copy(state = State.Paused)
        }
    }

    /**
     * Play or resume
     */
    suspend fun playOrResume() {
        val result = writeField(
            BleServices.GeneralConfig.Id,
            BleServices.GeneralConfig.Play,
            1.toByteArray()
        )

        val writeResult = checkResult(result)
        if (writeResult) {
            localStatus.value = localStatus.value.copy(state = State.Playing)
        }
    }

    /**
     * Has connection
     * Check if app has connection with device
     * @return
     */
    fun hasConnection(): Boolean {
        return gatt != null
    }

    /**
     * Update playlist
     * Change playlist
     * @param name
     * @return
     */
    private suspend fun updatePlaylist(name: String): Boolean {
        val result = writeField(
            BleServices.PlaylistConfig.Id, BleServices.PlaylistConfig.Name,
            name.toByteArray(Charsets.US_ASCII)
        )
        return checkResult(result)
    }

    /**
     * Play playlist
     *
     * @param nameWithoutExt
     * @param allTracks
     * @param isReplace
     * @return
     */
    suspend fun playPlaylist(nameWithoutExt: String, allTracks: List<Track>, isReplace: Boolean = false): Boolean {

        val playlistFile = allTracks.createFile(nameWithoutExt, appContext)

        //deleteFile(playlistFile)

        if (sendFile(playlistFile) && updatePlaylist(nameWithoutExt)) {
            if (isReplace) {
                playlistName = nameWithoutExt
                playingTracks.clear()
                playingTracks.addAll(allTracks)
                localStatus.value = localStatus.value.copy(
                    state = State.Playing,
                    pos = 1
                )
            }
            return true
        }
        return false
    }

    /**
     * Play position
     * Change play position
     * @param pos
     */
    suspend fun playPosition(pos: Int) {
        Timber.d("Play pos $pos")
        val newPos = writeField(
            BleServices.PlaylistConfig.Id,
            BleServices.PlaylistConfig.PathPosition,
            pos.toByteArray()
        )
        if (newPos?.isSuccess == true) {
            localStatus.value = localStatus.value.copy(pos = pos)
        }
    }

    /**
     * Add path
     * Add track into playlist
     * @param tracks
     * @return
     */
    suspend fun addPath(tracks: List<Track>): Boolean {
        return try {
            val tempo = mutableListOf<Track>()
            tempo.addAll(playingTracks)
            tempo.addAll(tracks)

            val file = tempo.createFile(playlistName, appContext)

            if (sendFile(file) /*&& updatePlaylist()*/) {
                playingTracks.clear()
                playingTracks.addAll(tempo)
                true
            } else {
                false
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
            Timber.d("Failed to add path ${exp.message}")
            false
        }
    }

    /**
     * Restart device
     *
     * @return
     */
    suspend fun restart(): Boolean {
        val response = writeField(
            BleServices.GeneralConfig.Id,
            BleServices.GeneralConfig.Restart,
            "1".toByteArray(Charsets.US_ASCII)
        )
        if (response?.isSuccess == true) {
            return response.characteristic.value.toString(Charsets.US_ASCII) == "ok"
        }
        return false
    }

    /**
     * Delete file in ble device
     *
     * @param file
     * @return
     */
    suspend fun deleteFile(file: File): Boolean {
        val result = writeField(
            BleServices.FileConfig.Id,
            BleServices.FileConfig.Delete,
            file.name.toByteArray(Charsets.US_ASCII)
        )
        if (checkResult(result)) {
            val response = readField(BleServices.FileConfig.Id, BleServices.FileConfig.Response)
            if (response?.isSuccess == true) {
                val value = response.value.toString(Charsets.US_ASCII)
                Timber.d("Deleting file result $value")
                return value == "1"
            }
        }
        return false
    }

    /**
     * Update light speed
     *
     * @param value
     */
    suspend fun updateLightSpeed(value: Int) {
        val speed = appContext.read(Prefs.StripSpeed, "0").toInt()

        if (speed != value) {
            val result = writeField(
                BleServices.LedStripConfig.Id,
                BleServices.LedStripConfig.StripSpeed,
                value.toByteArray()
            )
            val isSuccess = checkResult(result)
            if (isSuccess) {
                appContext.write(Prefs.StripSpeed, value.toString())
            }
        }
    }

    /**
     * Play track
     *
     * @param track
     * @return
     */
    suspend fun playTrack(track: Track): Boolean {
        if (playingTracks.isEmpty()) {
            return playPlaylist("temporary", listOf(track), true)
        } else {
            if (addPath(listOf(track))) {
                playPosition(playingTracks.size)
                return true
            }
        }
        return false
    }

    /**
     * Get un sync files of tracks
     *
     * @param tracks
     * @return
     */
    suspend fun getUnSyncFiles(tracks: List<Track>): List<Track> {
        return tracks.filterNot { checkTrackExist(it) }
    }

    /**
     * Set color mode
     *
     * @param mode
     */
    fun setColorMode(mode: ColorMode) {
        appContext.write(Prefs.ColorMode, mode)
    }

    /**
     * Set static mode
     *
     * @param mode
     */
    fun setStaticMode(mode: StaticMode) {
        appContext.write(Prefs.StaticMode, mode)
    }
}

