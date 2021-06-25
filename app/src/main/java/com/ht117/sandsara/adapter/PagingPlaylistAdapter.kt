package com.ht117.sandsara.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ht117.sandsara.databinding.ItemPagingBinding
import com.ht117.sandsara.model.Playlist

class PagingPlaylistAdapter(private val callback: ((PlaylistUiModel) -> Unit)? = null): PagingDataAdapter<PlaylistUiModel, PagingPlaylistAdapter.Holder>(differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemPagingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class Holder(val binding: ItemPagingBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PlaylistUiModel?) {
            item?.run {
                binding.root.setOnClickListener { callback?.invoke(this) }

                binding.ivThumb.load(cover)
                binding.tvAuthor.text = playlist.author
                binding.tvTitle.text = playlist.name
            }
        }
    }

    companion object {
        val differ = object: DiffUtil.ItemCallback<PlaylistUiModel>() {
            override fun areItemsTheSame(oldItem: PlaylistUiModel, newItem: PlaylistUiModel): Boolean {
                return oldItem.playlist.playlistId == newItem.playlist.playlistId
            }

            override fun areContentsTheSame(oldItem: PlaylistUiModel, newItem: PlaylistUiModel): Boolean {
               return oldItem.playlist.playlistId == newItem.playlist.playlistId
            }

        }
    }
}