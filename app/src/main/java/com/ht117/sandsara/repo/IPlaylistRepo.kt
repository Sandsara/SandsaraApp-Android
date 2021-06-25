package com.ht117.sandsara.repo

import com.ht117.sandsara.model.PagingData
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.Track
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IPlaylistRepo {

    suspend fun loadAll()

    suspend fun loadRecommendPlaylists(): List<Playlist>

    suspend fun loadDownloadedPlaylist(): Flow<List<Playlist>>

    suspend fun addTrackToPlaylist(playlistId: String, track: Track)

    suspend fun removeTrackFromPlaylist(playlistId: String, track: Track)

    suspend fun getPlaylistWithId(playlistId: String): Playlist

    suspend fun loadTracksFromPlaylist(playlist: Playlist): Flow<List<Track>>

    suspend fun loadPlaylistInfo(playlistId: String): Flow<Playlist>

    suspend fun deletePlaylist(playlist: Playlist)

    suspend fun loadPlaylistWithoutTrack(trackId: String): Flow<List<Playlist>>

    suspend fun addNewPlaylist(playlist: Playlist)

    suspend fun downloadPlaylistFile(target: File, playlist: Playlist)

    suspend fun browseAll(): Flow<List<Playlist>>

    suspend fun browseKey(key: String): Flow<List<Playlist>>

    suspend fun updateDownload(playlist: Playlist, isDownloaded: Boolean)

    suspend fun browseRemote(key: String, offset: String?): PagingData<Playlist>
}