package com.ht117.sandsara.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ht117.sandsara.model.Track
import timber.log.Timber
import java.io.IOException

class TrackPaging(val key: String = "", val repo: ITrackRepo): PagingSource<String, Track>() {

    override fun getRefreshKey(state: PagingState<String, Track>): String? {
        return ""
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Track> {
        return try {
            val result = repo.browseRemote(key, params.key)

            LoadResult.Page(data = result.data, prevKey = null, nextKey = result.offset)
        } catch (exp: Exception) {
            Timber.d("Failed ${exp.message}")
            LoadResult.Error(exp)
        }
    }

}