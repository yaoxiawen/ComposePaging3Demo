package com.example.composepaging3demo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay

class SimpleUseSource : PagingSource<Int, SimpleUseBean>() {
    override fun getRefreshKey(state: PagingState<Int, SimpleUseBean>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SimpleUseBean> {
        return try {
            val nextPage = params.key ?: 0
            delay(1500)
            val datas = mutableListOf(
                SimpleUseBean("哈哈${params.key}"),
                SimpleUseBean("哈哈${params.key}"),
                SimpleUseBean("哈哈${params.key}"),
                SimpleUseBean("哈哈${params.key}"),
                SimpleUseBean("哈哈${params.key}")
            )
            LoadResult.Page(
                data = datas,
                prevKey = if (nextPage == 0) null else nextPage - 1,
                nextKey = if (datas.isNotEmpty()) nextPage + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

