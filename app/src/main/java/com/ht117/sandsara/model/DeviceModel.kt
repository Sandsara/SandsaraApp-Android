package com.ht117.sandsara.model

data class BleDevice(val name: String, val address: String, val rssi: Short = Short.MIN_VALUE)

fun BleDevice.calculateDistance(): Double {
    return Math.pow(10.0, (-69.0) - rssi) / Math.pow(10.0, 3.0)
}

const val DELAY_TIME = 500L
const val RESPONSE_TIME = 1_000L
const val MTU_SIZE = 503

const val State_Play = "played"
const val State_Pause = "paused"

typealias PaletteMode = Int
const val CycleMode: PaletteMode = 0
const val TempMode: PaletteMode = 1
const val CustomMode: PaletteMode = 2

sealed class State {
    object Disconnect: State()
    object Calibrate: State()
    object ConnectFailed: State()
    object Connected: State()
    object Paused: State()
    object Sleep: State()
    object Busy : State()
    object Playing: State()
}
data class DeviceStatus(val state: State = State.Disconnect,
                        val pos: Int = 1,
                        val progress: Int = -1)

data class ProgressStatus(val progress: Int = 0)
