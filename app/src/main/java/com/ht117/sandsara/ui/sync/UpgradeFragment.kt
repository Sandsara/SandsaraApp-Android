package com.ht117.sandsara.ui.sync

import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.FragmentSyncBinding
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.model.SandFile
import com.ht117.sandsara.ui.base.BaseFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UpgradeFragment: BaseFragment(R.layout.fragment_sync) {

    private lateinit var binding: FragmentSyncBinding
    private val firmware by lazy { arguments?.get(KEY_FIRMWARE) as String }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentSyncBinding.bind(view)
        addFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lifecycleScope.launch {
            binding.tvInfo.text = getString(R.string.upgrading)

            val file = requireContext().getFileWithName(firmware, FirmwarePath)

            if (file != null) {
                BleManager.sendFileWithProgress(file).collect {
                    when (it) {
                        SyncFileProgress.Starting -> {
                            binding.syncProgress.show()
                        }
                        is SyncFileProgress.Progress -> {
                            binding.syncProgress.setProgress(it.progress)
                        }
                        is SyncFileProgress.Finished -> {
                            binding.syncProgress.hide()
                            BleManager.restart()
                            showToast(getString(R.string.upgrade_successfully))
                        }
                        is SyncFileProgress.Error -> {
                            binding.syncProgress.hide()
                            showToast(getString(R.string.upgrade_failed))
                        }
                    }
                }
            } else {
                showToast(R.string.sync_failed)
                navigateBack()
            }
        }
    }

    companion object {
        const val KEY_FIRMWARE = "firmware"
    }
}