package com.ht117.sandsara.ui.settings.lightmode.cycle

import android.view.View
import androidx.lifecycle.lifecycleScope
import codes.side.andcolorpicker.model.Color
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.PresetAdapter
import com.ht117.sandsara.clamp
import com.ht117.sandsara.databinding.ViewColorCycleBinding
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.model.*
import com.ht117.sandsara.ui.base.BaseFragment
import com.ht117.sandsara.ui.base.IAction
import com.ht117.sandsara.ui.base.IState
import com.ht117.sandsara.ui.base.IView
import com.ht117.sandsara.ui.settings.SettingFragment
import com.ht117.sandsara.view.ColorSlider
import com.ht117.sandsara.view.HSLColorView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.internal.applyConnectionSpec
import org.koin.androidx.viewmodel.ext.android.viewModel

data class CycleState(val palettes: List<Palette> = emptyList(), val selectedPalette: Gradient? = null) : IState

sealed class CycleAction : IAction {
    object LoadPalette : CycleAction()
}

class CycleFragment : BaseFragment(R.layout.view_color_cycle), IView<CycleState> {

    private var binding: ViewColorCycleBinding? = null
    private val viewModel: CycleViewModel by viewModel()

    private val adapter by lazy {
        PresetAdapter { gradient, _ ->
            binding?.colorSliderPallete?.setColorSeeds(gradient)
            writePrefs(Prefs.CyclePalette, Json.encodeToString(gradient))
            sendCommand(gradient)
        }
    }

    override fun render(state: CycleState) {
        val gradients = state.palettes.map { it.toGradient() }
        adapter.updateItems(gradients)

        val json = readPrefs(Prefs.SelectedPalette, "")
        renderPalette(json)
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = ViewColorCycleBinding.bind(view).apply {
            rvPresets.adapter = adapter

            ivDelete.setOnClickListener {
                colorSliderPallete.cancelEdit()
                hideColorEdit()
            }

            ivDone.setOnClickListener {
                colorSliderPallete.addPoint(hslColor.getCurrentColor())
                hideColorEdit()
            }

            hslColor.addOnColorChangeListener(object : HSLColorView.IColorChangeListener {
                override fun onSelectedColor(color: Color) {
                    viewEditColor.setSwatchColor(color)
                }

            })

            colorSliderPallete.addOnEditModeListener(object : ColorSlider.IInEditMode {
                override fun inEdit(color: Int) {
                    binding.run {
                        showColorEdit()
                        hslColor.setInitColor(color)
                    }
                }

                override fun inPreview() {
                    hideColorEdit()
                }

                override fun onChange(gradient: Gradient) {
                    sendCommand(gradient)
                }
            })

            lifecycleScope.run {
                launchWhenResumed {
                    binding?.run {
                        listOf(
                            viewModel.state,
                            BleManager.cycleConfigFlow,
                            lightSpeed.throttle(DELAY_TIME)
                        ).asFlow().flattenMerge().collect {
                            when (it) {
                                is CycleState -> {
                                    render(it)
                                }
                                is CycleConfig -> {
                                    flipDirection.setOnCheckedChangeListener(null)
                                    switchRotate.setOnCheckedChangeListener(null)

                                    flipDirection.isChecked = it.flipDirection == 1
                                    switchRotate.isChecked = it.cycleMode == 0

                                    if (it.stripSpeed.toFloat() != lightSpeed.value) {
                                        lightSpeed.value = clamp(
                                            it.stripSpeed.toFloat(),
                                            lightSpeed.valueFrom,
                                            lightSpeed.valueTo
                                        )
                                    }

                                    flipDirection.setOnCheckedChangeListener { _, isChecked ->
                                        val value = if (isChecked) 1 else 0

                                        lifecycleScope.launch {
                                            BleManager.updateFlipDirection(value)
                                        }
                                    }

                                    switchRotate.setOnCheckedChangeListener { _, isChecked ->
                                        val value = if (isChecked) 0 else 1
                                        lifecycleScope.launch { BleManager.updateCycleMode(value) }
                                    }
                                }
                                is SliderDebounce -> {
                                    BleManager.updateLightSpeed(it.value)
                                }
                            }
                        }
                    }
                }
                launchWhenStarted { viewModel.actions.emit(CycleAction.LoadPalette) }
            }
        }
    }

    private fun renderPalette(paletteJson: String) {
        if (paletteJson.isNullOrEmpty()) return
        val gradient = Json.decodeFromString<Gradient>(paletteJson)
        if (gradient.isStatic()) {
            if (adapter.itemCount != 0) {
              binding?.colorSliderPallete?.setColorSeeds(adapter.getItem(0))
            }
        } else {
            binding?.colorSliderPallete?.setColorSeeds(gradient)
        }
    }

    override fun onResume() {
        super.onResume()
        BleManager.setColorMode(CyclePalette)
    }

    private fun showColorEdit() {
        binding?.run {
            viewEditColor.show()
            hslColor.show()
            buttons.show()
            grEdit.hide()
        }

        if (parentFragment is SettingFragment) {
            (parentFragment as SettingFragment).showEditColor()
        }
    }

    private fun hideColorEdit() {
        binding?.run {
            viewEditColor.hide()
            hslColor.hide()
            buttons.hide()
            grEdit.show()
            val gradient = colorSliderPallete.getGradient()
            sendCommand(gradient)
        }

        if (parentFragment is SettingFragment) {
            (parentFragment as SettingFragment).hideEditColor()
        }
    }

    private fun sendCommand(gradient: Gradient) {
        lifecycleScope.launch {
            delay(DELAY_TIME)
            BleManager.sendPalette(gradient)
        }
    }
}