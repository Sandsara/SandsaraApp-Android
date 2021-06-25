package com.ht117.sandsara.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ActivityMainBinding
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.view.SandDevice
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val controller by lazy { findNavController(R.id.navHost) }

    private val flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()

        if (savedInstanceState == null) {
            setupNavigation()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupNavigation()
    }

    private fun setupView() {
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                window.decorView.systemUiVisibility = flag
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sandDevice.listener = object : SandDevice.IDeviceListener {
            override fun retry() {
                connectToDevice()
            }

            override fun showPlaying() {
                controller.navigate(R.id.playing)
            }
        }
    }

    private fun setupNavigation() {
        binding.run {
            controller.run {
                addOnDestinationChangedListener { _, destination, _ ->
                    Timber.d("Backstack $currentBackStackEntry")
                    when (destination.id) {
                        R.id.splash, R.id.discovery, R.id.sync_track, R.id.upgrade -> {
                            botNav.hide()
                            sandDevice.hide()
                        }
                        R.id.setup, R.id.playing -> {
                            sandDevice.hide()
                            botNav.show()
                        }
                        else -> {
                            botNav.show()
                            sandDevice.show()
                        }
                    }
                }

                NavigationUI.setupWithNavController(binding.botNav, this)
            }
        }
    }

    private fun connectToDevice() {
        if (!BleManager.hasConnection()) {
            if (!isBluetoothOn()) {
                turnOnBluetooth(RequestCode.REQUEST_BLE_ON)
                return
            }

            val savedAddress = read(Prefs.MacAddress, "")
            if (savedAddress.isNullOrEmpty()) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            RequestCode.REQUEST_LOC
                        )
                        return
                    }
                }
                controller.navigate(R.id.discovery)
            } else {
                lifecycleScope.launch {
                    BleManager.connect(savedAddress)
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
                controller.navigate(R.id.discovery)
            } else {
                Snackbar.make(
                    binding.botNav,
                    getString(R.string.permission_loc),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REQUEST_BLE_ON) {
            if (resultCode == RESULT_OK) {
                connectToDevice()
            } else {
                Toast.makeText(this, "You denied to turning on bluetooth", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.systemUiVisibility = flag
    }

    override fun onBackPressed() {
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (!BleManager.hasConnection()) {
                val address = read(Prefs.MacAddress, "")
                if (isBluetoothOn() && !address.isNullOrEmpty()) {
                    BleManager.connect(address)
                } else {
                    binding.sandDevice.disconnectState()
                }
            }
        }
    }

    override fun onPause() {
        if (!BleManager.isSyncing) {
            lifecycleScope.launch { BleManager.disconnect() }
        }
        super.onPause()
    }
}