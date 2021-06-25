package com.ht117.sandsara.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import codes.side.andcolorpicker.converter.setFromColorInt
import codes.side.andcolorpicker.converter.toColorInt
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.group.registerPickers
import codes.side.andcolorpicker.hsl.HSLColorPickerSeekBar
import codes.side.andcolorpicker.model.IntegerHSLColor
import codes.side.andcolorpicker.view.picker.ColorSeekBar
import com.ht117.sandsara.databinding.ViewHslColorBinding

class HSLColorView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewHslColorBinding = ViewHslColorBinding.inflate(LayoutInflater.from(context), this, true)
    private var group = PickerGroup<IntegerHSLColor>()
    private var listener: IColorChangeListener? = null
    private var curColor: Int = Color.RED

    fun addOnColorChangeListener(listener: IColorChangeListener) {
        this.listener = listener
    }

    fun setInitColor(color: Int) {
        curColor = color
        group.setColor(IntegerHSLColor().also {
            it.setFromColorInt(color)
        })
    }

    fun getCurrentColor(): Int {
        return curColor
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.run {
            group.registerPickers(hueSeekBar, satSeekBar, lightSeekBar)

            group.addListener(object: HSLColorPickerSeekBar.DefaultOnColorPickListener() {
                override fun onColorChanged(
                    picker: ColorSeekBar<IntegerHSLColor>,
                    color: IntegerHSLColor,
                    value: Int
                ) {
                    curColor = color.toColorInt()
                    listener?.onSelectedColor(color)
                }
            })
        }
    }

    interface IColorChangeListener {
        fun onSelectedColor(color: codes.side.andcolorpicker.model.Color)
    }
}