package com.ht117.sandsara.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.FloatRange
import androidx.core.graphics.get
import com.ht117.sandsara.R
import com.ht117.sandsara.ext.distance
import com.ht117.sandsara.ext.toBitmap
import com.ht117.sandsara.ext.toRGB
import com.ht117.sandsara.mix
import timber.log.Timber
import kotlin.math.roundToInt

class ColorSeekBar(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    private var colorSeeds = intArrayOf(
            Color.parseColor("#ff0000"), Color.parseColor("#faff00"),
            Color.parseColor("#14ff00"), Color.parseColor("#00fff0"),
            Color.parseColor("#0500ff"), Color.parseColor("#e400ff"),
            Color.parseColor("#ff0000"))
    private var canvasHeight: Int = 160
    private var barHeight: Int = 90
    private var rectf: RectF = RectF()
    private var rectPaint: Paint = Paint()
    private var thumbBorderPaint: Paint = Paint()
    private var thumbPaint: Paint = Paint()
    private var colorGradient = LinearGradient(0F, 0F, Math.max(width.toFloat(), 100F), 0F, colorSeeds, null, Shader.TileMode.CLAMP)
    private var startPoint = 0
    private var thumbX: Float = 24f
    private var thumbY: Float = (canvasHeight / 2).toFloat()
    private var thumbBorder: Float = 4f
    private var thumbRadius: Float = 24f
    private var thumbBorderRadius: Float = thumbRadius + thumbBorder
    private var thumbBorderColor = Color.WHITE
    private var paddingStart = 30f
    private var paddingEnd = 30f
    private var barCornerRadius: Float = 8f
    private var oldThumbRadius = thumbRadius
    private var oldThumbBorderRadius = thumbBorderRadius
    private var colorChangeListener: OnColorChangeListener? = null

    init {
        attributeSet?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ColorSeekBar)
            val colorsId = typedArray.getResourceId(R.styleable.ColorSeekBar_colorSeeds, 0)
            if (colorsId != 0) colorSeeds = getColorsById(colorsId)
            barCornerRadius = typedArray.getDimension(R.styleable.ColorSeekBar_cornerRadius, 8f)
            barHeight = typedArray.getDimension(R.styleable.ColorSeekBar_barHeight, 90f).toInt()
            thumbBorder = typedArray.getDimension(R.styleable.ColorSeekBar_thumbBorder, 4f)
            thumbBorderColor = typedArray.getColor(R.styleable.ColorSeekBar_thumbBorderColor, Color.WHITE)
            startPoint = typedArray.getInt(R.styleable.ColorSeekBar_startPoint, 0)
            typedArray.recycle()
        }
        rectPaint.isAntiAlias = true

        thumbBorderPaint.isAntiAlias = true
        thumbBorderPaint.color = thumbBorderColor

        thumbPaint.isAntiAlias = true

        thumbBorderRadius = thumbRadius + thumbBorder
        canvasHeight = (thumbBorderRadius * 3).toInt()
        thumbY = (canvasHeight / 2).toFloat()
        thumbX = when (startPoint) {
            1 -> width - paddingEnd
            0 -> width / 2F
            else -> paddingStart
        }

        oldThumbRadius = thumbRadius
        oldThumbBorderRadius = thumbBorderRadius
    }

    fun setColorSeeds(colors: IntArray) {
        colorSeeds = colors
        colorGradient = LinearGradient(0f, 0f, width.toFloat(), 0f, colorSeeds, null, Shader.TileMode.CLAMP)
        rectPaint.shader = colorGradient
        invalidate()
    }

    private fun getColorsById(@ArrayRes id: Int): IntArray {
        val s = context.resources.getStringArray(id)
        val colors = IntArray(s.size)
        for (j in s.indices) {
            colors[j] = Color.parseColor(s[j])
        }
        return colors
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        when (startPoint) {
            -1 -> thumbX = left.toFloat()
            0 -> thumbX = (right - left) / 2F
            1 -> thumbX = right.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //color bar position
        val barLeft: Float = paddingStart
        val barRight: Float = width.toFloat() - paddingEnd
        val barTop: Float = ((canvasHeight / 2) - (barHeight / 2)).toFloat()
        val barBottom: Float = ((canvasHeight / 2) + (barHeight / 2)).toFloat()

        //draw color bar
        rectf.set(barLeft, barTop, barRight, barBottom)
        canvas?.drawRoundRect(rectf, barCornerRadius, barCornerRadius, rectPaint)

        if (thumbX < barLeft) {
            thumbX = barLeft
        } else if (thumbX > barRight) {
            thumbX = barRight
        }
        thumbPaint.color = Color.WHITE

        // draw color bar thumb
        canvas?.drawCircle(thumbX, thumbY, thumbBorderRadius, thumbBorderPaint)
        canvas?.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        colorGradient = LinearGradient(0f, 0f, w.toFloat(), 0f, colorSeeds, null, Shader.TileMode.CLAMP)
        rectPaint.shader = colorGradient
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, canvasHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                thumbBorderRadius = (oldThumbBorderRadius * 1.5).toFloat()
                thumbRadius = (oldThumbRadius * 1.5).toFloat()
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                event.x.let {
                    thumbX = it
                    invalidate()
                }
                colorChangeListener?.onColorChangeListener(getRatio())
            }
            MotionEvent.ACTION_UP -> {
                thumbBorderRadius = oldThumbBorderRadius
                thumbRadius = oldThumbRadius
                invalidate()
            }
        }
        return true
    }

    fun getRatio(): Float {
        return (thumbX - paddingStart) / (width - (paddingStart + paddingEnd))
    }

    fun setOnColorChangeListener(onColorChangeListener: OnColorChangeListener) {
        this.colorChangeListener = onColorChangeListener
    }

    fun setRatio(@FloatRange(from = 0.0, to = 1.0) value: Float) {
        thumbX = paddingStart + value * (width - paddingStart - paddingEnd)
        invalidate()
    }

    fun setRatioByColor(color: Int) {
        val target = color.toRGB()
        val bmp = colorGradient.toBitmap(context.resources.displayMetrics.widthPixels)

        var min = Double.MAX_VALUE
        var index = 0
        for (i in 0 until bmp.width) {
            val value = bmp[i, 0].toRGB()

            val dist = target.distance(value)

            if (dist < min) {
                min = dist
                index = i
            }
        }
        Timber.d("Result $target ${bmp[index, 0].toRGB()}")

        val ratio = index * 1.0 / bmp.width
        Timber.d("Ratio $ratio")
        setRatio(ratio.toFloat())
    }

    interface OnColorChangeListener {

        fun onColorChangeListener(ratio: Float)
    }
}