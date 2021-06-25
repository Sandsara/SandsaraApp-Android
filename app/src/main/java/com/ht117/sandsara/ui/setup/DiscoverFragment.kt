package com.ht117.sandsara.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.DeviceAdapter
import com.ht117.sandsara.databinding.FragmentDiscoverBinding
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.ui.base.BaseFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class DiscoverFragment: BaseFragment(R.layout.fragment_discover) {

    private lateinit var binding: FragmentDiscoverBinding

    private val adapter = DeviceAdapter { data, _ ->
        lifecycleScope.launch {
            BleManager.stopScan()
            binding.tvTitle.text = getString(R.string.connecting)
            val result = BleManager.connect(data.address)
            if (result) {
                writePrefs(Prefs.MacAddress, data.address)
                writePrefs(Prefs.Device, data.name)
                close()
            } else {
                showToast(R.string.failed_to_connect)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            if (!requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RequestCode.REQUEST_LOC
                )
                return@launch
            } else {
                if (!isBluetoothOn()) {
                    turnOnBluetooth(RequestCode.REQUEST_BLE_ON)
                    return@launch
                } else {
                    BleManager.scanDevices()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RequestCode.REQUEST_LOC) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isBluetoothOn()) {
                    turnOnBluetooth(RequestCode.REQUEST_BLE_ON)
                } else {
                    lifecycleScope.launch {
                        BleManager.scanDevices()
                    }
                }
            } else {
                showToast(getString(R.string.permission_loc))
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentDiscoverBinding.bind(view)
        binding.apply {
            rvDevices.adapter = adapter
            ivClose.setOnClickListener {
                close()
            }
        }
    }

    private fun close() {
        try {
            lifecycleScope.launch { BleManager.stopScan() }
            val id = findNavController().previousBackStackEntry?.destination?.id
            if (id == R.id.advance_setting) {
                navigateBack()
            } else {
                navigateTo(R.id.discovery_to_home)
            }
        } catch (exp: Exception) {
            Timber.d("Failed ${exp.message}")
        }
    }

    override fun initLogic() {
        super.initLogic()
        lifecycleScope.launch {
            BleManager.scanDevices().collect {
                adapter.appendDevice(it)
            }
        }
    }

    override fun onPause() {
        lifecycleScope.launch {
            BleManager.stopScan()
        }
        super.onPause()
    }

}