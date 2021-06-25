package com.ht117.sandsara.ext

import com.ht117.sandsara.adapter.TrackUiModel
import com.ht117.sandsara.model.Track

sealed class SyncTrackModel {

    data class Completed(val track: TrackUiModel): SyncTrackModel()
    object Starting: SyncTrackModel()
    data class Error(val msg: String): SyncTrackModel()
    data class Progress(val track: TrackUiModel): SyncTrackModel()
}

sealed class SyncTrack {
    data class Starting(val track: Track): SyncTrack()
    data class Progress(val track: Track, val progress: Int): SyncTrack()
    data class Completed(val track: Track): SyncTrack()
    data class Error(val msg: String): SyncTrack()
}

sealed class SyncFileProgress {
    object Starting: SyncFileProgress()
    data class Progress(val progress: Int): SyncFileProgress()
    object Finished: SyncFileProgress()
    object Error: SyncFileProgress()
}