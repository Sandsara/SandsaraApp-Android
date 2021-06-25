package com.ht117.sandsara.repo

import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.ext.SyncTrackModel
import com.ht117.sandsara.model.PagingData
import com.ht117.sandsara.model.Track
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ITrackRepo {

    suspend fun loadAll()

    suspend fun browseAll(): Flow<List<Track>>

    suspend fun loadRecommendTracks(): List<Track>

    suspend fun downloadTrack(file: File, track: TrackUiModel): Flow<SyncTrackModel>

    suspend fun loadDownloadedTrack(): Flow<List<Track>>

    suspend fun getTrackWithId(trackId: String): Flow<Track>

    suspend fun getTrack(track: Track): Flow<Track>

    suspend fun updateTrackFavorite(track: Track)

    suspend fun loadFavoriteTracks(): Flow<List<Track>>

    suspend fun getTrackFromFile(fileName: String): Track

    suspend fun browseKey(key: String): Flow<List<Track>>

    suspend fun browseRemote(key: String, offset: String?): PagingData<Track>
}