package com.ht117.sandsara

/**
 * Ble services
 * Defines constants config of Ble devices
 */
object BleServices {

    const val WEB_URL = "http://www.sandsara.io"
    const val ClientConfig = "00002902-0000-1000-8000-00805f9b34fb"

    object GeneralConfig {
        const val Id = "fd31a840-22e7-11eb-adc1-0242ac120002"
        const val FirmwareVersion = "7b204278-30c3-11eb-adc1-0242ac120002"
        const val NameOfSandSara = "7b204548-30c3-11eb-adc1-0242ac120002"
        const val Status = "7b204660-30c3-11eb-adc1-0242ac120002"
        const val Pause = "7b20473c-30c3-11eb-adc1-0242ac120002"
        const val Play = "7b20480e-30c3-11eb-adc1-0242ac120002"
        const val Sleep = "7b204a3e-30c3-11eb-adc1-0242ac120002"
        const val BallSpeed = "7b204b10-30c3-11eb-adc1-0242ac120002"
        const val Restart = "7b204bce-30c3-11eb-adc1-0242ac120002"
        const val FactoryReset = "7b204c8c-30c3-11eb-adc1-0242ac120002"
        const val MessageError = "9b12aa02-2c6e-11eb-adc1-0242ac120002"
    }

    object FileConfig {
        const val Id = "fd31abc4-22e7-11eb-adc1-0242ac120002"
        const val SendFileFlag = "fcbff68e-2af1-11eb-adc1-0242ac120002"
        const val SendBytes = "fcbffa44-2af1-11eb-adc1-0242ac120002"
        const val Existed = "fcbffb52-2af1-11eb-adc1-0242ac120002"
        const val Delete = "fcbffc24-2af1-11eb-adc1-0242ac120002"
        const val Response = "fcbffce2-2af1-11eb-adc1-0242ac120002"
        const val ReadFileFlag = "fcbffdaa-2af1-11eb-adc1-0242ac120002"
        const val ReadFiles = "fcbffe72-2af1-11eb-adc1-0242ac120002"
    }

    object PlaylistConfig {
        const val Id = "fd31a778-22e7-11eb-adc1-0242ac120002"
        const val Name = "9b12a048-2c6e-11eb-adc1-0242ac120002"
        const val AmountOfPath = "9b12a26e-2c6e-11eb-adc1-0242ac120002"
        const val PathName = "9b12a534-2c6e-11eb-adc1-0242ac120002"
        const val PathPosition = "9b12a62e-2c6e-11eb-adc1-0242ac120002"
        const val AddPath = "9b12a7be-2c6e-11eb-adc1-0242ac120002"
        const val CreatePlaylistOfAllPath = "9b12a886-2c6e-11eb-adc1-0242ac120002"
        const val ProgressPath = "9b12a944-2c6e-11eb-adc1-0242ac120002"
        const val MessageError = "9b12aa02-2c6e-11eb-adc1-0242ac120002"
    }

    object LedStripConfig {
        const val Id = "fd31a2be-22e7-11eb-adc1-0242ac120002"

        const val StripSpeed = "1a9a7b7e-2305-11eb-adc1-0242ac120002"
        const val CycleMode =  "1a9a7dea-2305-11eb-adc1-0242ac120002"
        const val StripDirection = "1a9a8042-2305-11eb-adc1-0242ac120002"
        const val SelectedPalette = "1a9a813c-2305-11eb-adc1-0242ac120002"
        const val Brightness = "1a9a8948-2305-11eb-adc1-0242ac120002"
        const val UploadCustomPalette = "1a9a87b8-2305-11eb-adc1-0242ac120002"

        const val AmountColor = "1a9a820e-2305-11eb-adc1-0242ac120002"
        const val Positions = "1a9a82d6-2305-11eb-adc1-0242ac120002"
        const val Reds = "1a9a83a8-2305-11eb-adc1-0242ac120002"
        const val Greens = "1a9a8466-2305-11eb-adc1-0242ac120002"
        const val Blues = "1a9a852e-2305-11eb-adc1-0242ac120002"

        const val Error = "1a9a8880-2305-11eb-adc1-0242ac120002"
    }
}