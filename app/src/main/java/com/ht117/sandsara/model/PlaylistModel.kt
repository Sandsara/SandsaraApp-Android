package com.ht117.sandsara.model

import android.content.Context
import android.os.Parcelable
import com.ht117.sandsara.ext.PlaylistPath
import com.ht117.sandsara.ext.checkTrackExisted
import com.ht117.sandsara.ext.createTemp
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import timber.log.Timber
import java.io.File

@Serializable
@Parcelize
data class Playlist(
    @Transient val playlistId: String = "",
    @SerialName("trackId") val trackIds: List<String> = emptyList(),
    @SerialName("file") val file: List<SandFile> = emptyList(),
    val recommended: Boolean = false,
    val author: String,
    val name: String,
    @SerialName("files") val trackFiles: List<SandFile> = emptyList(),
    val names: List<String> = emptyList(),
    @SerialName("thumbnails") val images: List<Image> = emptyList(),
    val authors: List<String> = emptyList(),
    val tracks: Int,
    val isDelete: Boolean = false,
    val isDownloaded: Boolean = false,
    @Transient val trackModels: List<Track> = emptyList()
) : Parcelable

fun Playlist.isTheSame(other: Playlist): Boolean {
    return recommended == other.recommended
            && names == other.names
            && authors == other.authors
}

fun Playlist.createFile(context: Context): File {
    val playlistFile = context.createTemp("temp.playlist", PlaylistPath)
    val fos = playlistFile.outputStream()
    trackFiles.forEach { sandFile ->
        fos.write(sandFile.filename.toByteArray(Charsets.US_ASCII))
        fos.write("\r\n".toByteArray(Charsets.US_ASCII))
    }
    fos.close()
    return playlistFile
}

fun List<Track>.createFile(name: String, context: Context): File {
    val playlistFile = context.createTemp("${name}.playlist", PlaylistPath)
    val fos = playlistFile.outputStream()
    val files = map {
        Timber.d("Writing ${it.name}")
        it.sandFiles.first()
    }
    files.forEachIndexed { index, sandFile ->
        fos.write(sandFile.filename.toByteArray(Charsets.US_ASCII))
        if (index < files.size - 1) {
            fos.write("\r\n".toByteArray(Charsets.US_ASCII))
        }
    }
    fos.close()
    return playlistFile
}

@Serializable
@Parcelize
data class PlaylistResponse(
    val id: String,
    @SerialName("fields") val playlist: Playlist,
    val createdTime: String
) :Parcelable

@Serializable
@Parcelize
data class BaseResponsePlaylist(val records: List<PlaylistResponse>, val offset: String? = null) : Parcelable

fun Playlist.isLocal(context: Context): Boolean {
    val isNotDownload = trackFiles.find { !context.checkTrackExisted(it.filename) } != null

    return !isNotDownload
}