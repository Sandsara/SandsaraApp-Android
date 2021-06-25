package com.ht117.sandsara.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ht117.sandsara.model.Playlist
import timber.log.Timber

class PlaylistPaging(val key: String = "", val repo: IPlaylistRepo): PagingSource<String, Playlist>() {
    override fun getRefreshKey(state: PagingState<String, Playlist>): String? {
        return ""
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Playlist> {
        return try {
            val result = repo.browseRemote(key, params.key)
            LoadResult.Page(data = result.data, prevKey = null, nextKey = result.offset)
        } catch (exp: Exception) {
            Timber.d("Failed remote ${exp.message}")
            LoadResult.Error(exp)
        }
    }
}