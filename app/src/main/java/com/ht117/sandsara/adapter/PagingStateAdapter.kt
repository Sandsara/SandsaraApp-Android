package com.ht117.sandsara.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ItemLoadingStateBinding

class PagingStateAdapter: LoadStateAdapter<PagingStateAdapter.LoadStateHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateHolder {
        val binding = ItemLoadingStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadStateHolder(binding)
    }

    override fun onBindViewHolder(holder: LoadStateHolder, loadState: LoadState) {
        holder.bindState(loadState)
    }

    class LoadStateHolder(val binding: ItemLoadingStateBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindState(state: LoadState) {
            if (state is LoadState.Error) {
                binding.tvMessage.setText(R.string.failed_to_load)
            }
            binding.run {
                loader.isVisible = state is LoadState.Loading
                ivRetry.isVisible = state is LoadState.Error
                tvMessage.isVisible = state is LoadState.Error
            }

        }
    }
}