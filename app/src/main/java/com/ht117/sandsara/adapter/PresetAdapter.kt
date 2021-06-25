package com.ht117.sandsara.adapter

import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import coil.load
import coil.transform.CircleCropTransformation
import com.ht117.sandsara.databinding.ItemPresetBinding
import com.ht117.sandsara.ext.getLayoutInflater
import com.ht117.sandsara.model.Gradient
import com.ht117.sandsara.model.HslColor
import com.ht117.sandsara.model.toColors

class PresetAdapter(callback: ((Gradient, Int) -> Unit)?): BaseAdapter<Gradient,PresetAdapter.Holder>(callback) {

    override fun getDiffUtils(
        oldItems: List<Gradient>,
        newItems: List<Gradient>
    ): DiffUtil.Callback {
        return object: BaseDiffer(oldItems, newItems) {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition] == newItems[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(oldItemPosition, newItemPosition)
            }

        }
    }

    override fun getItemIndex(item: Gradient): Int {
        return items.indexOf(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemPresetBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        return Holder(binding)
    }

    inner class Holder(binding: ViewBinding): BaseHolder<Gradient>(binding) {

        override fun bindData(data: Gradient, position: Int, callback: ((Gradient, Int) -> Unit)?) {
            super.bindData(data, position, callback)
            (binding as ItemPresetBinding?)?.run {
                val drawable = GradientDrawable(data.direction, data.toColors())
                ivPreset.load(drawable) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
            }
        }
    }
}