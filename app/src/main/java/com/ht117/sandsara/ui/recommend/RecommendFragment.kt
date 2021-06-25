package com.ht117.sandsara.ui.recommend

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.PlaylistThumbAdapter
import com.ht117.sandsara.adapter.TrackThumbAdapter
import com.ht117.sandsara.databinding.FragmentRecommendBinding
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Mapper
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.library.playlist.detail.DetailPlaylistFragment
import com.ht117.sandsara.ui.track.TrackFragment
import com.ht117.sandsara.view.SearchView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

data class RecommendState(val isLoadingPlaylist: Boolean = false,
                          val playlists: List<Playlist>,
                          val messagePlaylist: String = "",
                          val isLoadingTrack: Boolean = false,
                          val tracks: List<Track>,
                          val messageTrack: String = ""): IState

sealed class RecommendAction: IAction {
    object Init: RecommendAction()
}

class RecommendFragment: BaseFragment(R.layout.fragment_recommend), IView<RecommendState> {

    private lateinit var binding: FragmentRecommendBinding
    private val viewModel: RecommendViewModel by viewModel()

    private var playlistAdapter = PlaylistThumbAdapter { data, _ ->
        navigateTo(R.id.recommend_to_playlist, bundleOf(DetailPlaylistFragment.KEY to data.playlistId))
    }
    private var trackAdapter = TrackThumbAdapter { data, _ ->
        val uiModel = Mapper.trackToTrackUiModel(requireContext(), data)
        navigateTo(R.id.navigate_to_track, bundleOf(TrackFragment.KEY to uiModel))
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentRecommendBinding.bind(view)
        binding.apply {
            rvPlaylist.adapter = playlistAdapter
            rvTracks.adapter = trackAdapter
            searchView.addOnListener(object: SearchView.ISearchListener {
                override fun onTouch() {
                    navigateTo(R.id.recommend_to_browse)
                }

                override fun onChange(key: String) {
                }

            })
        }

        lifecycleScope.run {
            launchWhenCreated {
                viewModel.state.collect { render(it) }
            }

            launchWhenResumed {
                if (trackAdapter.itemCount == 0 && playlistAdapter.itemCount == 0) {
                    viewModel.actions.emit(RecommendAction.Init)
                }
            }
        }
    }

    override fun cleanup() {
        binding.run {
            rvPlaylist.adapter = null
            rvTracks.adapter = null
        }
        super.cleanup()
    }

    override fun render(state: RecommendState) {
        handlePlaylists(state.isLoadingPlaylist, state.playlists)
        handleTracks(state.isLoadingTrack, state.tracks)
    }

    private fun handlePlaylists(isLoading: Boolean, playlists: List<Playlist>) {
        binding.run {
            if (isLoading) {
                tvPlaylistError.hide()
                loadingPlaylist.show()
            } else {
                loadingPlaylist.hide()
                playlistAdapter.updateItems(playlists)
                if (playlistAdapter.itemCount == 0) {
                    tvPlaylistError.show()
                } else {
                    tvPlaylistError.hide()
                }
            }
        }

    }

    private fun handleTracks(isLoading: Boolean, tracks: List<Track>) {
        binding.run {
            if (isLoading) {
                loadingTrack.show()
                tvTrackError.hide()
            } else {
                loadingTrack.hide()
                trackAdapter.updateItems(tracks)
                if (trackAdapter.itemCount == 0) {
                    tvTrackError.show()
                } else {
                    tvTrackError.hide()
                }
            }
        }
    }

}