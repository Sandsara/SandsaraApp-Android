package com.ht117.sandsara.ui.browse.track

import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.PagingStateAdapter
import com.ht117.sandsara.adapter.PagingTrackAdapter
import com.ht117.sandsara.databinding.FragmentBrowseAllBinding
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Mapper
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.track.TrackFragment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

data class BrowseTrackState(val data: Flow<PagingData<Track>>): IState

sealed class BrowseTrackAction: IAction {
    object BrowseAll: BrowseTrackAction()
    data class BrowseKey(val key: String): BrowseTrackAction()
}

class BrowseTrackFragment: BaseFragment(R.layout.fragment_browse_all), IView<BrowseTrackState> {

    private val viewModel: BrowseTrackViewModel by viewModel()
    private lateinit var binding: FragmentBrowseAllBinding

    private val adapter = PagingTrackAdapter { track ->
        val uiModel = Mapper.trackToTrackUiModel(requireContext(), track)
        navigateTo(R.id.navigate_to_track, bundleOf(TrackFragment.KEY to uiModel))
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentBrowseAllBinding.bind(view)

        binding.rvData.adapter = adapter
        adapter.withLoadStateFooter(PagingStateAdapter())
        lifecycleScope.launch {

            flowOf(viewModel.state, adapter.loadStateFlow).flattenMerge(2).collect {
                if (it is BrowseTrackState) {
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
        }
    }

    override fun initLogic() {
        super.initLogic()
        lifecycleScope.launch {
            viewModel.actions.emit(BrowseTrackAction.BrowseAll)
        }
    }

    override fun render(state: BrowseTrackState) {
        lifecycleScope.launch {
            state.data.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    fun sendKey(key: String?) {
        if (isResumed) {
            lifecycleScope.launch {
                if (key.isNullOrEmpty()) {
                    viewModel.actions.emit(BrowseTrackAction.BrowseAll)
                } else {
                    viewModel.actions.emit(BrowseTrackAction.BrowseKey(key))
                }
            }
        }
    }
}