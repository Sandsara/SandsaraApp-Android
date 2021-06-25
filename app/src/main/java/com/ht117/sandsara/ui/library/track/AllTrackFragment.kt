package com.ht117.sandsara.ui.library.track

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.ITrackListener
import com.ht117.sandsara.adapter.TrackDetailAdapter
import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.databinding.FragmentAllTrackBinding
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Mapper
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.track.TrackFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

data class AllTrackState(val trackRes: Resource<List<Track>> = Resource.Idle()): IState

sealed class AllTrackAction: IAction {
    object LoadAllTracks: AllTrackAction()
    data class UpdateTrack(val track: Track): AllTrackAction()
}

class AllTrackFragment: BaseFragment(R.layout.fragment_all_track), IView<AllTrackState> {

    private lateinit var binding: FragmentAllTrackBinding
    private val adapterCallback = object: ITrackListener {
        override fun onClickItem(track: TrackUiModel) {
            navigateTo(R.id.navigate_to_track, bundleOf(TrackFragment.KEY to track))
        }
    }

    private val adapter by lazy { TrackDetailAdapter(requireContext()) }
    private val viewModel: AllTrackViewModel by viewModel()

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentAllTrackBinding.bind(view)
        binding.run {
            rvAllTracks.itemAnimator = null
            adapter.listener = adapterCallback
            rvAllTracks.adapter = adapter
        }

        lifecycleScope.run {
            launchWhenCreated { viewModel.state.collect { render(it) } }

            launchWhenStarted { viewModel.actions.emit(AllTrackAction.LoadAllTracks) }
        }
    }

    override fun initLogic() {
        super.initLogic()
        lifecycleScope.launch {
            viewModel.actions.emit(AllTrackAction.LoadAllTracks)
        }
    }

    override fun render(state: AllTrackState) {
        binding.run {
            when (state.trackRes) {
                is Resource.Loading -> {
                    if (adapter.itemCount == 0) {
                        tvEmpty.hide()
                        loader.show()
                    } else {}
                }
                is Resource.Stream -> {
                    lifecycleScope.launch {
                        state.trackRes.stream.collect { tracks ->
                            val models = Mapper.tracksToTrackUiModels(requireContext(), tracks)
                            loader.hide()
                            adapter.updateItems(models)

                            if (tracks.isEmpty() && adapter.itemCount == 0) {
                                tvEmpty.show()
                            } else {
                                tvEmpty.hide()
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    loader.hide()
                    tvEmpty.show()
                    showToast(R.string.failed_to_load_all_tracks)
                }
                else -> {}
            }
        }
    }

}