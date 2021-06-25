package com.ht117.sandsara.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.ht117.sandsara.databinding.ItemPlaylistSelectBinding
import com.ht117.sandsara.ext.getLayoutInflater
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.isTheSame

class SelectPlaylistAdapter (callback: (Playlist, Int) -> Unit): BaseAdapter<Playlist, SelectPlaylistAdapter.SelectPlaylistHolder>(callback) {

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

    override fun getItemIndex(item: Playlist): Int {
        items.forEachIndexed { index, playlist ->
            if (item.playlistId == playlist.playlistId) {
                return index
            }
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectPlaylistHolder {
        binding = ItemPlaylistSelectBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        return SelectPlaylistHolder(binding as ItemPlaylistSelectBinding)
    }

    inner class SelectPlaylistHolder(binding: ItemPlaylistSelectBinding): BaseHolder<Playlist>(binding) {

        override fun bindData(data: Playlist, position: Int, callback: ((Playlist, Int) -> Unit)?) {
            (binding as ItemPlaylistSelectBinding).run {
                loadResource(data.images.firstOrNull()?.url?: "", target = ivThumb)

                tvTitle.text = data.name
                tvDesc.text = data.author

                root.setOnClickListener {
                    callback?.invoke(data, position)
                }
            }
        }
    }
}