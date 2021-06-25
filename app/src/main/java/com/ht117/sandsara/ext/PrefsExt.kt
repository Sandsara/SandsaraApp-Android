package com.ht117.sandsara.ext

import android.content.Context
import androidx.core.content.edit
import com.ht117.sandsara.model.*
import kotlin.reflect.KClass

object Prefs {
    const val ColorMode = "color_mode"
    const val StaticMode = "static_mode"
    const val CustomColor = "custom_color"


    const val Dictionary = "Sandsara"
    const val IsFirst = "first_time"

    const val MacAddress = "mac_address"
    const val Device = "name"
    const val Version = "version"
    const val StripSpeed = "strip_speed"
    const val BallSpeed = "ball_speed"
    const val StripDirection = "strip_direction"
    const val Brightness = "brightness"
    const val CycleMode = "cycle_mode"
    const val SelectedPalette = "selected_palette"
    const val PaletteMode = "palette_mode"
    const val CyclePalette = "cycle_palette"
    const val TempPalette = "temp_palette"
    const val CustomPalette = "custom_palette"
    const val LightCycleSpeed = "light_cycle_speed"
    const val Status = "status"

    const val NewFirmware = "latest_version"
    const val LastSync = "last_sync"

}

fun Context.isFirstTime(): Boolean {
    val value = read(Prefs.IsFirst, true)
    if (value) write(Prefs.IsFirst, false)

    return value
}

inline fun <reified T> Context.write(key: String, value: T) {
    val prefs = getSharedPreferences(Prefs.Dictionary, Context.MODE_PRIVATE)
    when (value) {
        is String -> { prefs.edit { putString(key, value) } }
        is Long -> { prefs.edit { putLong(key, value) } }
        is Int -> { prefs.edit { putInt(key, value) } }
        is Float -> { prefs.edit { putFloat(key, value) } }
        is Boolean -> { prefs.edit { putBoolean(key, value) } }
    }
}

inline fun <reified T> Context.read(key: String, defValue: T): T {
    val prefs = getSharedPreferences(Prefs.Dictionary, Context.MODE_PRIVATE)
    return when (defValue) {
        is String -> prefs.getString(key, defValue as String)?: defValue
        is Long -> prefs.getLong(key, defValue as Long)
        is Int -> prefs.getInt(key, defValue as Int)
        is Boolean -> prefs.getBoolean(key, defValue as Boolean)
        else -> throw Exception("Format exception")
    } as T
}

fun Context.getGeneralConfig(): GeneralConfig {
    val ballSpeed = read(Prefs.BallSpeed, "0")
    val status = read(Prefs.Status, "1")
    val brightness = read(Prefs.Brightness, "0")
    val json = read(Prefs.SelectedPalette, "")

    return GeneralConfig(
        ballSpeed = ballSpeed.toInt(),
        status = status.toInt(),
        brightness = brightness.toInt(),
        paletteJson = json
    )
}

fun Context.getCycleConfig(): CycleConfig {
    val direction = read(Prefs.StripDirection, "0")
    val stripSpeed = read(Prefs.StripSpeed, "0")
    val cycleMode = read(Prefs.CycleMode, "0")

    return CycleConfig(flipDirection = direction.toInt(), stripSpeed = stripSpeed.toInt(), cycleMode = cycleMode.toInt())
}

//fun Context.getConfig(): SettingConfig {
//    val address = read(Prefs.MacAddress, "")
//    val name = read(Prefs.Device, "Unknown")
//    val version = read(Prefs.Version, "Unknown")
//    val stripSpeed= read(Prefs.StripSpeed, "0")
//    val brightness = read(Prefs.Brightness, "0")
//
//    val ballSpeed = read(Prefs.BallSpeed, "0")
//    val cycleMode = read(Prefs.CycleMode, "0")
//    val status = read(Prefs.Status, "1")
//    val direction = read(Prefs.StripDirection, "0")
//
//    val colorMode = read(Prefs.ColorMode, CyclePalette)
//    val staticMode = read(Prefs.StaticMode, TempColor)
//
//    return SettingConfig(
//        macAddress = address,
//        name = name,
//        version =  version,
//        stripSpeed =  stripSpeed.toInt(),
//        brightness = brightness.toInt(),
//        cycleMode = cycleMode.toInt(),
//        status = status.toInt(),
//        colorMode = colorMode,
//        staticMode = staticMode,
//        flipDirection = direction.toInt(),
//        ballSpeed = ballSpeed.toInt()
//    )
//}