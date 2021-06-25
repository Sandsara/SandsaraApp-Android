package com.ht117.sandsara.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ht117.sandsara.databinding.ItemPagingBinding
import com.ht117.sandsara.model.Track

class PagingTrackAdapter(private val callback: ((Track) -> Unit)? = null): PagingDataAdapter<Track, PagingTrackAdapter.Holder>(differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemPagingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class Holder(val binding: ItemPagingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Track?) {
            item?.run {
                binding.root.setOnClickListener { callback?.invoke(this) }

                binding.ivThumb.load(images.firstOrNull()?.url)
                binding.tvAuthor.text = author
                binding.tvTitle.text = name
            }
        }

    }

    companion object {
        val differ = object: DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem.trackId == newItem.trackId
            }

            override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem.trackId == newItem.trackId
            }

        }
    }
}