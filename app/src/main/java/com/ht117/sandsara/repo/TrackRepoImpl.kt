package com.ht117.sandsara.repo

import android.content.Context
import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.buildFormula
import com.ht117.sandsara.data.NetworkService
import com.ht117.sandsara.data.SandSaraDb
import com.ht117.sandsara.ext.SyncTrackModel
import com.ht117.sandsara.ext.checkTrackExisted
import com.ht117.sandsara.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class TrackRepoImpl(private val client: HttpClient,
                    private val db: SandSaraDb,
                    private val context: Context) : ITrackRepo {

    private val trackDao by lazy { db.trackDao() }

    override suspend fun loadAll() {
        return withContext(Dispatchers.IO) {
            try {
                val trackCount = trackDao.getTrackCount()

                if (trackCount == 0) {
                    val response = client.get<BaseResponseTrack> {
                        url { encodedPath = "/tracks?view=all" }
                    }
                    val models = response.records.map { it.track.copy(trackId = it.id) }
                    val entities = models.map { it.toEntity() }

                    trackDao.addItems(*entities.toTypedArray())
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
                Timber.d("Failed to store tracks ${exp.message}")
            }
        }
    }

    override suspend fun browseAll(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { Mapper.entitiesToTracks(it).sortedBy { track -> track.name } }
    }

    override suspend fun browseKey(key: String): Flow<List<Track>> {
        return trackDao.getTrackWithKey(key).map { Mapper.entitiesToTracks(it) }
    }

    override suspend fun browseRemote(key: String, offset: String?): PagingData<Track> {
        val formula = buildFormula(key)

        val response = client.get<BaseResponseTrack> {
            url { encodedPath = "/tracks" }
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
        val data = response.records.map { it.track.copy(trackId = it.id) }
        return PagingData(data, response.offset)
    }

    override suspend fun loadRecommendTracks() = withContext(Dispatchers.IO) {
        try {
            val remote = try {
                val response = client.get<BaseResponseTrack> {
                    url { encodedPath = "/tracks?view=recommended" }
                }
                response.records.map {
                    it.track.copy(trackId = it.id)
                }
            } catch (exp: Exception) {
                Timber.d("Failed to load remote recommend track ${exp.message}")
                emptyList()
            }

            if (!remote.isNullOrEmpty()) {
                remote
            } else {
                trackDao.getRecommend().map { it.toTrack() }
            }
        } catch (exp: Exception) {
            Timber.d("Failed ${exp.message}")
            emptyList()
        }
    }

    override suspend fun downloadTrack(file: File, track: TrackUiModel) = flow {
        try {
            val rawClient = NetworkService.customClient()
            val fos = file.outputStream()

            emit(SyncTrackModel.Starting)
            val sandFile = track.track.sandFiles.first()

            val bytes = rawClient.get<ByteReadChannel>(sandFile.url)
            val bufSize = 4096
            val buffer = ByteArray(bufSize)
            var total = 0

            while (true) {
                val result = bytes.readAvailable(buffer, 0, bufSize)
                if (result == -1) {
                    break
                }
                total += result
                val progress = (total * 100 / sandFile.size).toInt()
                if (progress % 5 == 0) {
                    emit(SyncTrackModel.Progress(track.copy(downloadProgress = progress)))
                }
                fos.write(buffer, 0, result)
            }
            fos.close()
            emit(SyncTrackModel.Completed(track.copy(downloadProgress = 100, isDownloaded = true)))
        } catch (exp: Exception) {
            emit(SyncTrackModel.Error("Failed ${exp.message}"))
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    override suspend fun loadDownloadedTrack(): Flow<List<Track>> {
        return trackDao.getAllTracks().map {
            Mapper.entitiesToTracks(it)
        }.map { tracks ->
            tracks.filter { context.checkTrackExisted(it.sandFiles.firstOrNull()?.filename?: "") }
        }
    }

    override suspend fun getTrackWithId(trackId: String): Flow<Track> {
        return trackDao.getTrackWithId(trackId).filterNotNull().map {
            it.toTrack()
        }
    }

    override suspend fun getTrack(track: Track): Flow<Track> {
        val entity = trackDao.getTrack(track.trackId)
        if (entity == null) {
            trackDao.addItem(track.toEntity())
        } else {}
        return trackDao.getTrackWithId(track.trackId).filterNotNull().map { it.toTrack() }
    }

    override suspend fun updateTrackFavorite(track: Track) {
        trackDao.updateTrackFavorite(track.trackId, if (track.isFavorite) 1 else 0)
    }

    override suspend fun loadFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavoritesFlow()
            .map { Mapper.entitiesToTracks(it) }
    }

    override suspend fun getTrackFromFile(fileName: String): Track {
        val all = trackDao.getAll().map { it.toTrack() }
        return all.find { track ->
            track.sandFiles.find {
                it.filename == fileName
            } != null
        }?: Track(name = fileName, author = "Unknown", images = listOf(Image(url = "")), sandFiles = emptyList())
    }
}