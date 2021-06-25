package com.ht117.sandsara.ui.track.addtoplaylist

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ht117.sandsara.R
import com.ht117.sandsara.adapter.SelectPlaylistAdapter
import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.databinding.FragmentAddToPlaylistBinding
import com.ht117.sandsara.ext.hide
import com.ht117.sandsara.ext.show
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.ui.base.*
import com.maxkeppeler.bottomsheets.input.InputSheet
import com.maxkeppeler.bottomsheets.input.type.InputEditText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

data class AddToPlaylistState(
    val curPlaylist: Resource<List<Playlist>> = Resource.Idle(),
    val addStatus: Resource<Boolean> = Resource.Idle()
) : IState

sealed class AddPlaylistAction : IAction {
    data class LoadAllPlaylist(val trackId: String) : AddPlaylistAction()
    data class AddTracksToPlaylist(val ids: String, val track: Track) : AddPlaylistAction()
    data class CreateNewPlaylist(val name: String, val track: Track) : AddPlaylistAction()
}

class AddToPlaylistFragment : BaseFragment(R.layout.fragment_add_to_playlist),
    IView<AddToPlaylistState> {

    private lateinit var binding: FragmentAddToPlaylistBinding
    private val viewModel: AddToPlaylistViewModel by viewModel()
    private val curTrack by lazy { arguments?.get("track") as TrackUiModel }

    private val adapter = SelectPlaylistAdapter { data, _ ->
        lifecycleScope.launch {
            viewModel.actions.emit(
                    AddPlaylistAction.AddTracksToPlaylist(
                            data.playlistId,
                            curTrack.track
                    )
            )
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        binding = FragmentAddToPlaylistBinding.bind(view).apply {
            rvPlaylist.adapter = adapter

            ivCreate.setOnClickListener {
                context?.run {
                    InputSheet().show(this) {
                        title(R.string.create_new_playlist)
                        with(InputEditText {
                            required()
                            label("Your playlist name:")
                            hint("Abc")
                        })
                        onNegative(R.string.cancel) {}
                        onPositive(R.string.ok) {
                            val name = it.getString("0")?: return@onPositive
                            addNewPlaylist(name)
                        }
                    }
                }
            }

            ivBack.setOnClickListener { navigateBack() }
        }

        lifecycleScope.run {
            launchWhenCreated { viewModel.state.collect { render(it) } }
            launchWhenStarted { viewModel.actions.emit(AddPlaylistAction.LoadAllPlaylist(curTrack.track.trackId)) }
        }
    }

    override fun render(state: AddToPlaylistState) {
        handleCurPlaylist(state.curPlaylist)
        handleStatusAdd(state.addStatus)
    }

    private fun addNewPlaylist(name: String) {
        lifecycleScope.launch {
            viewModel.actions.emit(AddPlaylistAction.CreateNewPlaylist(name, curTrack.track))
        }
    }

    private fun handleCurPlaylist(resource: Resource<List<Playlist>>) {
        (binding as FragmentAddToPlaylistBinding?)?.run {
            lifecycleScope.launch {
                when (resource) {
                    is Resource.Loading -> {
                        loader.show()
                    }

                    is Resource.Stream -> {
                        loader.hide()
                        resource.stream.collect {
                            Timber.d("Found playlist $it")
                            adapter.updateItems(it)
                        }
                    }
                    is Resource.Error -> {
                        loader.hide()
                        showToast("Failed to load playlist")
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun handleStatusAdd(resource: Resource<Boolean>) {
        binding.run {
            when (resource) {
                is Resource.Success -> {
                    showToast("Add track successfully")
                    navigateBack()
                }
                is Resource.Error -> {
                    showToast("Failed to add track")
                }
                else -> {
                }
            }
        }
    }
}
