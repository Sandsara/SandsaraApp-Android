package com.ht117.sandsara.model

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import androidx.annotation.IntRange
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Gradient(val direction: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT,
                    val hslColors: List<HslColor>): Parcelable

fun Gradient.isStatic(): Boolean {
    if (hslColors.size == 2) {
        return hslColors[0] equal hslColors[1]
    }
    return false
}

@Serializable
@Parcelize
data class HslColor(@IntRange(from = 0, to = 255) val pos: Int, val red: Int, val green: Int, val blue: Int): Parcelable

infix fun HslColor.equal(other: HslColor): Boolean {
    return red == other.red && green == other.green && blue == other.blue
}

fun HslColor.toColor(): Int {
    return Color.rgb(red, green, blue)
}

fun HslColor.toRelativePos(width: Float): Float {
    return pos / 255F * width
}

fun Gradient.toBytes(): UByteArray {
    val bytes = mutableListOf<UByte>()
    if (hslColors.size == 1) {
        bytes.add(2.toUByte())
    } else {
        bytes.add(hslColors.size.toUByte())
    }
    // Position
    if (hslColors.size == 1) {
        bytes.add(0.toUByte())
        bytes.add(255.toUByte())
    } else {
        hslColors.forEach {
            bytes.add(it.pos.toUByte())
        }
    }

    // Red
    if (hslColors.size == 1) {
        bytes.add(hslColors.first().red.toUByte())
        bytes.add(hslColors.first().red.toUByte())
    } else {
        hslColors.forEach {
            bytes.add(it.red.toUByte())
        }
    }

    // Green
    if (hslColors.size == 1) {
        bytes.add(hslColors.first().green.toUByte())
        bytes.add(hslColors.first().green.toUByte())
    } else {
        hslColors.forEach {
            bytes.add(it.green.toUByte())
        }
    }

    // Blue
    if (hslColors.size == 1) {
        bytes.add(hslColors.first().blue.toUByte())
        bytes.add(hslColors.first().blue.toUByte())
    } else {
        hslColors.forEach {
            bytes.add(it.blue.toUByte())
        }
    }

    return bytes.toUByteArray()
}

fun Gradient.toColors(): IntArray {

    return if (hslColors.size == 1) {
        val colors = IntArray(2)
        val hsl = hslColors.first().toColor()
        colors[0] = hsl
        colors[1] = hsl
        colors
    } else {
        val colors = IntArray(hslColors.size)
        hslColors.forEachIndexed { index, hsl ->
            colors[index] = hsl.toColor()
        }
        colors
    }
}

fun Gradient.toLinearGradient(width: Float): LinearGradient {
    return if (hslColors.size == 1) {
        val poss = floatArrayOf(0F, 1F)
        LinearGradient(0f, 0f, width, 0f, toColors(), poss, Shader.TileMode.CLAMP)
    } else {
        val poss = hslColors.map { it.pos / 255F }.toFloatArray()

        LinearGradient(0f, 0f, width, 0f, toColors(), poss, Shader.TileMode.CLAMP)
    }
}

fun Int.toGradient(): Gradient {
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)

    return Gradient(hslColors = listOf(HslColor(0, red, green, blue), HslColor(255, red, green, blue)))
}