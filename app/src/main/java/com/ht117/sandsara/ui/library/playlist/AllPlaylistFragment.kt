package com.ht117.sandsara.ui.library.playlist

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.PlaylistDetailAdapter
import com.ht117.sandsara.adapter.PlaylistUiModel
import com.ht117.sandsara.createHolder
import com.ht117.sandsara.databinding.FragmentAllPlaylistBinding
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.model.toTracks
import com.ht117.sandsara.ui.base.*
import com.ht117.sandsara.ui.library.playlist.detail.DetailPlaylistFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

data class PlaylistState(val playlist: Resource<List<Playlist>> = Resource.Idle()): IState

sealed class PlaylistAction: IAction {
    object LoadPlaylist: PlaylistAction()
}

class AllPlaylistFragment: BaseFragment(R.layout.fragment_all_playlist), IView<PlaylistState> {

    private lateinit var binding: FragmentAllPlaylistBinding
    private val viewModel: AllPlaylistViewModel by viewModel()

    private val adapter = PlaylistDetailAdapter { data, _ ->
        navigateTo(R.id.navigate_to_playlist, bundleOf(DetailPlaylistFragment.KEY to data.playlist.playlistId))
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentAllPlaylistBinding.bind(view).apply {
            rvPlaylist.adapter = adapter
        }

        lifecycleScope.run {
            launchWhenCreated { viewModel.state.collect { render(it) } }
            launchWhenStarted { viewModel.actions.emit(PlaylistAction.LoadPlaylist) }
        }
    }

    override fun render(state: PlaylistState) {
        handlePlaylists(state.playlist)
    }

    private fun handlePlaylists(resource: Resource<List<Playlist>>) {
        binding.run {
            lifecycleScope.launch {
                when (resource) {
                    is Resource.Stream -> {
                        loader.hide()
                        resource.stream.collect {
                            val models = it.map { playlist ->
                                PlaylistUiModel(playlist = playlist, cover = createHolder(requireContext(), playlist.toTracks()))
                            }
                            if (it.isEmpty() && adapter.itemCount == 0) {
                                ivEmpty.show()
                            } else {
                                ivEmpty.hide()
                            }
                            adapter.updateItems(models)
                        }
                    }
                    is Resource.Error -> {
                        loader.hide()
                        showToast(R.string.failed_to_load_all_playlist)
                    }
                    else -> {
                    }
                }
            }
        }
    }
}