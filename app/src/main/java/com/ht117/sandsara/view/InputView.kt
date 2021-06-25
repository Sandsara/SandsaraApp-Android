package com.ht117.sandsara.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ViewInputBinding

class InputView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var hint: String = ""
    private lateinit var binding: ViewInputBinding

    init {
        attrs?.run {
            val arr = context.obtainStyledAttributes(this, R.styleable.InputView)
            hint = arr.getString(R.styleable.InputView_ip_hint)?: ""
        }

        inflate(context, R.layout.view_input, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = ViewInputBinding.bind(this)
    }
}