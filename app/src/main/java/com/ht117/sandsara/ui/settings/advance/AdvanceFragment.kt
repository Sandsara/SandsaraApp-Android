package com.ht117.sandsara.ui.settings.advance

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.BuildConfig
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.FragmentAdvanceSettingsBinding
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.model.FirmwareModel
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.sync.UpgradeFragment.Companion.KEY_FIRMWARE
import com.maxkeppeler.bottomsheets.input.InputSheet
import com.maxkeppeler.bottomsheets.input.type.InputEditText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

data class AdvanceState(val isDownloading: Boolean = false,
                        val progress: Resource<Int> = Resource.Idle()): IState
data class AdvanceAction(val firmware: FirmwareModel): IAction

class AdvanceFragment : BaseFragment(R.layout.fragment_advance_settings), IView<AdvanceState> {

    private lateinit var binding: FragmentAdvanceSettingsBinding
    private val viewModel: AdvanceViewModel by viewModel()

    private val firmware by lazy {
        try {
            val jsonStr = requireContext().read(Prefs.NewFirmware, "")
            if (!jsonStr.isNullOrEmpty()) {
                Json.decodeFromString<FirmwareModel>(jsonStr)
            } else {
                null
            }
        } catch (exp: Exception) {
            Timber.d("Json parsing exception ${exp.message}")
            null
        }
    }

    override fun render(state: AdvanceState) {
        if (state.isDownloading) {
            binding.nlvProgress.show()
            handleDownloading(state.progress)
        } else {
            binding.nlvProgress.hide()
        }
    }

    private fun handleDownloading(state: Resource<Int>) = lifecycleScope.launch {
        if (state is Resource.Stream) {
            state.stream.collect {
                binding.nlvProgress.setProgress(it)
                if (it == 100) {
                    binding.run {
                        nlvProgress.hide()
                        tvDownload.text = getString(R.string.update_firmware)
                    }
                }
            }
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentAdvanceSettingsBinding.bind(view)

        binding.apply {

            tvName.text = getString(R.string.name, readPrefs(Prefs.Device, "Unknown"))
            tvFirmware.text = getString(R.string.current_firmware, readPrefs(Prefs.Version, "Unknown"))

            if (olderVersion() || BuildConfig.DEBUG) {
                tvFirmwareUpdatable.text = getString(R.string.updatable_firmware, firmware?.version)
                tvDownload.show()
            }

            ivBack.setOnClickListener {
                navigateBack()
            }
            tvDownload.setOnClickListener {
                lifecycleScope.launch {
                    if (tvDownload.text == getString(R.string.download)) {
                        tvDownload.text = getString(R.string.downloading)
                        nlvProgress.show()
                        firmware?.run {
                            viewModel.actions.emit(AdvanceAction(this))
                        }
                    } else {
                        if (tvDownload.text == getString(R.string.update_firmware)) {

                            val sandFile = firmware?.file?.firstOrNull()?: return@launch

                            navigateTo(R.id.advance_to_upgrade, bundleOf(KEY_FIRMWARE to sandFile.filename))
                        }
                    }
                }
            }

            tvChangeName.setOnClickListener {
                context?.run {
                    InputSheet().show(this) {
                        title(R.string.change_device_name)
                        with(InputEditText {
                            required()
                            label(getString(R.string.device_name_title))
                            hint(getString(R.string.my_buddy))
                        })
                        onNegative(R.string.cancel) {}
                        onPositive(R.string.ok) {
                            val name = it.getString("0")?: return@onPositive
                            changeDeviceName(name)
                        }
                    }
                }
            }

            tvFactory.setOnClickListener {
                context?.run {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.do_you_want_to_reset)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            lifecycleScope.launch {
                                if (BleManager.resetFactory()) {
                                    showToast(R.string.factory_reset_success)
                                } else {
                                    showToast(R.string.failed_to_reset)
                                }
                            }
                        }
                        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
                        .create()
                        .show()
                }
            }

            tvConnectOtherSand.setOnClickListener {
                lifecycleScope.launch {
                    BleManager.disconnect()
                    navigateTo(R.id.advance_to_setup)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect {
                render(it)
            }
        }
    }

    private fun changeDeviceName(name: String) {
        lifecycleScope.launch {
            if (BleManager.changeName(name)) {
                binding.tvName.text = getString(R.string.name, name)
                writePrefs(Prefs.Device, name)
                showToast(R.string.change_device_name_success)
            } else {
                showToast(R.string.failed_to_update_device_name)
            }
        }
    }

    private fun olderVersion(): Boolean {
        val curVersion = readPrefs(Prefs.Version, "Unknown")
        val remoteVersion = firmware?.version ?: return false
        return curVersion > remoteVersion
    }
}