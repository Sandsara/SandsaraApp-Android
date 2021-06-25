package com.ht117.sandsara.model

import android.os.Parcelable
import androidx.annotation.IntRange
import kotlinx.parcelize.Parcelize

typealias Status = Int
typealias ColorMode = Int // 0 -> Cycle, 1 -> Static
typealias StaticMode = Int // 0 -> Temp, 1 -> Custom
typealias StripDirection = Int

const val CyclePalette: ColorMode = 0
const val StaticPalette: ColorMode = 1

const val TempColor: StaticMode = 0
const val CustomColor: StaticMode = 1

//@Parcelize
//data class SettingConfig(val macAddress: String,
//                         val name: String = "Unknown",
//                         val version: String = "Unknown",
//                         val stripSpeed: Int = 1,
//                         val brightness: Int = 0,
//                         val ballSpeed: Int = 1,
//                         val cycleMode: Int = 0,
//                         val paletteJson: String = "",
//                         @IntRange(from = 0, to = 1) val colorMode: ColorMode = CyclePalette,
//                         val staticMode: StaticMode = TempColor,
//                         @IntRange(from = 1, to = 5) val status: Status,
//                         val flipDirection: StripDirection): Parcelable

data class GeneralConfig(val ballSpeed: Int = 1,
                         val brightness: Int = 0,
                         val status: Int = 1,
                         val paletteJson: String)

data class CycleConfig(val flipDirection: StripDirection, val stripSpeed: Int = 1, val cycleMode: Int = 0)