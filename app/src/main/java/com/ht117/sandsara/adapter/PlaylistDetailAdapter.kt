package com.ht117.sandsara.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.ht117.sandsara.databinding.ItemPlaylistBinding
import com.ht117.sandsara.ext.getLayoutInflater
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.isTheSame

data class PlaylistUiModel(val playlist: Playlist, val cover: Bitmap? = null)

class PlaylistDetailAdapter(callback: (PlaylistUiModel, Int) -> Unit): BaseAdapter<PlaylistUiModel, PlaylistDetailAdapter.PlaylistHolder>(callback) {

    override fun getDiffUtils(
        oldItems: List<PlaylistUiModel>,
        newItems: List<PlaylistUiModel>
    ): DiffUtil.Callback {
        return object: BaseDiffer(oldItems, newItems) {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].playlist.playlistId == newItems[newItemPosition].playlist.playlistId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].playlist.isTheSame(newItems[newItemPosition].playlist)
            }

        }
    }

    override fun getItemId(position: Int): Long {
        return items[position].playlist.playlistId.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].playlist.playlistId.hashCode()
    }

    override fun getItemIndex(item: PlaylistUiModel): Int {
        items.forEachIndexed { index, playlist ->
            if (item.playlist.playlistId == playlist.playlist.playlistId) {
                return index
            }
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        binding = ItemPlaylistBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        return PlaylistHolder(binding as ItemPlaylistBinding)
    }

    inner class PlaylistHolder(binding: ItemPlaylistBinding): BaseHolder<PlaylistUiModel>(binding) {

        override fun bindData(data: PlaylistUiModel, position: Int, callback: ((PlaylistUiModel, Int) -> Unit)?) {
            super.bindData(data, position, callback)
            (binding as ItemPlaylistBinding).run {
                ivThumb.setImageBitmap(data.cover)
                tvTitle.text = data.playlist.name
                tvDesc.text = data.playlist.author
            }
        }
    }
}