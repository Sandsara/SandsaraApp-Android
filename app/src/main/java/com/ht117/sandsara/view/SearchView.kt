package com.ht117.sandsara.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ViewSearchBinding
import com.ht117.sandsara.ext.getLayoutInflater
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleOwner {

    private var isEditable = false
    private val lifecycleRegistry = LifecycleRegistry(this)
    private var binding: ViewSearchBinding =
        ViewSearchBinding.inflate(context.getLayoutInflater(), this, true)

    private var listener: ISearchListener? = null

    init {
        attrs?.run {
            val arr = context.obtainStyledAttributes(this, R.styleable.SearchView)
            isEditable = arr.getBoolean(R.styleable.SearchView_android_editable, true)
            arr.recycle()
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onDetachedFromWindow() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDetachedFromWindow()
    }

    fun getKey(): String {
        return binding.etInput.text.toString()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.apply {
            etInput.isEnabled = isEditable

            root.setOnClickListener {
                listener?.onTouch()
            }
            tvTitle.setOnClickListener {
                listener?.onTouch()
            }

            if (!isEditable) {
                tvTitle.show()
                etInput.hide()
            } else {
                tvTitle.hide()
                etInput.show()
                etInput.addTextChangedListener(object : TextWatcher {
                    private var text = ""

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val searchFor = s.toString().trim()
                        if (searchFor == text) {
                            return
                        }
                        text = searchFor

                        lifecycleScope.launch {
                            delay(300)
                            if (searchFor != text) {
                                return@launch
                            }
                            listener?.onChange(text)
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }

                })
            }
        }
    }

    fun addOnListener(listener: ISearchListener) {
        this.listener = listener
    }

    fun clearText() {
        binding.etInput.text.clear()
    }

    interface ISearchListener {
        fun onTouch()
        fun onChange(key: String)
    }
}