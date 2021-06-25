package com.ht117.sandsara

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ht117.sandsara.ext.combine
import com.ht117.sandsara.ext.crop
import com.ht117.sandsara.model.Gradient
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.model.toColor
import timber.log.Timber
import java.net.URLEncoder
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Calculate temp color
 *
 * @param kevin
 * @return color from kevin temperature
 */
fun calculateTempColor(kevin: Int): Int {
    val temp = kevin / 100.0
    var red: Double
    var green: Double
    var blue: Double
    if (temp <= 66) {
        red = 255.0
        green = temp
        green = 99.4708025861 * Math.log(green) - 161.1195681661

        if (temp <= 19) {
            blue = 0.0
        } else {
            blue = temp - 10
            blue = 138.5177312231 * Math.log(blue) - 305.0447927307
        }
    } else {
        red = temp - 60
        red = 329.698727446 * Math.pow(red, -0.1332047592)

        green = temp - 60
        green = 288.1221695283 * Math.pow(green, -0.0755148492)

        blue = 255.0
    }
    red = clamp(red, 0.0, 255.0)
    green = clamp(green, 0.0, 255.0)
    blue = clamp(blue, 0.0, 255.0)

    return Color.rgb(red.toInt(), green.toInt(), blue.toInt())
}

/**
 * Clamp
 * Return value in range [min, max]
 * @param x
 * @param min
 * @param max
 * @return
 */
fun clamp(x: Int, min: Int, max: Int): Int {
    if (x <= min) return min
    if (x >= max) return max
    return x
}

fun clamp(x: Float, min: Float, max: Float): Float {
    if (x <= min) return min
    if (x >= max) return max
    return x
}

fun clamp(x: Double, min: Double, max: Double): Double {
    if (x <= min) return min
    if (x >= max) return max
    return x
}

/**
 * Calculate temp color
 *
 * @param ratio
 * @param gradient
 * @return
 */
fun calculateTempColor(ratio: Float, gradient: Gradient): Int {
    return when {
        ratio <= 0.0 -> gradient.hslColors.first().toColor()
        ratio >= 1 -> gradient.hslColors.last().toColor()
        else -> {
            var colorPosition = ratio * (gradient.hslColors.size - 1)
            val i = colorPosition.toInt()
            colorPosition -= i
            val c0 = gradient.hslColors[i].toColor()
            val c1 = gradient.hslColors[i + 1].toColor()

            val red = mix(Color.red(c0), Color.red(c1), colorPosition)
            val green = mix(Color.green(c0), Color.green(c1), colorPosition)
            val blue = mix(Color.blue(c0), Color.blue(c1), colorPosition)
            val alpha = mix(Color.alpha(c0), Color.alpha(c1), colorPosition)
            return Color.rgb(red, green, blue)
        }
    }
}

fun mix(start: Int, end: Int, position: Float): Int {
    return start + Math.round(position * (end - start))
}

//val rotateAnim = RotateAnimation(
//        0f, 360f,
//        Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F
//).apply {
//    duration = 7500
//    repeatCount = Animation.INFINITE
//    interpolator = LinearInterpolator()
//}

/**
 * Create holder
 * Create bitmap holder
 * @param context
 * @param tracks
 * @return
 */
suspend fun createHolder(context: Context, tracks: List<Track>): Bitmap? {
    try {
        val loader = ImageLoader(context)
        val rnd = Random(System.currentTimeMillis())

        val bmps = tracks.mapNotNull {
            try {
                val request = ImageRequest.Builder(context)
                    .data(it.images.firstOrNull()?.url)
                    .allowHardware(false)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.execute(request).drawable!!.toBitmap()
            } catch (exp: Exception) {
                null
            }
        }.map {
            val quarter = rnd.nextInt(4)
            it.crop(quarter)
        }

        val result = mutableListOf<Bitmap>()
        var i = 0
        while (i < 12) {
            result.add(bmps[i % bmps.size])
            i++
        }

        return combine(result)
    } catch (exp: Exception) {
        Timber.d("Failed ${exp.message}")
        return null
    }
}

/**
 * Build formula
 * Build formula for querying playlist/track
 * @param query
 * @return
 */
fun buildFormula(query: String?): String {
    if (query.isNullOrEmpty()) return ""

    val key = query.trim()
    val builder = StringBuilder()

    val queries = key.split(" ").map {
        """FIND("$it", LOWER({name}&{author})) > 0"""
    }

    if (queries.size > 1) {
        builder.append("AND(")
    }

    queries.forEachIndexed { index, query ->
        if (index == 0) {
            builder.append(query)
        } else if (index > 0 && index < queries.size) {
            builder.append(", ")
                .append(query)
            if (index == queries.size - 1) {
                builder.append(")")
            }
        }
    }
    return builder.toString()
}