package com.ht117.sandsara.adapter

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ItemDetailTrackBinding
import com.ht117.sandsara.ext.checkTrackExisted
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Track
import kotlinx.parcelize.Parcelize
import timber.log.Timber


interface ITrackListener {
    fun onClickItem(track: TrackUiModel)
}

typealias TrackMode = Int

const val TrackMode_Unknown = 0
const val TrackMode_Download = 1
const val TrackMode_Downloading = 2
const val TrackMode_Ready = 3

@Parcelize
data class TrackUiModel(
    val track: Track,
    val downloadProgress: Int = -1,
    val isDownloaded: Boolean = false,
    val syncProgress: Int = -1,
    val isSynced: Boolean = false
) : Parcelable

class TrackDetailAdapter(private val context: Context,
                         var listener: ITrackListener? = null):
    RecyclerView.Adapter<TrackDetailAdapter.TrackDetailHolder>() {

    var mode: TrackMode = TrackMode_Unknown
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val items = mutableListOf<TrackUiModel>()

    init {
        setHasStableIds(true)
    }

    fun getItem(pos: Int) = items[pos]

    fun getItemIndex(item: TrackUiModel): Int {
        items.forEachIndexed { index, trackUiModel ->
            if (item.track.trackId == trackUiModel.track.trackId) {
                return index
            }
        }
        return -1
    }

    fun updateItem(item: TrackUiModel) {
        val index = getItemIndex(item)
        if (index != -1) {
            items[index] = item
            notifyItemChanged(index)
        }
    }

    fun updateItems(newItems: List<TrackUiModel>) {
        val result = DiffUtil.calculateDiff(DifferTrackUi(items, newItems))
        items.clear()
        items.addAll(newItems)
        result.dispatchUpdatesTo(this)
    }

    fun addAll(newItems: List<TrackUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getUnDownloadItems(): List<TrackUiModel> {
        return items.filter { !context.checkTrackExisted(it.track) }
    }

    override fun getItemId(position: Int): Long {
        return items[position].track.trackId.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].track.trackId.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackDetailHolder {
        val binding = ItemDetailTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackDetailHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackDetailHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.bindData(items[position], position)
    }

    override fun getItemCount() = items.size

    inner class TrackDetailHolder(val binding: ItemDetailTrackBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindData(
            data: TrackUiModel,
            position: Int
        ) {
            binding.run {
                val track = data.track
                ivThumb.load(track.images.firstOrNull()?.url ?: "") {
                    placeholder(R.drawable.ic_playlist)
                    error(R.drawable.ic_playlist)
                }

                tvAuthor.text = context.getString(R.string.by_author, track.author)
                tvTitle.text = track.name

                when (mode) {
                    TrackMode_Download -> {
                        handleDownloadState(data)
                    }
                    TrackMode_Downloading -> {
                        handleDownloadingState(data)
                    }
                    TrackMode_Ready -> {
                        handleReady()
                    }
                    else -> {
                        handleOther(data)
                    }
                }

                tvAuthor.setOnClickListener { listener?.onClickItem(data) }
                tvTitle.setOnClickListener { listener?.onClickItem(data) }
                ivThumb.setOnClickListener { listener?.onClickItem(data) }
            }
        }

        private fun handleDownloadState(model: TrackUiModel) {
            Timber.d("Download state")
            binding.run {
                nlvProgress.hide()
                if (!context.checkTrackExisted(model.track)) {
                    ivStatus.setColorFilter(Color.WHITE)
                    ivStatus.setImageResource(R.drawable.ic_download)
                    ivStatus.show()
                } else {
                    ivStatus.hide()
                }
            }
        }

        private fun handleDownloadingState(model: TrackUiModel) {
            Timber.d("Downloading state")
            binding.run {
                nlvProgress.show()
                ivStatus.hide()
                if (model.isDownloaded || model.downloadProgress == 100) {
                    nlvProgress.setProgress(100)
                    ivStatus.setColorFilter(Color.GREEN)
                    ivStatus.setImageResource(R.drawable.ic_success)
                    ivStatus.show()
                } else {
                    nlvProgress.setProgress(model.downloadProgress)
                    nlvProgress.show()
                }
            }
        }

        private fun handleReady() {
            Timber.d("Ready....")
            binding.run {
                ivStatus.hide()
                nlvProgress.hide()
            }
        }

        private fun handleOther(model: TrackUiModel) {
            binding.run {
                ivStatus.hide()
                nlvProgress.hide()
            }
        }
    }
}

class DifferTrackUi(private val oldItems: List<TrackUiModel>,
                    private val newItems: List<TrackUiModel>): DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldItems[oldPos].track.trackId.contentEquals(newItems[newPos].track.trackId)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldItems[oldItemPosition]
        val new = newItems[newItemPosition]

        return areItemsTheSame(oldItemPosition, newItemPosition)
                && old.isDownloaded == new.isDownloaded
                && old.downloadProgress == new.downloadProgress
    }

}