package com.ht117.sandsara.ext

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

object RequestCode {
    const val REQUEST_BLE_ON = 101
    const val REQUEST_LOC = 102
}

fun isBluetoothOn() = BluetoothAdapter.getDefaultAdapter()?.isEnabled?: false

fun Activity.turnOnBluetooth(requestCode: Int) {
    BluetoothAdapter.getDefaultAdapter()?.run {
        if (!isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, requestCode)
        }
    }
}

fun Fragment.turnOnBluetooth(requestCode: Int) {
    BluetoothAdapter.getDefaultAdapter()?.run {
        if (!isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, requestCode)
        }
    }
}

fun Context.isConnectedTo(address: String): Boolean {
    if (!BluetoothAdapter.checkBluetoothAddress(address)) return false

    val blueManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return blueManager.getConnectedDevices(BluetoothProfile.GATT).find { it.address == address } != null
}