package com.ht117.sandsara.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlaylistEntity::class, TrackEntity::class, PaletteEntity::class], version = 1)
abstract class SandSaraDb: RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao

    abstract fun trackDao(): TrackDao

    abstract fun paletteDao(): PaletteDao
}