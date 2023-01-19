package com.example.composepaging3demo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay

class SimpleUseSource : PagingSource<List<Int>, SimpleUseBean>() {
    override fun getRefreshKey(state: PagingState<List<Int>, SimpleUseBean>): List<Int>? {
        // 我们需要获取与最新访问索引最接近页面的前一个 Key（如果上一个 Key 为空，则为下一个 Key）
        // anchorPosition 即为最近访问的索引
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//        }
        // 如果请求出错调用refresh方法刷新数据时，当前已经请求到了前三页的数据，则可以通过设置在refresh后从第四页数据开始加载。如果getRefreshKey()返回null，调用refresh后会重新开始从第一页开始加载，产品没特殊要求直接返回null即可。
        return null
    }

    override suspend fun load(params: LoadParams<List<Int>>): LoadResult<List<Int>, SimpleUseBean> {
        return try {
            //pageNum和id同时作为key，请求下一页数据的参数
            val pageNum = params.key?.getOrNull(0) ?: 0
            val id = params.key?.getOrNull(1) ?: 0
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
                prevKey = null,
                nextKey = if (datas.isNotEmpty() && datas.size == params.loadSize) mutableListOf(
                    pageNum + 1,
                    datas[datas.size - 1].id
                ) else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

