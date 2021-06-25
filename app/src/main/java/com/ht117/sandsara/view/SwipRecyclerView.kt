package com.ht117.sandsara.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.swipe.SwipeHelper

class SwipRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var swipeCallback: ((Int) -> Unit)? = null

    private val itemTouchHelper = ItemTouchHelper(object: SwipeHelper(this) {
        override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
            return listOf(deleteButton(position))
        }
    })

    init {
        itemTouchHelper.attachToRecyclerView(this)
    }

    fun addSwipeCallback(callback: ((Int) -> Unit)? = null) {
        swipeCallback = callback
    }

    private fun deleteButton(position: Int): SwipeHelper.UnderlayButton {
        return SwipeHelper.UnderlayButton(context, context.getString(R.string.delete), 14.0f,
            android.R.color.holo_red_light, object : SwipeHelper.UnderlayButtonClickListener {
                override fun onClick() {
                    swipeCallback?.invoke(position)
                }
            })
    }
}