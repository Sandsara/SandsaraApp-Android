package com.ht117.sandsara.repo

import android.content.Context
import com.ht117.sandsara.buildFormula
import com.ht117.sandsara.data.*
import com.ht117.sandsara.ext.*
import com.ht117.sandsara.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class PlaylistRepoImpl(private val client: HttpClient,
                       private val db: SandSaraDb,
                       private val context: Context): IPlaylistRepo {

    private val playlistDao by lazy { db.playlistDao() }
    private val trackDao by lazy { db.trackDao() }

    override suspend fun loadAll() {
        return withContext(Dispatchers.IO) {
            try {
                val count = playlistDao.getPlaylistCount()

                if (count == 1) {
                    val response = client.get<BaseResponsePlaylist> {
                        url { encodedPath = "/playlist?view=all" }
                    }
                    val models = response.records.map { it.playlist.copy(playlistId = it.id) }
                    playlistDao.addItems(*Mapper.playlistsToEntities(models).toTypedArray())
                }

            } catch (exp: Exception) {
                Timber.d("Exception ${exp.message}")
                exp.printStackTrace()
            }
        }
    }

    override suspend fun browseAll(): Flow<List<Playlist>> {
        return playlistDao.browseAll().map { Mapper.entitiesToPlaylists(it).sortedBy { playlist -> playlist.name } }
    }

    override suspend fun browseKey(key: String): Flow<List<Playlist>> {
        return playlistDao.browseKey(key).map { Mapper.entitiesToPlaylists(it) }
    }

    override suspend fun browseRemote(key: String, offset: String?): PagingData<Playlist> {
        val formula = buildFormula(key)
        val response = client.get<BaseResponsePlaylist> {
            url { encodedPath = "/playlist" }
            parameter("pageSize", "20")
            if (offset != null) {
                parameter("offset", offset)
            }
            if (formula.isNotEmpty()) {
                parameter("filterByFormula", formula)
            }
            parameter("sort[0][field]", "name")
            parameter("sort[0][direction]", "asc")
        }
        val data = response.records.map { it.playlist.copy(playlistId = it.id) }
        return PagingData(data, response.offset)
    }

    override suspend fun updateDownload(playlist: Playlist, isDownloaded: Boolean) = withContext(Dispatchers.IO) {
        val entity = playlist.toEntity().copy(isDownloaded = isDownloaded)
        if (!isDownloaded) {
            playlistDao.updateItem(entity)
        } else {
            playlistDao.updateItem(entity.copy(isDeleted = false))
        }
    }

    override suspend fun loadRecommendPlaylists() = withContext(Dispatchers.IO) {
        try {
            val remote = try {
                val response = client.get<BaseResponsePlaylist> {
                    url { encodedPath = "/playlist?view=recommended" }
                }

                response.records.map { it.playlist.copy(playlistId = it.id) }
            } catch (exp: Exception) {
                Timber.d("Failed to load remote recommend playlist ${exp.message}")
                emptyList()
            }

            if (!remote.isNullOrEmpty()) {
                remote
            } else {
                playlistDao.getRecommendList().map { it.toPlaylist() }
            }
        } catch (exp: Exception) {
            Timber.d("Failed to load ${exp.message}")
            emptyList()
        }
    }

    override suspend fun loadDownloadedPlaylist(): Flow<List<Playlist>> {
        return playlistDao.getAll().map { Mapper.entitiesToPlaylists(it) }
            .map { it.filter { playlist ->
                (allTrackDownloaded(playlist) || isFavoritePlaylist(playlist)) && !playlist.isDelete
            }}
    }

    override suspend fun downloadPlaylistFile(file: File, playlist: Playlist) {
        try {
            val rawClient = NetworkService.customClient()
            val fos = file.outputStream()

            val sandFile = playlist.file.first()

            val bytes = rawClient.get<ByteReadChannel>(sandFile.url)
            val bufSize = 4096
            val buffer = ByteArray(bufSize)

            while (true) {
                val result = bytes.readAvailable(buffer, 0, bufSize)
                if (result == -1) {
                    break
                }
                fos.write(buffer)
            }
            fos.close()
        } catch (exp: Exception) {
            Timber.d("Failed to download playlist ${exp.message}")
        }
    }

    override suspend fun getPlaylistWithId(playlistId: String): Playlist {
        return playlistDao.getPlaylistWithId(playlistId).toPlaylist()
    }

    override suspend fun addTrackToPlaylist(playlistId: String, track: Track) = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistWithId(playlistId).toPlaylist()
        val trackIds = playlist.trackIds.toMutableList()
        trackIds.add(track.trackId)

        val trackFiles = playlist.trackFiles.toMutableList()
        trackFiles.add(track.sandFiles.first())

        val names = playlist.names.toMutableList()
        names.add(track.name)

        val images = playlist.images.toMutableList()
        images.add(track.images.first())

        val authors = playlist.authors.toMutableList()
        authors.add(track.author)

        val tracks = playlist.tracks + 1

        val newPlaylist = playlist.copy(trackIds = trackIds, trackFiles = trackFiles, names = names, images = images, authors = authors, tracks = tracks)
        playlistDao.updateItem(newPlaylist.toEntity())

        val name = track.sandFiles.first().filename
        val file = context.getFileWithName(playlist.name, PlaylistPath)?: context.createTemp(playlist.name, PlaylistPath)
        file.appendBytes("$name\r\n".toByteArray())
    }

    override suspend fun removeTrackFromPlaylist(playlistId: String, track: Track) = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistWithId(playlistId).toPlaylist()
        val trackIds = playlist.trackIds.toMutableList()
        trackIds.remove(track.trackId)

        val trackFiles = playlist.trackFiles.toMutableList()
        trackFiles.remove(track.sandFiles.first())

        val names = playlist.names.toMutableList()
        names.remove(track.name)

        val images = playlist.images.toMutableList()
        images.remove(track.images.first())

        val authors = playlist.authors.toMutableList()
        authors.remove(track.author)

        val tracks = Math.max(playlist.tracks - 1, 0)

        val newPlaylist = playlist.copy(trackIds = trackIds, trackFiles = trackFiles, names = names, images = images, authors = authors, tracks = tracks)
        playlistDao.updateItem(newPlaylist.toEntity())
    }

    private fun allTrackDownloaded(playlist: Playlist): Boolean {
        try {
            if (playlist.trackFiles.isEmpty() || !playlist.isDownloaded) return false
            playlist.trackFiles.forEach {
                if (!context.checkTrackExisted(it.filename)) {
                    return false
                }
            }
            return true
        } catch (exp: Exception) {
            return false
        }
    }

    private fun isFavoritePlaylist(playlist: Playlist): Boolean {
        return playlist.playlistId == "favorite" && playlist.trackIds.isNotEmpty()
    }

    override suspend fun loadTracksFromPlaylist(playlist: Playlist): Flow<List<Track>> {
        Timber.d("Retrieving playlist $playlist")
        val info = playlistDao.getPlaylistWithId(playlist.playlistId)

        return trackDao.getTracksWithIds(info.toPlaylist().trackIds).map { Mapper.entitiesToTracks(it) }
    }

    override suspend fun loadPlaylistInfo(playlistId: String): Flow<Playlist> = withContext(Dispatchers.IO) {
        playlistDao.getPlaylistInfo(playlistId).filterNotNull().map {
            val playlist = it.toPlaylist()
            val tracks = trackDao.getTracksIds(playlist.trackIds).map { entity ->
                entity.toTrack()
            }

            playlist.copy(trackModels = tracks)
        }
    }.distinctUntilChanged()

    override suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        val newEntity = playlist.toEntity().copy(isDeleted = true, isDownloaded = false)
        playlistDao.updateItem(newEntity)
    }

    override suspend fun addNewPlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.addItem(playlist.copy(isDownloaded = true).toEntity())
    }

    override suspend fun loadPlaylistWithoutTrack(trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.getAll().map {
            val result = Mapper.entitiesToPlaylists(it)
            result.filter { playlist ->
                !playlist.trackIds.contains(trackId) && playlist.isLocal(context)
            }
        }

    }
}