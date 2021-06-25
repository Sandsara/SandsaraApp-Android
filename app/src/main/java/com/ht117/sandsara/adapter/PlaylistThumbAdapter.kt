package com.ht117.sandsara.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ItemThumbBinding
import com.ht117.sandsara.ext.getLayoutInflater
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.isTheSame

class PlaylistThumbAdapter(callback: (Playlist, Int) -> Unit): BaseAdapter<Playlist, PlaylistThumbAdapter.Holder>(callback) {

    override fun getDiffUtils(
        oldItems: List<Playlist>,
        newItems: List<Playlist>
    ): DiffUtil.Callback {
        return object: BaseDiffer(oldItems, newItems) {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].playlistId == newItems[newItemPosition].playlistId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].isTheSame(newItems[newItemPosition])
            }

        }
    }

    override fun getItemId(position: Int): Long {
        return items[position].playlistId.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].playlistId.hashCode()
    }

    override fun getItemIndex(item: Playlist): Int {
        items.forEachIndexed { index, playlist ->
            if (item.playlistId == playlist.playlistId) {
                return index
            }
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemThumbBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        return Holder(binding)
    }

    inner class Holder(binding: ViewBinding) : BaseHolder<Playlist>(binding) {
        override fun bindData(data: Playlist, position: Int, callback: ((Playlist, Int) -> Unit)?) {
            super.bindData(data, position, callback)
            (binding as ItemThumbBinding).run {
                loadResource(data.images.firstOrNull()?.url?: "", R.drawable.ic_playlist, ivThumb)
            }
        }
    }
}