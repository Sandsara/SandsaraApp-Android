package com.ht117.sandsara.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Thumbnail(val url: String,
                 val width: Int,
                 val height: Int): Parcelable

@Serializable
@Parcelize
data class Thumbnails(val small: Thumbnail,
                      val large: Thumbnail,
                      val full: Thumbnail): Parcelable

@Serializable
@Parcelize
data class Image(val id: String = "",
                 val url: String,
                 val filename: String = "",
                 val size: Long = 0L,
                 val type: String = "",
                 val thumbnails: Thumbnails? = null): Parcelable

@Serializable
@Parcelize
data class SandFile(val id: String = "",
                    val url: String,
                    val filename: String,
                    val size: Long = 0L,
                    val type: String = ""): Parcelable

@Serializable
data class Palette(val red: String,
                   val green: String,
                   val blue: String,
                   val position: String)

@Serializable
data class PaletteResponse(val id: String,
                           @SerialName("fields") val palettes: Palette)

@Serializable
data class BasePaletteResponse(val records: List<PaletteResponse>)

fun Palette.toGradient(): Gradient {
    val reds = red.split(",").map { it.toInt() }
    val greens = green.split(",").map { it.toInt() }
    val blues = blue.split(",").map { it.toInt() }
    val poss = position.split(",").map { it.toInt() }

    if (reds.size != greens.size || blues.size != poss.size || greens.size != blues.size) {
        throw RuntimeException("Invalid palette")
    }

    val hsls = mutableListOf<HslColor>()
    reds.forEachIndexed { index, red ->
        val hsl = HslColor(pos = poss[index], red = red, green = greens[index], blue = blues[index])
        hsls.add(hsl)
    }

    return Gradient(hslColors = hsls)
}