package com.ht117.sandsara.data

import androidx.room.*

@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey val playlistId: String,
    val trackIds: String,
    val file: String,
    val isRecommended: Boolean,
    val title: String,
    val author: String,
    val trackFiles: String,
    val names: String,
    val images: String,
    val authors: String,
    val tracks: Int,
    val isDeleted: Boolean,
    val isDownloaded: Boolean
)

@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey val trackId: String,
    val name: String,
    val author: String,
    val images: String,
    val files: String,
    val isRecommended: Boolean,
    val isFavorite: Boolean
)

@Entity(tableName = "palette")
data class PaletteEntity(@PrimaryKey val id: String,
                         val reds: String,
                         val greens: String,
                         val blues: String,
                         val positions: String)