package com.ht117.sandsara.ui.settings.lightmode.static

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ViewColorStaticBinding
import com.ht117.sandsara.ext.Prefs
import com.ht117.sandsara.ext.read
import com.ht117.sandsara.model.StaticPalette
import com.ht117.sandsara.model.TempColor
import com.ht117.sandsara.ui.base.BaseFragment
import com.ht117.sandsara.ui.settings.lightmode.static.custom.CustomColorFragment
import com.ht117.sandsara.ui.settings.lightmode.static.temperature.ColorTemperatureFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StaticFragment: BaseFragment(R.layout.view_color_static) {

    private lateinit var binding: ViewColorStaticBinding

    override fun initView(view: View) {
        super.initView(view)

        binding = ViewColorStaticBinding.bind(view).apply {
            staticTabColor.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val fragment = when (tab?.position) {
                        0 -> ColorTemperatureFragment()
                        1 -> CustomColorFragment()
                        else -> ColorTemperatureFragment()
                    }

                    childFragmentManager.beginTransaction()
                            .replace(R.id.staticContainer, fragment)
                            .commitAllowingStateLoss()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        BleManager.setColorMode(StaticPalette)

        binding.run {
            staticTabColor.getTabAt(0)?.select()
            staticTabColor.getTabAt(1)?.select()
        }
    }
}