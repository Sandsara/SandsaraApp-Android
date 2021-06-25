package com.ht117.sandsara.ext

import android.graphics.*
import androidx.annotation.IntRange

fun Bitmap.crop(@IntRange(from = 0, to = 3) quarter: Int): Bitmap {
    val dstW = width / 2
    val dstH = height / 2
    val point = when (quarter) {
        0 -> Point(0, 0)
        1 -> Point(dstW, 0)
        2 -> Point(0, dstH)
        3 -> Point(dstW, dstH)
        else -> Point(0, 0)
    }

    return Bitmap.createBitmap(this, point.x, point.y, dstW, dstH)
}

/**
 * @param bmps size = 12
 */
fun combine(bmps: List<Bitmap>): Bitmap {
    if (bmps.size != 12) throw RuntimeException("Size it not suitable")
    val totalW = bmps[0].width + bmps[1].width + bmps[2].width + bmps[3].width
    val totalH = bmps[0].height + bmps[4].height + bmps[8].height
    val bmp = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val canvas = Canvas(bmp)

    val xPoints = listOf(0F, bmps[0].width.toFloat(), bmps[0].width + bmps[1].width.toFloat(), bmps[0].width + bmps[1].width + bmps[2].width.toFloat())
    val yPoints = listOf(0F, bmps[0].height.toFloat(), bmps[0].height.toFloat() + bmps[1].height)

    canvas.drawBitmap(bmps[0], xPoints[0], yPoints[0], paint)
    canvas.drawBitmap(bmps[1], xPoints[1], yPoints[0], paint)
    canvas.drawBitmap(bmps[2], xPoints[2], yPoints[0], paint)
    canvas.drawBitmap(bmps[3], xPoints[3], yPoints[0], paint)

    canvas.drawBitmap(bmps[4], xPoints[0], yPoints[1], paint)
    canvas.drawBitmap(bmps[5], xPoints[1], yPoints[1], paint)
    canvas.drawBitmap(bmps[6], xPoints[2], yPoints[1], paint)
    canvas.drawBitmap(bmps[7], xPoints[3], yPoints[1], paint)

    canvas.drawBitmap(bmps[8], xPoints[0], yPoints[2], paint)
    canvas.drawBitmap(bmps[9], xPoints[1], yPoints[2], paint)
    canvas.drawBitmap(bmps[10], xPoints[2], yPoints[2], paint)
    canvas.drawBitmap(bmps[11], xPoints[3], yPoints[2], paint)

    return bmp
}

fun LinearGradient.toBitmap(width: Int): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = this
    val bmp = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    canvas.drawRect(0F, 0F, width.toFloat(), 1F, paint)
    return bmp
}