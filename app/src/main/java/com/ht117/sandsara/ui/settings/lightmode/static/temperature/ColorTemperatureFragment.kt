package com.ht117.sandsara.ui.settings.lightmode.static.temperature

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.calculateTempColor
import com.ht117.sandsara.clamp
import com.ht117.sandsara.databinding.ViewTemperatureColorBinding
import com.ht117.sandsara.ext.Prefs
import com.ht117.sandsara.ext.read
import com.ht117.sandsara.ext.throttle
import com.ht117.sandsara.ext.write
import com.ht117.sandsara.model.*
import com.ht117.sandsara.ui.base.BaseFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ColorTemperatureFragment: BaseFragment(R.layout.view_temperature_color) {

    private lateinit var binding: ViewTemperatureColorBinding

    override fun initView(view: View) {
        super.initView(view)

        binding = ViewTemperatureColorBinding.bind(view)
        binding.apply {

            viewColor.setBackgroundColor(calculateTempColor(slider.valueFrom.toInt()))

            lifecycleScope.launch {
                slider.throttle(DELAY_TIME).collect {
                    val color = calculateTempColor(it.value)
                    viewColor.setBackgroundColor(color)
                    writePrefs(Prefs.TempPalette, color)
                    BleManager.sendPalette(color.toGradient())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BleManager.setStaticMode(TempColor)

        binding.run {
            val mode = readPrefs(Prefs.PaletteMode, CycleMode)
            if (mode == TempMode) {
                val selectPaletteJson = readPrefs(Prefs.SelectedPalette, "")
                if (selectPaletteJson.isNullOrEmpty()) {
                    slider.value = DEF_KEVIN.toFloat()
                    viewColor.setBackgroundColor(calculateTempColor(DEF_KEVIN))
                } else {
                    val gradient = Json.decodeFromString<Gradient>(selectPaletteJson)
                    val value = gradient.hslColors.first().toColor()
                    slider.value = clamp(value.toFloat(), slider.valueFrom, slider.valueTo)
                    viewColor.setBackgroundColor(calculateTempColor(slider.value.toInt()))
                }
            } else {
                val value = readPrefs(Prefs.TempPalette, DEF_KEVIN)
                slider.value = clamp(value.toFloat(), slider.valueFrom, slider.valueTo)
                viewColor.setBackgroundColor(calculateTempColor(slider.value.toInt()))
            }
        }
    }

    companion object {
        const val DEF_KEVIN = 2000
    }
}