package com.ht117.sandsara.model

import android.content.Context
import android.os.Parcelable
import com.ht117.sandsara.ext.PlaylistPath
import com.ht117.sandsara.ext.createTemp
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import timber.log.Timber
import java.io.File

@Serializable
@Parcelize
data class Track(
    @Transient val trackId: String = "",
    val name: String,
    val author: String,
    @SerialName("file") val sandFiles: List<SandFile>,
    @SerialName("thumbnail") val images: List<Image>,
    val recommended: Boolean = false,
    val trackNumber: Int = 0,
    @Transient val isFavorite: Boolean = false
) : Parcelable

fun Track.isTheSame(other: Track): Boolean {
    return recommended == other.recommended
            && isFavorite == other.isFavorite
}

fun Track.createPlaylistFile(context: Context): File? {
    return try {
        val file = context.createTemp("${name}.playlist", PlaylistPath)
        val fos = file.outputStream()
        val sand = sandFiles.first()
        fos.write(sand.filename.toByteArray(Charsets.US_ASCII))
        fos.write("\r\n".toByteArray(Charsets.US_ASCII))
        fos.close()
        file
    } catch (exp: Exception) {
        Timber.d("Failed to create playlist ${exp.message}")
        null
    }
}

@Serializable
@Parcelize
data class TrackResponse(
    val id: String,
    @SerialName("fields") val track: Track,
    val createdTime: String
) : Parcelable

@Serializable
@Parcelize
data class BaseResponseTrack(val records: List<TrackResponse>, val offset: String? = null) : Parcelable

data class PagingData<T>(val data: List<T>, val offset: String? = null)
