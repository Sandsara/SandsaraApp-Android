package com.ht117.sandsara.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ht117.sandsara.databinding.ItemPlayingTrackBinding
import com.ht117.sandsara.model.Track

typealias PlayingTrackCallback = (Track, Int) -> Unit

class PlayingTrackAdapter(private val callback: PlayingTrackCallback? = null)
    : RecyclerView.Adapter<PlayingTrackAdapter.PlayingTrackHolder>() {

    private val items = mutableListOf<Track>()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return items[position].trackId.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayingTrackHolder {
        val binding = ItemPlayingTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayingTrackHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayingTrackHolder, position: Int) {
        holder.bind(items[position], position)
    }

    fun update(newItems: List<Track>) {
        val differ = DiffUtil.calculateDiff(Differ(items, newItems))
        items.clear()
        items.addAll(newItems)
        differ.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = items.size

    inner class PlayingTrackHolder(val binding: ItemPlayingTrackBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track, position: Int) {
            binding.run {
                root.setOnClickListener { callback?.invoke(track, position) }
                ivThumb.load(track.images.firstOrNull()?.url)
                tvAuthor.text = track.author
                tvTitle.text = track.name
            }
        }

    }

    companion object {
        class Differ(private val oldItems: List<Track>,
                     private val newItems: List<Track>): DiffUtil.Callback() {
            override fun getOldListSize() = oldItems.size

            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                return oldItems[oldPos].trackId == newItems[newPos].trackId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(oldItemPosition, newItemPosition)
            }
        }
    }
}