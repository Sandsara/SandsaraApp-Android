package com.ht117.sandsara.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.ht117.sandsara.R

const val PADDING = 5
const val DEF_TXT_SIZE = 25
const val DEF_STROKE_WIDTH = 7

class NumberLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var txtSize: Int = 0
    private var txtColor: Int = 0
    private var loadedColor: Int = 0
    private var unloadedColor: Int = 0
    private var progress: Int = 0
    private var stroke: Int = 0
    private var content: String = "0%"
    private var min = 0
    private var max = 100
    private var isShowText = true
    private var txtPadding = PADDING
    private var radius = 0F

    private var loadedBarPainter = Paint(Paint.ANTI_ALIAS_FLAG)
    private var unloadedBarPainter = Paint(Paint.ANTI_ALIAS_FLAG)
    private var txtPainter = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        attrs?.run {
            initView(attrs)
        }
    }

    fun setProgress(progress: Int) {
        if (progress < max) {
            this.progress = progress
        } else {
            this.progress = progress
        }
        content = "$progress%"
        invalidate()
    }

    private fun initView(attrs: AttributeSet) {
        var array: TypedArray? = null

        try {
            array = context.obtainStyledAttributes(attrs, R.styleable.NumberLoadingView)
            txtSize = array!!.getDimensionPixelSize(R.styleable.NumberLoadingView_nlv_txtSize, DEF_TXT_SIZE)
            txtColor = array.getColor(R.styleable.NumberLoadingView_nlv_txtColor, Color.WHITE)
            loadedColor = array.getColor(R.styleable.NumberLoadingView_nlv_loadedColor, Color.GREEN)
            unloadedColor = array.getColor(R.styleable.NumberLoadingView_nlv_unloadedColor, Color.WHITE)
            stroke = array.getDimensionPixelSize(R.styleable.NumberLoadingView_nlv_strokeWidth, DEF_STROKE_WIDTH)
            min = array.getInt(R.styleable.NumberLoadingView_nlv_min, 0)
            max = array.getInt(R.styleable.NumberLoadingView_nlv_max, 100)
            isShowText = array.getBoolean(R.styleable.NumberLoadingView_nlv_showText, true)
            progress = 0
            content = "$progress%"
        } finally {
            array?.recycle()
        }
        loadedBarPainter.apply {
            color = loadedColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = stroke.toFloat()
        }

        unloadedBarPainter.apply {
            color = unloadedColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = stroke.toFloat()
        }

        txtPainter.apply {
            textSize = txtSize.toFloat()
            color = txtColor
            style = Paint.Style.FILL
            typeface = Typeface.DEFAULT_BOLD
        }

        if (!isShowText) {
            txtPadding = 0
        }
    }

    fun reset() {
        progress = 0
        content = "$progress%"
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isShowText) {
            val bound = Rect()
            txtPainter.getTextBounds(content, 0, content.length, bound)

            val hSize = MeasureSpec.getSize(heightMeasureSpec)
            val max = Math.max(hSize, bound.height() + txtPadding * 2)

            radius = (max / 2).toFloat()
            setMeasuredDimension(widthMeasureSpec, max)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val left = paddingLeft
        val top = paddingTop
        val width = width
        val height = height

        val bound = Rect()
        txtPainter.getTextBounds(content, 0, content.length, bound)

        if (progress == max) {
            if (isShowText) {
                canvas.drawRoundRect(left.toFloat(), (top + height / 2).toFloat(), (left + width / 2 - txtPadding - bound.width() / 2).toFloat(),
                    (top + height / 2).toFloat(), radius, radius, loadedBarPainter)
                canvas.drawText(
                    content,
                    (left + width / 2 - txtPadding - bound.width() / 2).toFloat(),
                    (top + height / 2 + bound.height() / 2).toFloat(),
                    txtPainter
                )

                canvas.drawRoundRect((left + width / 2 + txtPadding + bound.width() / 2).toFloat(), (top + height / 2).toFloat(), (left + width).toFloat(), (top + height / 2).toFloat(),
                    radius, radius, loadedBarPainter
                )
            } else {
                canvas.drawRoundRect(left.toFloat(), (top + height / 2).toFloat(), (left + width).toFloat(),
                    (top + height / 2).toFloat(), radius, radius, loadedBarPainter)
            }
        } else {
            val loaded = width * progress / max

            if (isShowText) {
                if (loaded + txtPadding * 2 + bound.width() <= width) {
                    canvas.drawRoundRect(left.toFloat(), (top + height / 2).toFloat(), (left + loaded).toFloat(),
                        (top + height / 2).toFloat(), radius, radius, loadedBarPainter)
                    canvas.drawText(
                        content,
                        (left + loaded + txtPadding).toFloat(),
                        (top + height / 2 + bound.height() / 2).toFloat(),
                        txtPainter
                    )
                    canvas.drawRoundRect((left + loaded + 2 * txtPadding + bound.width()).toFloat(), (top + height / 2).toFloat(),
                        (left + width).toFloat(), (top + height / 2).toFloat(), radius, radius,
                        unloadedBarPainter
                    )
                } else {
                    canvas.drawRoundRect(left.toFloat(), (top + height / 2).toFloat(), (left + width - 2 * txtPadding - bound.width()).toFloat(),
                        (top + height / 2).toFloat(), radius, radius,
                        loadedBarPainter
                    )
                    if (isShowText) {
                        canvas.drawText(
                            content,
                            (left + width - txtPadding - bound.width()).toFloat(),
                            (top + height / 2 + bound.height() / 2).toFloat(),
                            txtPainter
                        )
                    }
                }
            } else {
                canvas.drawRoundRect(left.toFloat(), (top + height / 2).toFloat(), (left + width).toFloat(),
                    (top + height / 2).toFloat(), radius, radius, unloadedBarPainter)
                canvas.drawRoundRect(left.toFloat(), (top + height / 2).toFloat(), (left + loaded).toFloat(),
                    (top + height / 2).toFloat(), radius, radius, loadedBarPainter)
            }

        }
    }
}