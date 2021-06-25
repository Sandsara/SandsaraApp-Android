package com.ht117.sandsara.data

import androidx.room.*
import com.ht117.sandsara.model.Playlist
import com.ht117.sandsara.model.Track
import com.ht117.sandsara.ui.track.TrackAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface BaseDao<in T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItems(vararg items: T)

    @Delete
    suspend fun removeItem(item: T)

    @Update
    suspend fun updateItem(item: T)
}

@Dao
abstract class PlaylistDao: BaseDao<PlaylistEntity> {

    @Query("select count(*) from playlist")
    abstract fun getPlaylistCount(): Int

    @Query("select * from playlist")
    abstract fun getAll(): Flow<List<PlaylistEntity>>

    @Query("select * from playlist where isRecommended = 1")
    abstract suspend fun getRecommendList(): List<PlaylistEntity>

    @Query("select * from playlist where playlistId = :id")
    abstract suspend fun getPlaylistWithId(id: String): PlaylistEntity

    @Query("select * from playlist where playlistId = :id")
    abstract fun getPlaylistInfo(id: String): Flow<PlaylistEntity>

    @Query("select * from playlist where playlistId in (:ids)")
    abstract fun getPlaylistsWithIds(ids: List<String>): Flow<List<PlaylistEntity>>

    @Query("select * from playlist where playlistId != 'favorite'")
    abstract fun browseAll(): Flow<List<PlaylistEntity>>

    @Query("select * from playlist where title like '%' || :key || '%' or author like '%' || :key || '%'")
    abstract fun browseKey(key: String): Flow<List<PlaylistEntity>>

    @Query("update playlist set isDownloaded = :isDownload and isDeleted = 0 where playlistId = :playlistId")
    abstract fun updateDownload(playlistId: String, isDownload: Int)
}

@Dao
abstract class TrackDao: BaseDao<TrackEntity> {

    @Query("select count(*) from track")
    abstract fun getTrackCount(): Int

    @Query("select * from track")
    abstract suspend fun getAll(): List<TrackEntity>

    @Query("select * from track where isRecommended = 1")
    abstract suspend fun getRecommend(): List<TrackEntity>

    @Query("select * from track")
    abstract fun getAllTracks(): Flow<List<TrackEntity>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateTrack(track: TrackEntity)

    @Query("update track set isFavorite = :isFavorite where trackId = :trackId")
    abstract suspend fun updateTrackFavorite(trackId: String, isFavorite: Int)

    @Query("select * from track where trackId = :id")
    abstract fun getTrackWithId(id: String): Flow<TrackEntity>

    @Query("select * from track where trackId = :id")
    abstract suspend fun getTrack(id: String): TrackEntity?

    @Query("select * from track where trackId in (:ids)")
    abstract fun getTracksWithIds(ids: List<String>): Flow<List<TrackEntity>>

    @Query("select * from track where trackId in (:ids)")
    abstract suspend fun getTracksIds(ids: List<String>): List<TrackEntity>

    @Query("select * from track where isFavorite = 1")
    abstract fun getFavoritesFlow(): Flow<List<TrackEntity>>

    @Query("select * from track where name like '%' || :key || '%' or author like '%' || :key || '%'")
    abstract fun getTrackWithKey(key: String): Flow<List<TrackEntity>>
}

@Dao
interface PaletteDao: BaseDao<PaletteEntity> {

    @Query("select * from palette")
    suspend fun getAll(): List<PaletteEntity>
}