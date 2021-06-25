package com.ht117.sandsara.model

import android.content.Context
import com.ht117.sandsara.BleManager
import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.data.PaletteEntity
import com.ht117.sandsara.data.PlaylistEntity
import com.ht117.sandsara.data.TrackEntity
import com.ht117.sandsara.ext.checkTrackExisted
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Playlist.toTracks(): List<Track> {
    val result = mutableListOf<Track>()

    trackIds.forEachIndexed { index, id ->
        result.add(Track(trackId = id, author = authors[index],
            name = names[index], images = listOf(images[index]),
            recommended = false, trackNumber = 0, isFavorite = false, sandFiles = listOf(trackFiles[index])))
    }
    return result
}

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        trackId = trackId,
        name = name,
        author = author,
        images = Json.encodeToString(images),
        files = Json.encodeToString(sandFiles),
        isRecommended = recommended,
        isFavorite = isFavorite
    )
}

fun TrackEntity.toTrack(): Track {
    return Track(
        trackId = trackId,
        name = name,
        author = author,
        images = Json.decodeFromString(images),
        sandFiles = Json.decodeFromString(files),
        recommended = isRecommended,
        isFavorite = isFavorite
    )
}

fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        playlistId = playlistId,
        trackIds = Json.encodeToString(trackIds),
        file = Json.encodeToString(file),
        title = name,
        author = author,
        isRecommended = recommended,
        trackFiles = Json.encodeToString(trackFiles),
        images = Json.encodeToString(images),
        authors = Json.encodeToString(authors),
        names = Json.encodeToString(names),
        tracks = tracks,
        isDeleted = isDelete,
        isDownloaded = isDownloaded
    )
}

fun PlaylistEntity.toPlaylist(): Playlist {
    return Playlist(
        playlistId = playlistId,
        trackIds = Json.decodeFromString(trackIds),
        file = Json.decodeFromString(file),
        author = author,
        recommended = isRecommended,
        authors = Json.decodeFromString(authors),
        names = Json.decodeFromString(names),
        images = Json.decodeFromString(images),
        name = title,
        trackFiles = Json.decodeFromString(trackFiles),
        tracks = tracks,
        isDelete = isDeleted,
        isDownloaded = isDownloaded
    )
}

fun PaletteResponse.toEntity(): PaletteEntity {
    return PaletteEntity(id = id,
        reds = palettes.red,
        greens = palettes.green,
        blues = palettes.blue,
        positions = palettes.position)
}

fun PaletteEntity.toModel(): Palette {
    return Palette(red = reds, green = greens, blue = blues, position = positions)
}

object Mapper {
    fun playlistsToEntities(playlists: List<Playlist>): List<PlaylistEntity> {
        return playlists.map { it.toEntity() }
    }

    fun entitiesToPlaylists(entities: List<PlaylistEntity>): List<Playlist> {
        return entities.map { it.toPlaylist() }
    }

    fun tracksToEntities(tracks: List<Track>): List<TrackEntity> {
        return tracks.map { it.toEntity() }
    }

    fun entitiesToTracks(entities: List<TrackEntity>): List<Track> {
        return entities.map { it.toTrack() }
    }

    fun tracksToTrackUiModels(context: Context, tracks: List<Track>): List<TrackUiModel> {
        return tracks.map { trackToTrackUiModel(context, it) }
    }

    fun trackToTrackUiModel(context: Context, track: Track): TrackUiModel {
        val isDownloaded = context.checkTrackExisted(track.sandFiles.first().filename)
        return TrackUiModel(track = track, isDownloaded = isDownloaded, downloadProgress = if (isDownloaded) 100 else 0)
    }
}