package com.ht117.sandsara.ext

import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.google.android.material.slider.Slider
import com.ht117.sandsara.view.HSLColorView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import timber.log.Timber

fun View.show() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun View.hide() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

@ColorInt
fun lightColor(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) value: Float): Int {
    val hsv = FloatArray(3)
    Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv)
    hsv[2] = value
    return Color.HSVToColor(hsv)
}

fun PointF.toSquareF(size: Float): RectF {
    return RectF(x, y, x + size, y + size)
}

data class SliderDebounce(val id: Int, val value: Int)
fun Slider.throttle(time: Long = 0) = callbackFlow {
    try {
        addOnChangeListener { slider, value, _ ->
            offer(SliderDebounce(slider.id, value.toInt()))
        }
    } finally {
        awaitClose { stop() }
    }
}.distinctUntilChanged().sample(time)

fun Slider.stop() {}

fun HSLColorView.throttle(time: Long) = callbackFlow<codes.side.andcolorpicker.model.Color> {
    try {
        addOnColorChangeListener(object : HSLColorView.IColorChangeListener {
            override fun onSelectedColor(color: codes.side.andcolorpicker.model.Color) {
                offer(color)
            }
        })
    } finally {
        awaitClose { stop() }
    }
}.distinctUntilChanged().sample(time)
fun HSLColorView.stop() {}

fun Int.toRGB(): RGB {
    return RGB(Color.red(this), Color.green(this), Color.blue(this))
}

fun RGB.distance(other: RGB): Double {
    return Math.sqrt(Math.pow(((red - other.red).toDouble()), 2.0) +
            Math.pow(((green - other.green).toDouble()), 2.0) +
            Math.pow(((blue - other.blue).toDouble()), 2.0))
}

data class RGB(val red: Int, val green: Int, val blue: Int)