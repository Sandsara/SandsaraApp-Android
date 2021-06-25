package com.ht117.sandsara.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ht117.sandsara.R
import com.ht117.sandsara.ext.read
import com.ht117.sandsara.ext.write

abstract class BaseFragment(val layout: Int) : Fragment(layout) {

    open fun initView(view: View) {}
    open fun initLogic() {}
    open fun cleanup() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onResume() {
        super.onResume()
        initLogic()
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    fun navigateTo(destination: Int, args: Bundle? = null, options: NavOptions? = null) {
        findNavController().navigate(destination, args, options)
    }

    fun navigateBack() {
        val lastEntry = findNavController().previousBackStackEntry
        findNavController().popBackStack()
        if (lastEntry?.destination?.id == R.id.sync_track) {
            findNavController().popBackStack()
        }
    }

    fun showToast(message: String) {
        view?.run {
            Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun showToast(resId: Int) {
        view?.run {
            Snackbar.make(this, getString(resId), Snackbar.LENGTH_SHORT).show()
        }
    }

    fun addFlag(flag: Int) {
        activity?.window?.addFlags(flag)
    }

    inline fun<reified T> readPrefs(key: String, defValue: T): T {
        return requireContext().read(key, defValue)
    }

    inline fun<reified T> writePrefs(key: String, value: T) {
        requireContext().write(key, value)
    }
}
