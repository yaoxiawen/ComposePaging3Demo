package com.example.composepaging3demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults.elevation
import androidx.compose.material.ButtonDefaults.textButtonColors
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.example.composepaging3demo.ui.theme.ComposePaging3DemoTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposePaging3DemoTheme {
                RefreshLoadUse2()
            }
        }
    }

    @Composable
    fun SimpleUse() {
        val viewModel: SimpleUseViewModel = viewModel()
        val datas = viewModel.recipesPager.collectAsLazyPagingItems()
        LazyColumn(content = {
            itemsIndexed(datas) { _, data ->
                Box(
                    Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(1.dp, Color.Red, RoundedCornerShape(5.dp))
                        .padding(start = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = data?.data ?: "")
                }
            }
        })
    }


    @Composable
    fun RefreshLoadUse() {
        val refreshState = rememberSwipeRefreshState(isRefreshing = false)
        val viewModel: SimpleUseViewModel = viewModel()
        val collectAsLazyPagingItems = viewModel.recipesPager.collectAsLazyPagingItems()
        SwipeRefresh(state = refreshState, onRefresh = {
            collectAsLazyPagingItems.refresh()
        }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                content = {
                    itemsIndexed(collectAsLazyPagingItems) { index, refreshData ->//??????item?????????
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color.Green, shape = RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color.Red,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(start = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = refreshData?.data ?: "")
                        }
                    }
                    when (collectAsLazyPagingItems.loadState.append) {
                        is LoadState.Loading -> {//??????????????????item??????
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "??????????????????")
                                }
                            }
                        }
                        else -> {//?????????????????????????????????????????????item
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "--???????????????????????????--")
                                }
                            }
                        }
                    }
                })
        }
    }

    @Composable
    fun RefreshLoadUse2() {
        val viewModel: SimpleUseViewModel = viewModel()
        val collectAsLazyPagingItems = viewModel.recipesPager.collectAsLazyPagingItems()
        SwipeRefreshList(collectAsLazyPagingItems) {
            itemsIndexed(collectAsLazyPagingItems) { index, refreshData ->//??????item?????????
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.Green, shape = RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(start = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = refreshData?.data ?: "")
                }
            }
        }
    }

    /**
     * ??????????????????
     * ??????1???LazyPagingItems???????????????????????????????????????ViewModel,???ViewMode??????
     * ??????2??????????????? listContent ???????????????????????????????????????LazyListScope????????????
     * */
    @Composable
    fun <T : Any> SwipeRefreshList(
        collectAsLazyPagingItems: LazyPagingItems<T>,
        listContent: LazyListScope.() -> Unit,
    ) {
        val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
        SwipeRefresh(
            state = rememberSwipeRefreshState,
            onRefresh = { collectAsLazyPagingItems.refresh() }
        ) {

            rememberSwipeRefreshState.isRefreshing =
                collectAsLazyPagingItems.loadState.refresh is LoadState.Loading

            // lazyColumn???????????????
            val lazyListState = rememberLazyListState()

            // ???????????????????????????????????????????????????
            val coroutineScope = rememberCoroutineScope()

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                ) {

                // ????????????????????????????????????????????????
                listContent()

                collectAsLazyPagingItems.apply {
                    when {
                        loadState.append is LoadState.Loading -> {
                            //?????????????????????loading
                            item { LoadingItem() }
                        }
                        loadState.append is LoadState.Error -> {
                            //??????????????????
                            item {
                                ErrorMoreRetryItem() {
                                    collectAsLazyPagingItems.retry()
                                }
                            }
                        }


                        loadState.append == LoadState.NotLoading(endOfPaginationReached = true) -> {
                            // ???????????????????????????
                            item {
                                NoMoreDataFindItem(onClick = {
                                    // ???????????? ??????????????????
                                    coroutineScope.launch {
                                        lazyListState.animateScrollToItem(0)
                                    }
                                })
                            }
                        }

                        loadState.refresh is LoadState.Error -> {
                            if (collectAsLazyPagingItems.itemCount <= 0) {
                                // ????????????????????????itemCount??????0????????????????????????
                                item {
                                    ErrorContent() {
                                        collectAsLazyPagingItems.retry()
                                    }
                                }
                            } else {
                                item {
                                    ErrorMoreRetryItem() {
                                        collectAsLazyPagingItems.retry()
                                    }
                                }
                            }
                        }
                        loadState.refresh is LoadState.Loading -> {
                            // ?????????????????????????????????
                            if (collectAsLazyPagingItems.itemCount == 0) {
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * ??????????????????????????????
     * */
    @Composable
    fun ErrorMoreRetryItem(retry: () -> Unit) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(
                onClick = { retry() },
                modifier = Modifier
                    .padding(20.dp)
                    .width(80.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(3.dp),
                colors = textButtonColors(backgroundColor = Color.LightGray),
                elevation = elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                ),
            ) {
                Text(text = "?????????", color = Color.DarkGray)
            }
        }
    }

    /**
     * ????????????????????????
     * */
    @Composable
    fun ErrorContent(retry: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Image(
//                modifier = Modifier.padding(top = 80.dp),
//                painter = painterResource(id = R.drawable.ic_default_empty),
//                contentDescription = null
//            )
            Text(text = "??????????????????????????????", modifier = Modifier.padding(8.dp))
            TextButton(
                onClick = { retry() },
                modifier = Modifier
                    .padding(20.dp)
                    .width(80.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(5.dp),
                colors = textButtonColors(backgroundColor = Color.LightGray),
                elevation = elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                )
                //colors = ButtonDefaults
            ) { Text(text = "??????", color = Color.Gray) }
        }
    }

    /**
     * ?????????????????????????????????...
     * */
    @Composable
    fun LoadingItem() {
        Row(
            modifier = Modifier
                .height(34.dp)
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp),
                color = Color.Gray,
                strokeWidth = 2.dp
            )
            Text(
                text = "?????????...",
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 20.dp),
                fontSize = 18.sp,
            )
        }
    }

    /**
     * ?????????????????????
     * */
    @Composable
    fun NoMoreDataFindItem(onClick: () -> Unit) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(
                onClick = { onClick() },
                modifier = Modifier
                    .padding(20.dp)
                    .width(80.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(3.dp),
                colors = textButtonColors(backgroundColor = Color.LightGray),
                elevation = elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                ),
            ) {
                Text(text = "??????????????????????????? ~~ Click to top", color = Color.Gray)
            }
        }
    }
}