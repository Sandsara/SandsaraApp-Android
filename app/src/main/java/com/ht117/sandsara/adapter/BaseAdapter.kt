package com.ht117.sandsara.adapter

import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.ht117.sandsara.R
import timber.log.Timber

abstract class BaseAdapter<T, VH: BaseHolder<T>>(private var callback: ((T, Int) -> Unit)? = null): RecyclerView.Adapter<VH>() {

    protected lateinit var binding: ViewBinding
    protected val items = mutableListOf<T>()

    fun getAll() = items
    
    fun getItem(position: Int) = items[position]

    abstract fun getDiffUtils(oldItems: List<T>, newItems: List<T>): DiffUtil.Callback

    abstract fun getItemIndex(item: T): Int

    open fun addAll(newItems: List<T>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    open fun updateItems(newItems: List<T>) {
        val result = DiffUtil.calculateDiff(getDiffUtils(items, newItems))
        items.clear()
        items.addAll(newItems)
        result.dispatchUpdatesTo(this)
    }

    open fun updateItem(item: T) {
        val index = getItemIndex(item)
        if (index != -1) {
            items[index] = item
            notifyItemChanged(index)
        }
    }

    fun clearAll() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bindData(items[position], position, callback)
    }

    override fun getItemCount() = items.size

    fun loadResource(resource: String, defError: Int = R.drawable.ic_playlist, target: ImageView) {
        target.load(resource) {
            placeholder(R.drawable.ic_playlist)
            error(defError)
        }
    }

    abstract inner class BaseDiffer(private val oldItems: List<T>, private val newItems: List<T>): DiffUtil.Callback() {
        override fun getNewListSize() = newItems.size
        override fun getOldListSize() = oldItems.size
    }
}

abstract class BaseHolder<T>(private val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {

    open fun bindData(data: T, position: Int, callback: ((T, Int) -> Unit)?) {
        binding.root.setOnClickListener {
            callback?.invoke(data, position)
        }
    }
}