package com.ht117.sandsara.ui.splash

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.FragmentSplashBinding
import com.ht117.sandsara.ext.Prefs
import com.ht117.sandsara.ext.read
import com.ht117.sandsara.ui.base.BaseFragment
import com.ht117.sandsara.ui.base.IAction
import com.ht117.sandsara.ui.base.IState
import com.ht117.sandsara.ui.base.IView
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

object SplashAction: IAction

/**
 * Splash state
 *
 * @property isSuccess
 * @property isFinished
 * @constructor Create empty Splash state
 */
data class SplashState(val isSuccess: Boolean = false,
                       val isFinished: Boolean = false): IState

/**
 * Splash fragment
 *
 * @constructor Create empty Splash fragment
 */
class SplashFragment: BaseFragment(R.layout.fragment_splash), IView<SplashState> {

    private lateinit var binding: FragmentSplashBinding
    private val viewModel: SplashViewModel by viewModel()

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentSplashBinding.bind(view)

        lifecycleScope.launchWhenStarted {
            viewModel.actions.emit(SplashAction)
            viewModel.state.collect { render(it) }
        }
    }

    override fun render(state: SplashState) {
        if (state.isFinished) {
            val mac = readPrefs(Prefs.MacAddress, "")
            if (mac.isNullOrEmpty()) {
                navigateTo(R.id.splash_to_setup)
            } else {
                navigateTo(R.id.splash_to_home)
            }
        }
    }
}