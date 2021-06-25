package com.ht117.sandsara.ext

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.view.LayoutInflater
import androidx.core.content.ContextCompat

fun Int.toByteArray(): ByteArray {
    return toString().toByteArray(Charsets.US_ASCII)
}

fun Float.toByteArray(): ByteArray {
    return toString().toByteArray(Charsets.US_ASCII)
}

fun Context.getLayoutInflater(): LayoutInflater {
    return LayoutInflater.from(this)
}

fun Context.dpToPx(dp: Float): Float {
    return dp * resources.displayMetrics.density
}

fun Context.pxToDp(px: Float): Float {
    return px / resources.displayMetrics.density
}

fun Context.isSupportBle(): Boolean {
    return BluetoothAdapter.getDefaultAdapter() != null &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
}

fun Context.isGpsOn(): Boolean {
    val locMan = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.getWidth() = resources.displayMetrics.widthPixels

fun Context.getHeight() = resources.displayMetrics.heightPixels

fun String?.convertToInt(defValue: Int = 0): Int {
    return if (this == null) {
        defValue
    } else {
        try {
            this.toInt()
        } catch (exp: Exception) {
            defValue
        }
    }
}