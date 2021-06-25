package com.ht117.sandsara.ext

import android.content.Context
import com.ht117.sandsara.model.Track
import java.io.File

fun Context.checkTrackExisted(track: Track): Boolean {
    val name = track.sandFiles.firstOrNull()?.filename?: ""
    if (name.isNullOrEmpty()) return false
    return checkTrackExisted(name)
}

fun Context.checkTrackExisted(name: String): Boolean {
    val root = File(cacheDir, TrackPath)
    val file = File(root, name)
    return file.exists()
}

fun Context.checkPlaylistExist(name: String): Boolean {
    val root = File(cacheDir, PlaylistPath)
    val file = File(root, name)
    return file.exists()
}

fun Context.getFileWithName(name: String, path: String): File? {
    val root = File(cacheDir, path)
    val file = File(root, name)
    if (file.exists()) {
        return file
    }
    return null
}

typealias SandPath = String
const val TrackPath = "tracks"
const val PlaylistPath = "playlists"
const val FirmwarePath  = "firmwares"
/**
 * Create track
 */
fun Context.createTemp(name: String, path: SandPath): File {
    val rootTrack = File(cacheDir, path)
    val file = File(rootTrack, name)
    if (!rootTrack.exists()) {
        rootTrack.mkdirs()
    } else {
        if (file.exists()) {
            file.delete()
        }
    }
    file.createNewFile()
    return file
}