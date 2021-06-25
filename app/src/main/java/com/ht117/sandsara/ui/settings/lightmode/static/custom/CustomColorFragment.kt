package com.ht117.sandsara.ui.settings.lightmode.static.custom

import android.graphics.Color.RED
import android.view.View
import androidx.lifecycle.lifecycleScope
import codes.side.andcolorpicker.converter.setFromColorInt
import codes.side.andcolorpicker.converter.toColorInt
import codes.side.andcolorpicker.model.Color
import codes.side.andcolorpicker.model.IntegerHSLColor
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ViewCustomColorBinding
import com.ht117.sandsara.ext.Prefs
import com.ht117.sandsara.ext.throttle
import com.ht117.sandsara.model.*
import com.ht117.sandsara.ui.base.BaseFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CustomColorFragment: BaseFragment(R.layout.view_custom_color) {

    private lateinit var binding: ViewCustomColorBinding

    override fun initView(view: View) {
        super.initView(view)
        binding = ViewCustomColorBinding.bind(view)

        binding.apply {

            lifecycleScope.launch {

                flowOf(BleManager.generalConfigFlow, hslColor.throttle(DELAY_TIME)).flattenMerge()
                    .collect {
                        if (it is Color) {
                            viewColor.setSwatchColor(it)
                            writePrefs(Prefs.CustomColor, it.toColorInt())
                            BleManager.sendPalette(it.toColorInt().toGradient())
                        } else if (it is GeneralConfig) {
                            renderPalette(it.paletteJson)
                        }
                    }
            }
        }
    }

    private fun renderPalette(paletteJson: String) {
        binding.run {
            if (paletteJson.isNullOrEmpty()) {
                setColor(RED)
            } else {
                val gradient = Json.decodeFromString<Gradient>(paletteJson)
                if (gradient.isStatic()) {
                    setColor(gradient.hslColors.first().toColor())
                } else {
                    setColor(RED)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BleManager.setStaticMode(CustomColor)
    }

    private fun setColor(value: Int) {
        binding.run {
            hslColor.setInitColor(value)
            viewColor.setSwatchColor(IntegerHSLColor().also { it.setFromColorInt(value) })
        }
    }
}