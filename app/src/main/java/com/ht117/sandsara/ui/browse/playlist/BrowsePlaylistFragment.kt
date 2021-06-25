package com.ht117.sandsara.ui.browse.playlist

import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.map
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.*
import com.ht117.sandsara.createHolder
import com.ht117.sandsara.databinding.FragmentBrowseAllBinding
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.toTracks
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.library.playlist.detail.DetailPlaylistFragment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

data class BrowsePlaylistState(val data: Flow<PagingData<Playlist>>): IState

sealed class BrowsePlaylistAction: IAction {
    object BrowseAll: BrowsePlaylistAction()
    data class BrowseKey(val key: String): BrowsePlaylistAction()
}

class BrowsePlaylistFragment: BaseFragment(R.layout.fragment_browse_all), IView<BrowsePlaylistState> {

    private val viewModel: BrowsePlaylistViewModel by viewModel()
    private lateinit var binding: FragmentBrowseAllBinding
    private val adapter = PagingPlaylistAdapter { model ->
        navigateTo(R.id.navigate_to_playlist, bundleOf(DetailPlaylistFragment.KEY to model.playlist.playlistId))
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentBrowseAllBinding.bind(view)

        binding.rvData.adapter = adapter
        adapter.withLoadStateHeaderAndFooter(header = PagingStateAdapter(), footer = PagingStateAdapter())

        lifecycleScope.launch {
            flowOf(viewModel.state, adapter.loadStateFlow).flattenMerge(2).collect {
                if (it is BrowsePlaylistState) {
                    render(it)
                } else if (it is CombinedLoadStates) {
                    binding.loader.isVisible = (it.source.refresh is LoadState.Loading && adapter.itemCount == 0)

                    if ((it.source.refresh is LoadState.Error || it.source.refresh is LoadState.NotLoading) && adapter.itemCount == 0) {
                        binding.tvMessage.setText(R.string.not_found_track)
                        binding.tvNotFound.show()
                        binding.tvMessage.show()
                    } else {
                        binding.tvNotFound.hide()
                        binding.tvMessage.hide()
                    }
                }
            }

            viewModel.state.collect { render(it) }
        }
    }

    override fun initLogic() {
        super.initLogic()
        lifecycleScope.launch {
            viewModel.actions.emit(BrowsePlaylistAction.BrowseAll)
        }
    }

    fun sendKey(key: String?) {
        if (isVisible) {
            lifecycleScope.launch {
                if (key.isNullOrEmpty()) {
                    viewModel.actions.emit(BrowsePlaylistAction.BrowseAll)
                } else {
                    viewModel.actions.emit(BrowsePlaylistAction.BrowseKey(key))
                }
            }
        }
    }

    override fun render(state: BrowsePlaylistState) {
        lifecycleScope.launch {
            state.data.collectLatest {
                val models = it.map { playlist ->
                    PlaylistUiModel(playlist = playlist, cover = createHolder(requireContext(), playlist.toTracks()))
                }
                adapter.submitData(models)
            }
        }
    }
}