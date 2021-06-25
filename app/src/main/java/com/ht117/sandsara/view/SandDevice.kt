package com.ht117.sandsara.view

import android.content.Context
import android.graphics.Typeface.BOLD
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.set
import androidx.lifecycle.*
import coil.load
import coil.transform.CircleCropTransformation
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ViewSandDeviceBinding
import com.ht117.sandsara.ext.Prefs
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.read
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SandDevice @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private var status = DeviceStatus()
    var listener: IDeviceListener? = null

    private val binding = ViewSandDeviceBinding.inflate(LayoutInflater.from(context), this, true)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        binding.run {

            root.setOnClickListener { showPlaying() }
            tvMessage.setOnClickListener { showPlaying() }
            ivInfo.setOnClickListener { showPlaying() }

            tvRetry.setOnClickListener {
                connecting()
                listener?.retry()
            }

            ivStatus.setOnClickListener {
                when (status.state) {
                    State.Disconnect, State.ConnectFailed -> {
                        connecting()
                        listener?.retry()
                    }
                    State.Playing -> {
                        lifecycleScope.launch {
                            BleManager.pauseDevice()
                        }
                    }
                    State.Paused -> {
                        lifecycleScope.launch {
                            BleManager.playOrResume()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        lifecycleScope.launch {
            if (visibility == VISIBLE) {
                BleManager.localStatus.flatMapMerge {
                    if (it.state == State.Connected || it.state == State.Playing || it.state == State.Paused) {
                        listOf(flowOf(it), BleManager.deviceStateFlow).asFlow().flattenMerge()
                    } else {
                        flowOf(it)
                    }
                }.collect {
                    when (it) {
                        is DeviceStatus -> {
                            handleState(it)
                            handleProgress(it.progress)
                        }
                        is ProgressStatus -> handleProgress(it.progress)
                        is Int -> BleManager.getDeviceStatus(it)
                    }
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    private fun showPlaying() {
        if (status.state == State.Playing || status.state == State.Paused) {
            listener?.showPlaying()
        }
    }

    private fun handleState(newState: DeviceStatus) {
        when (newState.state) {
            State.Busy -> busyState()
            State.Calibrate -> calibrateState()
            State.Disconnect -> disconnectState()
            State.ConnectFailed -> connectFailed()
            State.Connected -> connectedState()
            State.Sleep -> sleepState()
            State.Paused -> {
                playingTrack(newState.pos, true)
            }
            State.Playing -> {
                playingTrack(newState.pos)
            }
        }
        status = newState
    }

    private fun handleProgress(progress: Int) {
        binding.run {
            nlvProgress.setProgress(progress)

            if (status.state == State.Playing || status.state == State.Paused) {

                if (progress >= 0) {
                    nlvProgress.show()
                } else {
                    nlvProgress.hide()
                }

                if (progress >= 100) {
                    val pos = (status.pos + 1) % BleManager.playingTracks.size
                    status = status.copy(pos = pos)
                    playingTrack(pos)
                }
            }
        }
    }

    private fun calibrateState() {
        hideAll()
        binding.run {
            ivInfo.setImageResource(R.drawable.ic_exclamation)
            tvMessage.text = context.getString(R.string.device_calibrating)

            ivInfo.show()
            tvMessage.show()
        }
    }

    private fun sleepState() {
        hideAll()
        binding.run {
            ivInfo.setImageResource(R.drawable.ic_exclamation)
            tvMessage.text = context.getString(R.string.device_sleep)

            ivInfo.show()
            tvMessage.show()
        }
    }

    private fun busyState() {
        hideAll()
        binding.run {
            ivInfo.setImageResource(R.drawable.ic_exclamation)
            tvMessage.text = context.getString(R.string.device_busy)

            ivInfo.show()
            tvMessage.show()
        }
    }

    override fun onDetachedFromWindow() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDetachedFromWindow()
    }

    private fun connecting() {
        hideAll()
        binding.run {
            ivInfo.setImageResource(R.drawable.ic_exclamation)
            tvMessage.text = context.getString(R.string.connecting_to_sandsara)
            ivInfo.show()
            loader.show()
            tvMessage.show()
        }
    }

    fun disconnectState() {
        hideAll()
        binding.run {
            ivInfo.setImageResource(R.drawable.ic_exclamation)
            ivStatus.setImageResource(R.drawable.ic_retry)
            tvMessage.text = context.getString(R.string.no_device_found)

            loader.hide()
            ivInfo.show()
            ivStatus.show()
            tvMessage.show()
            tvRetry.show()
            botDivider.show()
            nlvProgress.hide()
        }
    }

    private fun connectedState() {
        hideAll()
        binding.run {
            ivInfo.setImageResource(R.drawable.ic_exclamation)
            tvMessage.text = "${context.getString(R.string.found_sandsara)} ${context.read(Prefs.Device, "SandSara")}"
            ivInfo.show()
            tvMessage.show()
            botDivider.show()
        }
    }

    private fun connectFailed() {
        hideAll()
        binding.run {
            ivStatus.setImageResource(R.drawable.ic_retry)
            ivInfo.setImageResource(R.drawable.ic_exclamation)
            tvMessage.text = context.getString(R.string.no_device_found)

            tvRetry.show()
            tvMessage.show()
            ivStatus.show()
            ivInfo.show()
            botDivider.show()
        }
    }

    private fun playingTrack(position: Int, isPause: Boolean = false) {
        hideAll()
        try {
            val tracks = BleManager.playingTracks
            val track = tracks[position - 1]
            binding.run {
                ivInfo.load(track.images.firstOrNull()?.url ?: "") {
                    error(R.drawable.ic_playlist)
                    transformations(CircleCropTransformation())
                }

                if (isPause) {
                    ivStatus.setImageResource(R.drawable.ic_play_track)
                } else {
                    ivStatus.setImageResource(R.drawable.ic_pause_mini)
                }
                val span = SpannableStringBuilder("${track.name} ${context.getString(R.string.by_author, track.author)}")
                span[0, track.name.length] = StyleSpan(BOLD)
                tvMessage.text = span

                nlvProgress.show()
                ivInfo.show()
                tvMessage.show()
                ivStatus.show()
            }
        } catch (exp: Exception) {
            Timber.d("Exception ${exp.message}")
        }
    }

    private fun hideAll() {
        binding.run {
            ivInfo.hide()
            tvMessage.hide()
            ivStatus.hide()
            loader.hide()
            tvRetry.hide()
            nlvProgress.hide()
        }
    }

    interface IDeviceListener {
        fun showPlaying()
        fun retry()
    }
}