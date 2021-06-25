package com.ht117.sandsara.ui.settings

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.BleServices
import com.ht117.sandsara.R
import com.ht117.sandsara.clamp
import com.ht117.sandsara.databinding.FragmentSettingBinding
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.model.*
import com.ht117.sandsara.ui.base.BaseFragment
import com.ht117.sandsara.ui.base.IAction
import com.ht117.sandsara.ui.base.IState
import com.ht117.sandsara.ui.settings.lightmode.cycle.CycleFragment
import com.ht117.sandsara.ui.settings.lightmode.static.StaticFragment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

sealed class SettingState: IState {
    object Initialize: SettingState()
}

sealed class SettingAction: IAction

class SettingFragment: BaseFragment(R.layout.fragment_setting) {

    private lateinit var binding: FragmentSettingBinding

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentSettingBinding.bind(view)

        binding.run {
            tabLight.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    Timber.d("Select tab ${tab?.position}")
                    hideEditColor()
                    val fragment = when (tab?.position) {
                        1 -> StaticFragment()
                        else -> CycleFragment()
                    }

                    childFragmentManager.beginTransaction()
                            .replace(R.id.colorContainer, fragment)
                            .commit()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })

            tvAdvance.setOnClickListener {
                navigateTo(R.id.setting_to_advance)
            }

            tvWebsite.setOnClickListener {
                val uri = Uri.parse(BleServices.WEB_URL)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                }
            }

            switchSleep.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    if (isChecked) {
                        BleManager.toggleSleep(1)
                    } else {
                        BleManager.playOrResume()
                    }
                }
            }

            lifecycleScope.run {
                launchWhenResumed {
                    flowOf(BleManager.generalConfigFlow, seekBallSpeed.throttle(DELAY_TIME), seekBrightness.throttle(DELAY_TIME))
                        .flattenMerge().collect {
                            if (it is SliderDebounce) {
                                if (it.id == R.id.seek_ball_speed) {
                                    BleManager.updateBallSpeed(it.value)
                                } else if (it.id == R.id.seekBrightness) {
                                    BleManager.updateBrightness(it.value)
                                }
                            } else if (it is GeneralConfig) {
                                binding.run {
                                    if (seekBallSpeed.value != it.ballSpeed.toFloat()) {
                                        seekBallSpeed.value = clamp(
                                            it.ballSpeed.toFloat(),
                                            seekBallSpeed.valueFrom,
                                            seekBallSpeed.valueTo
                                        )
                                    }
                                    if (seekBrightness.value != it.brightness.toFloat()) {
                                        seekBrightness.value = clamp(
                                            it.brightness.toFloat(),
                                            seekBrightness.valueFrom,
                                            seekBrightness.valueTo
                                        )
                                    }

                                    switchSleep.setOnCheckedChangeListener(null)
                                    switchSleep.isChecked = it.status == 4

                                    switchSleep.setOnCheckedChangeListener { _, isChecked ->
                                        lifecycleScope.launch {
                                            if (isChecked) {
                                                BleManager.toggleSleep(1)
                                            } else {
                                                BleManager.playOrResume()
                                            }
                                        }
                                    }

                                    if (!it.paletteJson.isNullOrEmpty() && !BleManager.loadPalette) {
                                        BleManager.loadPalette = true
                                        val gradient = Json.decodeFromString<Gradient>(it.paletteJson)
                                        if (gradient.isStatic()) {
                                            enterStaticMode()
                                        } else {
                                            enterCycleMode()
                                        }
                                    } else {
                                        //enterCycleMode()
                                    }
                                }
                            }
                        }

                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        BleManager.loadPalette = false
    }

    private fun enterCycleMode() {
        binding.run {
            tabLight.getTabAt(1)?.select()
            tabLight.getTabAt(0)?.select()
        }
    }

    private fun enterStaticMode() {
        binding.run {
            tabLight.getTabAt(0)?.select()
            tabLight.getTabAt(1)?.select()
        }
    }

    fun showEditColor() {
        binding.grColor.hide()
    }

    fun hideEditColor() {
        binding.grColor.show()
    }
}