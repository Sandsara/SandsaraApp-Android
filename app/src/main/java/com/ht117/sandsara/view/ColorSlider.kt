package com.ht117.sandsara.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ht117.sandsara.R
import com.ht117.sandsara.calculateTempColor
import com.ht117.sandsara.clamp
import com.ht117.sandsara.ext.toSquareF
import com.ht117.sandsara.model.*
import timber.log.Timber
import kotlin.math.roundToInt

class ColorSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gradient = Color.WHITE.toGradient()

    private var downEvent: MotionEvent? = null
    private var editPos: Float = 0F
    private var isInEditModeColor = false
    private var isSlidable = false
    private var dotRadius: Float = 48F
    private var barRect = RectF()

    private var selectedIndex = Int.MIN_VALUE
    private var selectRanges = FloatArray(2)
    private var selectedDotColor: Int = Color.TRANSPARENT
    private var selectedDotPos: Float = 0F

    private var barHeight: Float = 150F
    private var stickHeight: Float = barHeight / 2
    private var barWidth: Float = 0F

    private lateinit var colorGradient: LinearGradient

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
    }
    private val previewPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 4F
    }
    private var callback: IInEditMode? = null

    init {
        context.let {
            val typedArr = it.obtainStyledAttributes(attrs, R.styleable.ColorSlider)
            barHeight = typedArr.getDimension(R.styleable.ColorSlider_cs_barHeight, barHeight)
            stickHeight = barHeight / 2
            typedArr.recycle()
        }

        bulletPaint.apply {
            strokeWidth = 4F
        }
    }

    fun setColorSeeds(newGradient: Gradient) {
        downEvent = null
        gradient = Gradient(hslColors = newGradient.hslColors)

        selectedDotColor = Color.TRANSPARENT
        selectedDotPos = 0F

        colorGradient = gradient.toLinearGradient(barWidth)
        barPaint.shader = colorGradient
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val barLeft = paddingStart.toFloat() + dotRadius + 2
        val barRight = width - paddingEnd.toFloat() - dotRadius - 2
        val barTop = paddingTop.toFloat()
        val barBot = paddingTop + barHeight
        barWidth = barRight - barLeft
        barRect.set(barLeft, barTop, barRight, barBot)

        canvas?.run {
            drawRect(barRect, barPaint)
            gradient.hslColors.forEachIndexed { index, hsl ->
                if (index != selectedIndex) {
                    val bulletColor = hsl.toColor()
                    bulletPaint.color = bulletColor

                    val pos = when (index) {
                        0 -> barLeft + bulletPaint.strokeWidth / 2
                        gradient.hslColors.size - 1 -> barRight - bulletPaint.strokeWidth / 2
                        else -> barRect.left + hsl.toRelativePos(barWidth) + bulletPaint.strokeWidth / 2 - dotRadius
                    }
                    val center = barRect.bottom + stickHeight + dotRadius / 2
                    drawLine(pos, barRect.bottom, pos, barRect.bottom + stickHeight, bulletPaint)
                    drawCircle(pos, center, dotRadius, bulletPaint)

                    val hsv = FloatArray(3)
                    Color.colorToHSV(bulletColor, hsv)
                    val brightness = hsv[2]
                    if (brightness < 0.15F) {
                        drawCircle(pos, center, dotRadius, borderPaint)
                    }
                }
            }

            // Drawing preview dot
            downEvent?.run {
                if (isInEditModeColor) {
                    Timber.d("In edit mode $x")
                    val x = when (selectedIndex) {
                        0 -> barRect.left + bulletPaint.strokeWidth / 2
                        gradient.hslColors.size - 1 -> barRect.right - bulletPaint.strokeWidth / 2
                        else -> selectedDotPos + bulletPaint.strokeWidth / 2 - dotRadius
                    }
                    Timber.d("Drawing line preview ")
                    drawLine(x, barBot, x, barBot + stickHeight * 2, previewPaint)
                } else {
                    val pos = selectedDotPos + bulletPaint.strokeWidth / 2 - dotRadius
                    drawLine(pos, barRect.bottom, pos, barRect.bottom + stickHeight, previewPaint)
                    drawCircle(pos, barRect.bottom + stickHeight + dotRadius / 2, dotRadius, previewPaint)
                    val hsv = FloatArray(3)
                    Color.colorToHSV(previewPaint.color, hsv)
                    val brightness = hsv[2]
                    if (brightness < 0.15F) {
                        drawCircle(pos, barRect.bottom + stickHeight + dotRadius / 2, dotRadius, borderPaint)
                    }
                }
            }
        }
    }

    private fun getSelectedPointIndex(x: Float, y: Float): Int {

        val squares = gradient.hslColors.mapIndexed { index, hsl ->
            when (index) {
                0 -> PointF(barRect.left + bulletPaint.strokeWidth / 2 - dotRadius, barRect.bottom + stickHeight).toSquareF(dotRadius * 2)
                gradient.hslColors.size - 1 -> PointF(barRect.right - bulletPaint.strokeWidth / 2 - dotRadius, barRect.bottom + stickHeight).toSquareF(dotRadius * 2)
                else -> PointF(barRect.left + hsl.toRelativePos(barWidth) + bulletPaint.strokeWidth / 2 - (1.5F * dotRadius), barRect.bottom + stickHeight).toSquareF(dotRadius * 2)
            }
        }

        squares.forEachIndexed { index, rectF ->
            if (rectF.contains(x, y)) {
                return index
            }
        }
        return -1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, (barHeight + stickHeight + dotRadius * 2).roundToInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        colorGradient = gradient.toLinearGradient(w.toFloat())
        barPaint.shader = colorGradient
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isInEditModeColor) {
                    downEvent = MotionEvent.obtain(event)
                    selectedIndex = getSelectedPointIndex(event.x, event.y)
                    selectedDotPos = event.x
                    calculatePosInRange(event.x)

                    Timber.d("Down ... $selectedIndex")
                }
            }
            MotionEvent.ACTION_MOVE -> {
                isSlidable = 0 < selectedIndex && selectedIndex < gradient.hslColors.size - 1
                if (!isInEditModeColor && isSlidable) {

                    Timber.d("Moving.... [${selectRanges[0]} - ${selectRanges[1]}] - ${event.x}")
                    parent.requestDisallowInterceptTouchEvent(true)

                    selectedDotPos = clamp(event.x, selectRanges[0], selectRanges[1])

                    selectedDotColor = if (selectedIndex >= 0) {
                        gradient.hslColors[selectedIndex].toColor()
                    } else {
                        calculateTempColor((selectedDotPos - dotRadius) / barWidth, gradient)
                    }
                    previewPaint.color = selectedDotColor
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                Timber.d("Up... ${downEvent?.x}")
                if (!isInEditModeColor) {
                    isSlidable = false
                    isInEditModeColor = true
                    editPos = selectedDotPos
                    selectedDotColor = if (selectedIndex >= 0) {
                        gradient.hslColors[selectedIndex].toColor()
                    } else {
                        calculateTempColor((selectedDotPos - dotRadius) / barWidth, gradient)
                    }
                    previewPaint.color = selectedDotColor

                    if (0 < selectedIndex && selectedIndex < gradient.hslColors.size - 1) {
                        val newIndex = getSelectedPointIndex(event.x, event.y)
                        if (newIndex == selectedIndex) {
                            callback?.inEdit(selectedDotColor)
                            invalidate()
                        } else {
                            addPoint(selectedDotColor)
                        }
                    } else {
                        callback?.inEdit(selectedDotColor)
                        invalidate()
                    }
                }
            }
        }
        return true
    }

    fun addPoint(color: Int) {
        if (isInEditModeColor) {
            isInEditModeColor = false
            isSlidable = false

            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            val newColors = gradient.hslColors.toMutableList()

            val hsl = when (selectedIndex) {
                0 -> {
                    HslColor(0, red, green, blue)
                }
                gradient.hslColors.size - 1 -> {
                    HslColor(255, red, green, blue)
                }
                else -> {
                    HslColor(((editPos - barRect.left + dotRadius / 2) * 255F / barWidth).roundToInt(), red, green, blue)
                }
            }

            if (selectedIndex == -1) {
                newColors.add(hsl)
            } else {
                newColors[selectedIndex] = hsl
            }
            newColors.sortBy { it.pos }

            gradient = gradient.copy(hslColors = newColors)
            colorGradient = gradient.toLinearGradient(barWidth)
            barPaint.shader = colorGradient

            editPos = 0F
            selectedDotPos = 0F
            selectedIndex = Int.MIN_VALUE
            invalidate()
            callback?.onChange(gradient)
        }
    }

    fun cancelEdit() {
        isInEditModeColor = false
        isSlidable = false

        Timber.d("Cancel $selectedIndex")
        if (0 < selectedIndex && selectedIndex < gradient.hslColors.size - 1) {
            val newHsls = gradient.hslColors.toMutableList()
            newHsls.removeAt(selectedIndex)
            gradient = gradient.copy(hslColors = newHsls)
            colorGradient = gradient.toLinearGradient(barWidth)
            barPaint.shader = colorGradient
        }

        selectedIndex = Int.MIN_VALUE
        editPos = 0F
        selectedDotPos = 0F
        invalidate()
    }

    private fun calculatePosInRange(pos: Float) {
        val value = pos / barWidth * 255F

        val hsls = if (selectedIndex == -1) {
            gradient.hslColors
        } else {
            gradient.hslColors.toMutableList().apply {
                removeAt(selectedIndex)
            }
        }

        for (i in 0 until hsls.size - 1) {
            val start = hsls[i]
            val end = hsls[i + 1]

            if (start.pos < value && value < end.pos) {
                val baseStart = start.pos / 255F * barWidth + barRect.left

                selectRanges[0] = if (i == 0) {
                    baseStart + dotRadius * 2
                } else {
                    baseStart + dotRadius
                }

                val baseEnd = barRect.left + end.pos / 255F * barWidth
                selectRanges[1] = if (i == hsls.size - 1) {
                    baseEnd
                } else {
                    baseEnd - dotRadius
                }
                break
            }
        }
    }

    fun addOnEditModeListener(callback: IInEditMode?) {
        this.callback = callback
    }

    fun getGradient() = gradient

    interface IInEditMode {
        fun inEdit(color: Int)
        fun inPreview()
        fun onChange(gradient: Gradient)
    }

    companion object {
        const val MAX_POINTS = 12
    }
}