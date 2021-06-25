package com.ht117.sandsara.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ht117.sandsara.data.NetworkService
import com.ht117.sandsara.data.SandSaraDb
import com.ht117.sandsara.repo.*
import com.ht117.sandsara.ui.browse.playlist.BrowsePlaylistViewModel
import com.ht117.sandsara.ui.browse.track.BrowseTrackViewModel
import com.ht117.sandsara.ui.recommend.RecommendViewModel
import com.ht117.sandsara.ui.library.playlist.AllPlaylistViewModel
import com.ht117.sandsara.ui.library.playlist.detail.DetailPlaylistViewModel
import com.ht117.sandsara.ui.library.track.AllTrackViewModel
import com.ht117.sandsara.ui.settings.SettingViewModel
import com.ht117.sandsara.ui.settings.advance.AdvanceViewModel
import com.ht117.sandsara.ui.settings.lightmode.cycle.CycleViewModel
import com.ht117.sandsara.ui.splash.SplashViewModel
import com.ht117.sandsara.ui.track.TrackViewModel
import com.ht117.sandsara.ui.track.addtoplaylist.AddToPlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appDI = module {
    viewModel { SplashViewModel(get(), get(), get(), get()) }
    viewModel { RecommendViewModel(get(), get()) }
    viewModel { BrowseTrackViewModel(get()) }
    viewModel { BrowsePlaylistViewModel(get()) }
    viewModel { SettingViewModel() }

    viewModel { AllTrackViewModel(get()) }
    viewModel { AllPlaylistViewModel(get()) }

    viewModel { DetailPlaylistViewModel(get(), get(), get()) }
    viewModel { TrackViewModel(get(), get(), get()) }
    viewModel { AddToPlaylistViewModel(get(), get()) }

    viewModel { CycleViewModel(get()) }
    viewModel { AdvanceViewModel(get(), get()) }
}

val repoDI = module {
    single<ITrackRepo> { TrackRepoImpl(get(), get(), get()) }
    single<IPlaylistRepo> { PlaylistRepoImpl(get(), get(), get()) }
    single<IDeviceRepo> { DeviceRepoImpl(get(), get(), get()) }
}

val dataDI = module {
    single { NetworkService.defaultClient() }
    single {
        Room.databaseBuilder(get(), SandSaraDb::class.java, "Sandsara")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL(
                        """insert into playlist (`playlistId`, `trackIds`, `file`, `isRecommended`, `title`, `author`, `trackFiles`, `names`, `images`, `authors`, `tracks`, `isDeleted`, `isDownloaded`) 
                            |values ("favorite", "[]", "[]", 0, "Favorite", "You", "[]", "[]", "[]", "[]", 0, 0, 1)""".trimMargin()
                    )
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    single { (db: SandSaraDb) -> db.playlistDao() }
    single { (db: SandSaraDb) -> db.trackDao() }
    single { (db: SandSaraDb) -> db.paletteDao() }
}