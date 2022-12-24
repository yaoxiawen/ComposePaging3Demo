package com.example.composepaging3demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn

class SimpleUseViewModel: ViewModel() {
    companion object {
        private const val PAGE_SIZE = 5
    }

    val recipesPager = Pager(PagingConfig(PAGE_SIZE)) {
        SimpleUseSource()
    }.flow.cachedIn(viewModelScope)
}