package com.ht117.sandsara.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.ht117.sandsara.databinding.ItemThumbBinding
import com.ht117.sandsara.ext.getLayoutInflater
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.model.isTheSame

class TrackThumbAdapter(callback: (Track, Int) -> Unit): BaseAdapter<Track, TrackThumbAdapter.Holder>(callback) {

    override fun getDiffUtils(oldItems: List<Track>, newItems: List<Track>): DiffUtil.Callback {
        return object: BaseDiffer(oldItems, newItems) {
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].isTheSame(newItems[newItemPosition])
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].trackId == newItems[newItemPosition].trackId
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return items[position].trackId.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].trackId.hashCode()
    }

    override fun getItemIndex(item: Track): Int {
        items.forEachIndexed { index, track ->
            if (item.trackId == track.trackId) {
                return index
            }
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemThumbBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        return Holder(binding)
    }

    inner class Holder(binding: ViewBinding): BaseHolder<Track>(binding) {
        override fun bindData(data: Track, position: Int, callback: ((Track, Int) -> Unit)?) {
            super.bindData(data, position, callback)
            (binding as ItemThumbBinding).run {
                ivThumb.setImageDrawable(null)
                loadResource(data.images.firstOrNull()?.url?: "", target = ivThumb)
            }
        }
    }
}